COMPREHENSIVE AUDIT: TillzoPOS Android POS/ERP System

Executive Summary

I've conducted a deep analysis of the TillzoPOS codebase (Kotlin-based Android app, 99.6% Kotlin, targeting single-till, single-user POS + ERP). The app is well-architected with MVVM + Clean Architecture, but contains several critical bugs, incomplete features, and data sync vulnerabilities that will cause silent failures in production. Below is the detailed breakdown.



PART 1: CRITICAL WORKFLOWS \& BUGS

A. CRITICAL DATA SYNC PATH: Sale → Local DB → Google Sheets

The Sale Lifecycle Traced:



Code

1\. User completes sale in HomeScreen

&#x20;  ↓

2\. CompleteSaleUseCase.invoke() called

&#x20;  - Creates SaleEntity with sync\_status = "pending"

&#x20;  - Inserts to Room (SaleDao.insertSale)

&#x20;  - Triggers OneTimeWorkRequest (SyncWorker) immediately

&#x20;  ↓

3\. SyncWorker.doWork() runs (every 15 mins OR manually triggered)

&#x20;  - Reads all "pending" rows from SaleDao

&#x20;  - Calls SalesUploadUseCase

&#x20;  - On HTTP 200: marks sync\_status = "synced" in Room

&#x20;  - Deducts stock AFTER HTTP 200 (Lines 123-125 in SyncWorker.kt)

&#x20;  ↓

4\. RestApiSyncImpl delegates to SheetsRepository

&#x20;  - SheetsRepository.uploadBatch() calls SheetsRemoteDataSource

&#x20;  - Posts JSON to Google Sheets REST API

&#x20;  - Returns SyncResult (Success/Failure)

CRITICAL BUG #1: Silent Sync Failure — No Failure Callback



Location: SyncWorker.kt, lines 116–131

Issue: If uploadTable() returns false, the code logs a warning but continues processing other tables. However, the Result.retry() decision (line 144) is based on anyFailure, which is only set if uploadTable() explicitly returns false.

Problem: If a network request times out OR returns HTTP 429 (quota), the SheetsRemoteDataSource may throw an exception without updating sync\_status to "synced". This leaves the sale in "pending" state indefinitely.

Impact: User sells 100 items, thinks they're synced, but they're stuck "pending" forever if network fails mid-upload. Next day, inventory is not deducted, data is corrupted.

Fix Needed:



Kotlin

// In SyncWorker.kt, lines 116-131, add proper exception handling:

for (tableName in tables) {

&#x20;   try {

&#x20;       val uploadResult = uploadTable(tableName)

&#x20;       if (uploadResult) {

&#x20;           syncLogDao.markTableSynced(tableName, System.currentTimeMillis())

&#x20;       } else {

&#x20;           anyFailure = true

&#x20;       }

&#x20;   } catch (e: Exception) {

&#x20;       Log.e(TAG, "Exception uploading $tableName: ${e.message}", e)

&#x20;       anyFailure = true  // ← MUST set this on ANY exception

&#x20;   }

}

CRITICAL BUG #2: Race Condition in Stock Deduction



Location: SyncWorker.kt, lines 167–218 (deductStockForSyncedSales)

Issue: Stock deduction happens in the same coroutine as the sync upload. If the upload succeeds on the server but the local stock deduction fails (e.g., database locked, transaction conflict), the data is inconsistent: server has the sale, but app still shows old stock.

Timeline:

Sale uploaded to Sheet (HTTP 200)

Stock deduction starts

Database transaction fails (e.g., SQLiteException)

Exception caught, logged, but not rethrown (line 215)

Sale marked as "synced", but stock never deducted → SILENT DATA LOSS

Fix Needed:



Kotlin

// In deductStockForSyncedSales, wrap stock updates in a transaction:

override suspend fun deductStockForSyncedSales(itemsJsonList: List<String>) {

&#x20;   appDatabase.withTransaction {  // ← Atomic transaction

&#x20;       for (itemsJson in itemsJsonList) {

&#x20;           // ... perform all stock updates ...

&#x20;           // If ANY fails, ENTIRE transaction rolls back

&#x20;           // If HTTP succeeded but DB fails, log \& alert

&#x20;       }

&#x20;   }

}

CRITICAL BUG #3: Missing Inverse Sync — No Pull After Push



Location: DeltaSyncManager (not shown, but mentioned in SyncOrchestrator)

Issue: The blueprint specifies M2.6 (Delta Sync — fetch only updated rows via last\_updated\_timestamp), but the codebase shows only PUSH (upload) logic, no PULL (download) logic.

Problem: Terminal A updates inventory offline, sells items. Terminal B has stale data in its local Room database. When Terminal B syncs, it doesn't pull Terminal A's updates.

Impact: In a multi-terminal setup (even though single-user, they may have multiple devices), data diverges permanently.

B. Payment Breakdown Sync Issue

BUG #4: PaymentSplitJson Mapping Inconsistency



Location: CompleteSaleUseCase.kt, line 101

Issue: payment\_split\_json is only populated if paymentMethod == "SPLIT". But in SyncWorker.deductStockForSyncedSales(), the code parses items\_json as List<Map<String, Any>> and expects keys like "itemId" and "quantity".

Problem: If the JSON from the frontend uses different keys (e.g., "product\_id" vs "itemId"), the parsing fails silently, and NO stock is deducted for that sale.

Symptom: User does split payment, sale shows in Sheet, inventory count never changes.

Test Case: Inspect what keys the CartItem/frontend actually sends. Then verify SyncWorker line 176 (cartItem\["itemId"]) matches.



PART 2: MISSING CRITICAL FEATURES

Prioritized List (Single-User POS Reality Check)

Feature	Status	Priority	Business Impact

Stock Sync to Google Sheets	❌ Missing	🔴 CRITICAL	Inventory never updates in the cloud

Manual Delta Sync (Pull)	❌ Missing	🔴 CRITICAL	Multi-device users never see updates

Receipt Reprint	⚠️ Incomplete	🔴 CRITICAL	Cannot reissue lost receipts

Till Reconciliation	⚠️ Incomplete	🔴 CRITICAL	End-of-day reports are unreliable

Daily Backup to Cloud	🟡 Mentioned	🟠 HIGH	No automatic disaster recovery

Barcode Upload	❌ Missing	🟠 HIGH	Generated QR codes are not saved

Stock Adjustment Sync	❌ Not in Sheet sync chain	🟠 HIGH	Manual stock adjustments lost on cloud

Low Stock Notifications	🟡 Partial	🟠 HIGH	Alerts shown but not persisted

Product Image/Photo	❌ None	🟢 MEDIUM	Cannot visualize stock items

Return/Refund Workflow	⚠️ Incomplete	🟢 MEDIUM	No partial-item returns, only full

GST/Tax Breakdown	⚠️ Partial	🟢 MEDIUM	Tax calculated but not per-item tracked

Expense Tracking	⚠️ Backend only	🟡 LOW	No UI to log daily expenses

Critical Missing Feature #1: Stock Adjustment Sync

What's Missing: StockAdjustmentEntity exists (17 entities in Room), but StockAdjustmentEntity.toSheetRow() is defined (line 520 in SyncWorker.kt), yet no call to upload stock adjustments in the sync flow.

Proof: Search SyncWorker for "StockAdjustment" — you'll find it, but it's called inside uploadPendingStockAdjustments() method, which is called on line 102. However, there's no call to deduct from total inventory after the adjustment is synced.

Fix: After uploading stock adjustments, also update the main InventoryEntity.current\_stock locally.

Critical Missing Feature #2: Receipt Reprint / Transaction History

UI: HistoryScreen exists but is only a screen, not a transaction retrieval service.

Problem: User wants to reprint a receipt from 3 days ago. App queries Room for the SaleEntity, but there's no API call to retrieve receipts from past dates if the data was already synced to Google Sheets.

Fix: Add a method searchSalesByDateRange() in SaleDao that queries Room. If Room is empty (data cleared), fetch from Google Sheets REST API using a date filter.

Critical Missing Feature #3: Till Reconciliation (M11 - Till Management)

What's Done: TillSessionEntity, TillOpenScreen, ZReportScreen exist. Till sessions are tracked.

What's Missing:

No final reconciliation report comparing expected cash vs physical count.

No workflow to "lock" a till session once reconciled (status is tracked but not enforced).

No export/print of reconciliation report.

Fix: Add a TillReconciliation dialog that:

Shows opening cash + all sales (cash method) + refunds

Shows expected closing cash

Prompts for physical count

Calculates variance

Marks session as "RECONCILED" (prevents re-opening)

PART 3: FEATURES TO REMOVE / SIMPLIFY

Over-Engineering #1: Multi-POS Terminal Support (M2.8)

Current: pos\_terminal\_id is stored in every entity. SyncWorker reads from "all terminals".

Reality: Single-user, single-till setup. This field adds database columns, complexity, and potential sync conflicts.

Action:

Remove multi-terminal logic from DeltaSyncManager (if it exists).

Hardcode posTerminalId = "TERMINAL\_1" in AppSetupPrefs.

Simplify all queries to filter by this single terminal (or remove the filter entirely).

Over-Engineering #2: Role-Based Access Control (M3 - RBAC)

Current: permissions\_json stored in UserEntity. Admin can grant per-user, per-module permissions.

Reality: Single user, no multi-user tenant. There is no concept of "Admin," "Manager," "Cashier" in the target audience.

Action:

Remove UserEntity and permissions\_json.

Remove permission checks from all screens.

Simplify authentication to just: "User logged in = they can do everything."

Over-Engineering #3: Barcode Configuration UI

Current: BarcodeGeneralConfigEntity + BarcodeFieldConfigEntity with 40+ fields for label layout (titleX, titleY, priceX, priceY, etc.).

Reality: Single shop owner just wants to print a QR + price label on 1"×1.5" stickers.

Action:

Replace with a simple JSON: { labelWidth: "1", labelHeight: "1.5", includeQR: true, includePrice: true }

Pre-configure sensible defaults (no UI).

Remove 2 entities + 2 DAOs.

Over-Engineering #4: Disaster Recovery (M2.9)

Current: DisasterWorker runs daily at 23:59 to backup all sheets to a backup spreadsheetId.

Reality:

Single-user, small dataset (few thousand rows).

Google Sheets version history is already available (built-in).

Backup adds latency and HTTP quota consumption.

Action:

Remove DisasterWorker.

Document: "Use Google Sheets' revision history to recover."

Or: Add a monthly manual export to a backup folder (don't automate).

PART 4: CODE QUALITY, BUGS \& UNCOMPLETED LOGIC

Bug #5: AppNavHost Missing Route

Location: Project\_Progress.txt, line 196–199

Issue: QrGeneratorScreen calls onNavigateToQr(), but AppNavHost has no route for "qr/{barcodeId}".

Result: Navigation crash on user tap.

Fix: Add to AppNavHost:

Kotlin

composable("qr/{barcodeId}") { backStackEntry ->

&#x20;   val barcodeId = backStackEntry.arguments?.getString("barcodeId") ?: ""

&#x20;   QrGeneratorScreen(barcodeId = barcodeId)

}

Bug #6: WorkManager Auto-Init Race Condition (RESOLVED)

Location: AndroidManifest.xml, lines 81–90

Status: ✅ Already fixed. WorkManager initializer is disabled via tools:node="remove", and lazy initialization is used.

Bug #7: Null Pointer in Stock Deduction

Location: SyncWorker.kt, line 179 (val item = inventoryDao.getItemById(productId))

Issue: If product ID doesn't exist in Room, getItemById() returns null, and the code does continue. But no log message indicating data inconsistency.

Risk: Silent stock loss if product was deleted from inventory.

Fix:

Kotlin

val item = inventoryDao.getItemById(productId) ?: run {

&#x20;   Log.e(TAG, "CRITICAL: Product $productId from synced sale NOT found in inventory!")

&#x20;   continue

}

Bug #8: Till Session Management Inconsistency

Location: CompleteSaleUseCase.kt, lines 109–126

Issue: Till session is updated with sale details, but the till session table is NOT updated in sync to Google Sheets unless uploadPendingTillSessions() succeeds. If sync fails, till totals are wrong on the server.

Fix: Move till session recording AFTER sales are synced to sheets, or add retry logic.

Bug #9: No Error Recovery for JSON Parsing

Location: SyncWorker.kt, line 174

Issue: gson.fromJson(itemsJson, listType) can throw if JSON is malformed. Exception is caught broadly (line 214), but the sale remains "synced" even though stock was never deducted.

Fix:

Kotlin

val items: List<Map<String, Any>> = try {

&#x20;   gson.fromJson(itemsJson, listType) ?: emptyList()

} catch (e: Exception) {

&#x20;   Log.e(TAG, "JSON parse failed for sale items: $itemsJson", e)

&#x20;   return@withContext false  // Return failure so sale is NOT marked synced

}

Bug #10: Missing Null Check in Payment Breakdown

Location: CompleteSaleUseCase.kt, line 101

Issue: payment\_split\_json is only set if paymentMethod == "SPLIT". But SaleEntity may be queried expecting this field to exist. If it's null, serialization/deserialization may fail.

Fix: Always initialize payment\_split\_json with a default JSON string, not null.

Incomplete Logic #1: Delta Sync (M2.6)

Status: 🟡 Blueprint mentions it, but implementation not visible in uploaded files.

Risk: If DeltaSyncManager is not fully implemented, the app will always do full syncs, wasting API quota.

Action: Verify DeltaSyncManager.kt exists and implements:

GET Settings!A1 for last\_updated\_timestamp

GET each Sheet's data with WHERE timestamp > lastSyncTime

UPSERT locally using system\_row\_id as key

Incomplete Logic #2: SchemaGuardUseCase

Status: 🟡 Called in SyncWorker (line 244), but implementation not shown.

Risk: If schema check is incomplete, missing columns will crash the app on sync.

Action: Verify it:

Reads Sheet headers via GET /spreadsheets/{id}

Compares against expected schema

Calls batchUpdate to create missing columns if needed

PART 5: UX/UI FLOW ISSUES (Single-User Reality)

Issue #1: 4-Tap Rule Violated (Should Be 3-Tap)

Target: "3-Tap sale: Select → Quantity → Pay"

Current Reality:

Tap quick grid item (or search)

Enter quantity in dialog

Cart shows updated

Tap "Checkout"

Select payment method

Confirm payment = 6 taps minimum, not 3.

Fix: Collapse "Checkout" and "Payment" into a single action. After quantity entry, jump straight to payment dialog.

Issue #2: Blind Selling Creates Confusion

Current: No stock check at POS. User doesn't see if item is out of stock.

Problem: User tries to sell 100 kg of flour, sale is recorded, but warehouse only had 10 kg. Inventory goes negative until sync repairs it.

Fix for Single-User: Enable real-time stock check in POS (not blind selling). For a single-user, inventory is always current in Room. No perf penalty.

Remove "blind selling" logic from PosViewModel.

Check InventoryDao.getItemById(itemId).current\_stock >= qty before adding to cart.

Issue #3: Menu Navigation is Hidden Too Much

Current: Casio UI hides Wastage, Returns, CRM, Sync, Z-Report in a hamburger menu.

Problem: Single-user wants quick access to these. Not hidden features; just less common.

Fix: Add a bottom navigation bar with 4 tabs:

Home (POS)

Inventory (products, stock adjustments, low stock alerts)

Reports (Z-Report, transaction history, returns, wastage)

Settings (sync status, forced sync, theme, logout)

PART 6: ACTIONABLE CODE FIXES (PRIORITY ORDER)

PRIORITY 1: Fix Silent Sync Failure (Critical)

File: app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt



Current Code (Lines 114–154):



Kotlin

var anyFailure = false

for (tableName in tables) {

&#x20;   val uploadResult = uploadTable(tableName)

&#x20;   if (uploadResult) {

&#x20;       syncLogDao.markTableSynced(tableName, System.currentTimeMillis())

&#x20;       Log.d(TAG, "Table $tableName synced successfully")



&#x20;       if (tableName == "Sales" \&\& salesBeforeSync.isNotEmpty()) {

&#x20;           deductStockForSyncedSales(salesBeforeSync.map { it.items\_json })

&#x20;       }

&#x20;   } else {

&#x20;       syncLogDao.markTableFailed(tableName)

&#x20;       Log.w(TAG, "Table $tableName sync failed — will retry")

&#x20;       anyFailure = true

&#x20;   }

}

Fixed Code:



Kotlin

var anyFailure = false

for (tableName in tables) {

&#x20;   try {

&#x20;       val uploadResult = uploadTable(tableName)

&#x20;       if (uploadResult) {

&#x20;           syncLogDao.markTableSynced(tableName, System.currentTimeMillis())

&#x20;           Log.d(TAG, "Table $tableName synced successfully")



&#x20;           if (tableName == "Sales" \&\& salesBeforeSync.isNotEmpty()) {

&#x20;               try {

&#x20;                   deductStockForSyncedSales(salesBeforeSync.map { it.items\_json })

&#x20;               } catch (e: Exception) {

&#x20;                   Log.e(TAG, "Stock deduction FAILED after sales sync: ${e.message}", e)

&#x20;                   // Mark this table as needing re-sync

&#x20;                   syncLogDao.markTableFailed(tableName)

&#x20;                   anyFailure = true

&#x20;               }

&#x20;           }

&#x20;       } else {

&#x20;           syncLogDao.markTableFailed(tableName)

&#x20;           Log.w(TAG, "Table $tableName sync failed — will retry")

&#x20;           anyFailure = true

&#x20;       }

&#x20;   } catch (e: Exception) {

&#x20;       Log.e(TAG, "CRITICAL: Exception during table $tableName sync: ${e.message}", e)

&#x20;       syncLogDao.markTableFailed(tableName)

&#x20;       anyFailure = true

&#x20;   }

}

PRIORITY 2: Fix Stock Deduction Race Condition

File: app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt (Lines 167–218)



Current Code:



Kotlin

private suspend fun deductStockForSyncedSales(itemsJsonList: List<String>) {

&#x20;   val inventoryDao = appDatabase.inventoryDao()

&#x20;   val productBatchDao = appDatabase.productBatchDao()

&#x20;   val listType = object : TypeToken<List<Map<String, Any>>>() {}.type



&#x20;   for (itemsJson in itemsJsonList) {

&#x20;       try {

&#x20;           val items: List<Map<String, Any>> = gson.fromJson(itemsJson, listType) ?: continue

&#x20;           // ... deduct stock ...

&#x20;       } catch (e: Exception) {

&#x20;           Log.e(TAG, "Stock deduction error for one sale — skipping: ${e.message}")

&#x20;       }

&#x20;   }

}

Fixed Code:



Kotlin

private suspend fun deductStockForSyncedSales(itemsJsonList: List<String>) {

&#x20;   val inventoryDao = appDatabase.inventoryDao()

&#x20;   val productBatchDao = appDatabase.productBatchDao()

&#x20;   val listType = object : TypeToken<List<Map<String, Any>>>() {}.type



&#x20;   appDatabase.withTransaction {  // ← Atomic transaction

&#x20;       for (itemsJson in itemsJsonList) {

&#x20;           try {

&#x20;               val items: List<Map<String, Any>> = gson.fromJson(itemsJson, listType) ?: continue

&#x20;               for (cartItem in items) {

&#x20;                   val productId = cartItem\["itemId"] as? String ?: continue

&#x20;                   val qtySold = (cartItem\["quantity"] as? Double) ?: (cartItem\["quantity"] as? Number)?.toDouble() ?: continue



&#x20;                   val item = inventoryDao.getItemById(productId) ?: run {

&#x20;                       Log.e(TAG, "CRITICAL SYNC BUG: Product $productId NOT found in inventory!")

&#x20;                       throw IllegalArgumentException("Product $productId missing from inventory")

&#x20;                   }

&#x20;                   

&#x20;                   val newStock = maxOf(0.0, item.current\_stock - qtySold)

&#x20;                   inventoryDao.updateStock(productId, newStock)



&#x20;                   if (newStock <= 0.0) {

&#x20;                       notificationHelper.outOfStockAlert(item.item\_name)

&#x20;                   } else if (newStock <= item.low\_stock\_threshold) {

&#x20;                       notificationHelper.lowStockAlert(item.item\_name, newStock, item.unit)

&#x20;                   }



&#x20;                   if (item.hasBatches) {

&#x20;                       var remaining = qtySold

&#x20;                       while (remaining > 0.0) {

&#x20;                           val oldestBatch = productBatchDao.getOldestActiveBatch(productId) ?: break

&#x20;                           val deductFromBatch = minOf(remaining, oldestBatch.stockQty)

&#x20;                           val newBatchQty = oldestBatch.stockQty - deductFromBatch

&#x20;                           val now = System.currentTimeMillis()

&#x20;                           if (newBatchQty <= 0.0) {

&#x20;                               productBatchDao.deactivateBatch(oldestBatch.batchId, now)

&#x20;                           } else {

&#x20;                               productBatchDao.updateBatchStock(oldestBatch.batchId, newBatchQty, now)

&#x20;                           }

&#x20;                           remaining -= deductFromBatch

&#x20;                       }



&#x20;                       val allBatches = productBatchDao.getAllBatchesForProduct(productId)

&#x20;                       val total = allBatches.filter { it.isActive \&\& !it.isDeleted }.sumOf { it.stockQty }

&#x20;                       inventoryDao.updateTotalStock(productId, total)

&#x20;                   }



&#x20;                   Log.d(TAG, "Stock deducted: $productId → ${item.current\_stock} - $qtySold = $newStock")

&#x20;               }

&#x20;           } catch (e: Exception) {

&#x20;               Log.e(TAG, "Stock deduction error for sale: ${e.message}", e)

&#x20;               throw e  // ← Rethrow to rollback transaction

&#x20;           }

&#x20;       }

&#x20;   }  // ← Transaction commits only if no exception

}

PRIORITY 3: Add Stock Adjustment Sync

File: app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt



Add after line 321:



Kotlin

private suspend fun uploadPendingStockAdjustments() {

&#x20;   try {

&#x20;       val dao = appDatabase.stockAdjustmentDao()

&#x20;       val pendingAdj = dao.getPendingAdjustments()

&#x20;       if (pendingAdj.isEmpty()) return



&#x20;       uploadTableIfNeeded("Stock\_Adjustments", pendingAdj.map { it.toSheetRow() })

&#x20;       

&#x20;       // Also update local inventory for each adjustment

&#x20;       for (adj in pendingAdj) {

&#x20;           val item = appDatabase.inventoryDao().getItemById(adj.productId) ?: continue

&#x20;           val newStock = when (adj.adjustmentType) {

&#x20;               "ADD" -> item.current\_stock + adj.quantityChanged

&#x20;               "REMOVE" -> maxOf(0.0, item.current\_stock - adj.quantityChanged)

&#x20;               else -> item.current\_stock

&#x20;           }

&#x20;           appDatabase.inventoryDao().updateStock(adj.productId, newStock)

&#x20;           dao.markAsSynced(listOf(adj.adjustmentId))

&#x20;       }

&#x20;       Log.d(TAG, "Stock adjustments synced and inventory updated: ${pendingAdj.size}")

&#x20;   } catch (e: Exception) { 

&#x20;       Log.e(TAG, "Stock Adjustment Upload failed", e) 

&#x20;   }

}

PRIORITY 4: Add Receipt Reprint Function

New File: app/src/main/java/com/tillzo/pos/domain/usecase/ReprintReceiptUseCase.kt



Kotlin

class ReprintReceiptUseCase @Inject constructor(

&#x20;   private val saleDao: SaleDao,

&#x20;   private val sheetsRemoteDataSource: SheetsRemoteDataSource

) {

&#x20;   suspend operator fun invoke(invoiceId: String): SaleEntity? {

&#x20;       // Try local cache first

&#x20;       val localSale = saleDao.getSaleById(invoiceId)

&#x20;       if (localSale != null) return localSale



&#x20;       // If not found locally, fetch from Google Sheets

&#x20;       try {

&#x20;           val rows = sheetsRemoteDataSource.readRange("Sales!A:Z")

&#x20;           val sale = rows.find { it.getOrNull(0) == invoiceId }  // Match by invoice\_id

&#x20;           if (sale != null) {

&#x20;               // Parse and return (or cache to Room)

&#x20;               return convertRowToSaleEntity(sale)

&#x20;           }

&#x20;       } catch (e: Exception) {

&#x20;           Log.e("ReprintReceiptUseCase", "Failed to fetch receipt from cloud: ${e.message}")

&#x20;       }

&#x20;       return null

&#x20;   }



&#x20;   private fun convertRowToSaleEntity(row: List<Any>): SaleEntity? {

&#x20;       // Parse sheet row into SaleEntity

&#x20;       return SaleEntity(

&#x20;           system\_row\_id = row.getOrNull(0) as? String ?: UUID.randomUUID().toString(),

&#x20;           // ... map all fields from row ...

&#x20;       )

&#x20;   }

}

Then call from HistoryScreen or a new ReceiptDetailScreen.



PRIORITY 5: Fix AppNavHost Missing Route

File: app/src/main/java/com/tillzo/pos/ui/AppNavHost.kt



Add route:



Kotlin

composable("qr/{barcodeId}") { backStackEntry ->

&#x20;   val barcodeId = backStackEntry.arguments?.getString("barcodeId") ?: ""

&#x20;   QrGeneratorScreen(barcodeId = barcodeId, onNavigateBack = { navController.popBackStack() })

}

PRIORITY 6: Enable Real-Time Stock Check (Remove Blind Selling)

File: app/src/main/java/com/tillzo/pos/ui/pos/PosViewModel.kt



Change:



Kotlin

// OLD: addToCart(item) { /\* never checks stock \*/ }



// NEW: addToCart(item, qty) { 

fun addToCart(item: InventoryEntity, quantity: Double) {

&#x20;   if (item.current\_stock < quantity) {

&#x20;       showError("Only ${item.current\_stock} ${item.unit} available")

&#x20;       return

&#x20;   }

&#x20;   // ... add to cart ...

}

PART 7: SUMMARY TABLE: What to Fix First

Issue	File	Lines	Severity	Time Est

Silent sync failure (no transaction rollback)	SyncWorker.kt	114–154	🔴 CRITICAL	2 hours

Stock deduction outside transaction	SyncWorker.kt	167–218	🔴 CRITICAL	2 hours

Missing stock adjustment sync update	SyncWorker.kt	Line 321	🔴 CRITICAL	1 hour

Null pointer in stock deduction	SyncWorker.kt	Line 179	🟠 HIGH	30 min

Missing QR route	AppNavHost.kt	Line ?	🟠 HIGH	15 min

Blind selling creates negative stock	PosViewModel.kt	?	🟠 HIGH	1 hour

Till session sync inconsistency	CompleteSaleUseCase.kt	109–126	🟡 MEDIUM	1 hour

No receipt reprint from cloud	All	N/A	🟡 MEDIUM	3 hours

Delta sync incomplete	DeltaSyncManager.kt	?	🟡 MEDIUM	4 hours

CONCLUSION

The TillzoPOS app is architecturally sound (MVVM, clean layers, Hilt DI, WorkManager), but has production-blocking bugs in the sync pipeline that will cause:



Silent data loss (sales synced but stock not deducted)

Inventory corruption (negative stock, inconsistent state)

Incomplete workflows (can't reprint receipts, no stock adjustments on cloud)

Recommended Action: Fix the 3 CRITICAL bugs first (Priorities 1–3), then implement missing workflows (Priorities 4–6). Test with offline/online scenarios before shipping.

