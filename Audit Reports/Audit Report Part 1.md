# Tillzo POS + ERP Codebase Audit Report
**Date:** July 17, 2026  
**Auditor:** Senior Android Architect & Expert POS/ERP Product Manager  
**Codebase:** Tillzo POS (Android Client with Google Sheets REST API Backend)

---

## 🏛️ Executive Summary

An architectural and product-level review of the **Tillzo POS** codebase has been performed. While the application implements a solid offline-first architecture using Jetpack Compose, Room DB, and Hilt, it currently contains **critical sync integrity flaws, severe data corruption hazards, and product gaps** that must be addressed before the app can be safely deployed.

This report outlines our findings categorized into four pillars:
1. **Critical Workflows & Sync Integrity Breakages** (Data loss and corruption risks)
2. **Product & Feature Ground Reality** (POS/ERP functionality gaps vs. over-engineering)
3. **Code Quality, Logical Bugs, and Uncompleted Logic** (NPEs, mathematical errors, hardcoded items)
4. **Actionable Code Fixes** (Concrete, copy-pasteable patches for key vulnerabilities)

---

## 1. 🔄 Pillar 1: Critical Workflows & Data Sync Integrity

The data synchronization engine between the local SQLite database (Room) and Google Sheets (using the REST API via WorkManager and UseCases) has several architectural vulnerabilities:

### A. Un-synced Stock Deductions (Critical Data Drift)
* **How it fails:** When a sale is completed offline, it is stored in Room. Once connection is restored, the `SyncWorker` uploads it. After a successful HTTP 200 upload, `SyncWorker.deductStockForSyncedSales()` is triggered. It calculates the new stock levels and calls `inventoryDao.updateStock()` (or `updateTotalStock()`).
* **The Breakage:** Neither `InventoryDao.updateStock()` nor `InventoryDao.updateTotalStock()` sets the item's `sync_status` to `"pending"`. Because `sync_status` remains `"synced"`, `InventoryUpsertUseCase` never picks up the updated stock count to upload it back to Google Sheets.
* **Impact:** The Google Sheet `Inventory` sheet stock counts remain permanently outdated, making cloud inventory monitoring useless.

### B. Synced Status Disregard on Network Failures (Severe Data Loss Risk)
* **How it fails:** In `SyncWorker.kt`, the upload helper methods for secondary tables (`uploadPendingPurchaseOrders`, `uploadPendingGRNs`, `uploadPendingProductBatches`, `uploadPendingStockAdjustments`) call `sheetsRepository.uploadBatch(tableName, rows)` but **never check the return value** (or ignore it). 
* **The Breakage:** Immediately after calling the upload method, they invoke local DAO updates (e.g. `poDao.markSynced(it.poId)`) to mark the records as synced in the local database.
* **Impact:** If a network drop, Sheets API rate limit, or authorization failure occurs during upload, the remote write will fail, but the local DB will register the items as successfully synced. The system will **never retry** syncing these records, resulting in silent and permanent data loss on the cloud.

### C. The 26-Column Restrictive Range (Data Truncation/Corruption)
* **How it fails:** In `SheetsRepository.fetchDelta(lastTimestamp)`, the API range to query modifications is hardcoded to:
  ```kotlin
  val raw = dataSource.readRange("$tab!A:Z")
  ```
  This retrieves only columns A through Z (the first 26 columns).
* **The Breakage:** The `Vendors` table is defined with 56 columns (A to BD) and `BarcodeGeneralConfigs` with 45 columns (A to AS). Any details located in columns AA and beyond (such as escalations, bank codes, contract URLs, or customized printer settings) are completely cut off. 
* **Impact:** When `DeltaSyncManager` runs its periodic pull and maps row elements to Room entities, the missing keys evaluate to defaults (`""`, `0.0`, or `false`). This wipes out existing geographic, contract, and banking details locally in the database.

### D. Inconsistent POS Terminal ID Fallback (drawer variance trigger)
* **How it fails:** When a Cash Drawer Shift session is opened in `TillViewModel`, `posTerminalId` is resolved via:
  ```kotlin
  private fun posTerminalId() = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
  ```
  This creates a till session under the identifier `"TERM_1"` if the sheet is not yet provisioned. 
* **The Breakage:** In `ExpenseViewModel.kt`, logging an expense attempts to deduct the cash from the drawer using:
  ```kotlin
  val posTerminalId = appSetupPrefs.spreadsheetId.take(20)
  ```
  This misses the `.ifBlank { "TERM_1" }` safety wrapper.
* **Impact:** If the user logs an expense before sheets are synced or during offline usage, `posTerminalId` resolves to `""`. The application fails to retrieve the active session (looking for `""` instead of `"TERM_1"`), and the cash drawer's expected balance is **never deducted**, creating a false cash variance upon shift closure.

---

## 2. 🛒 Pillar 2: Product & Feature Ground Reality (POS + ERP)

Evaluating the feature set against standard single-user POS/ERP expectations reveals several areas of mismatch:

### A. Prioritized Feature Gaps
1. **Cash Drawer Verification / Variance Recording:** The Z-Report/Close Day flow does not allow the cashier to enter the counted physical cash in the drawer. It automatically sets `closingCash = expectedCash` and `netCash = 0.0`, masking cash shortages or surpluses.
2. **Missing Cashier Locking Mechanism:** When no till session is open, `HomeScreen` correctly displays an overlay. However, if a cashier navigates to another screen, there is no system-wide guard. A global middleware check should ensure no transactions can be initiated without an active session.
3. **Comprehensive Z-Report Breakdowns:** A standard Z-Report requires payment type breakdowns (Cash, Card, Wallet, Udhaar). Currently, the print slip computes this but the screen itself only shows gross totals.

### B. Bloat & Over-Engineering
1. **Excessive Vendor Profiles:** The `vendors` table contains 56 columns including three layers of escalation contacts (L1/L2/L3 names, phones, emails), preferred currency, branch codes, bank account details, and compliance certificates. For a single-user store, this is severe over-engineering that adds unnecessary database size and UI noise.
2. **Google Drive PDF/Contract Attachments:** Storing PDF contract attachments on Google Drive linked inside Vendor and GRN records (`contractFileId`, `contractFileUrl`) is overly complex. Store owners want fast local operations, not Google Drive document integration.
3. **SYS_DB_DO_NOT_TOUCH Schema Verification:** Creating and programmatically hiding system database sheets adds lag to the startup/sync cycle and introduces additional error vectors.

---

## 3. 🐛 Pillar 3: Code Quality, Bugs, and Uncompleted Logic

### A. The "Phantom Initial Stock" Bug (Data Destruction)
* **File:** `InventoryCrudViewModel.kt` & `AddProductUseCase.kt`
* **Bug:** When creating a product manually with an initial stock, the product is inserted into the `Inventory` table, and `AddProductUseCase` creates a corresponding entry in the `ProductBatchEntity` table. However, if a product is updated or saved again through `InventoryCrudViewModel.saveItem()`, there is no check to verify that a batch exists if the stock has been modified.
* **Recalculation Overwrite:** When a GRN is confirmed later, `ConfirmGrnUseCase` calls `inventoryRepository.recalculateTotalStock(productId)`, which sums the stock of all active batches in `ProductBatchEntity`. If the initial stock wasn't mapped to a batch (e.g. if the batch was deleted, or if the product was synced downstream without its batches), `recalculateTotalStock` will calculate stock solely from GRN batches and write it to `current_stock`, completely deleting the initial stock.

### B. The "Stock Adjustment Reversion" Bug (Inconsistent State)
* **File:** `StockAdjustmentScreen.kt` & `ManualStockAdjustmentUseCase.kt`
* **Bug:** In `ManualStockAdjustmentUseCase.kt`, making a stock correction updates `current_stock` directly in the `Inventory` table. It does not update or insert any adjustments into the `ProductBatchEntity` table.
* **Trigger:** The next time a GRN is received or any batch edits occur, the system triggers `recalculateTotalStock(productId)`. This sums the unadjusted batches and overwrites the product's `current_stock`, reverting the manual adjustment.

### C. Hardcoded Admin PIN (Security Risk)
* **File:** `HomeScreen.kt:L866`
* **Bug:** The Admin PIN used to secure pinning/unpinning items to the quick access grid is hardcoded directly in the compose code:
  ```kotlin
  if (pinInput == "1234") { ... }
  ```
  This is a critical security smell. It should be stored as a hashed preference or verified against the logged-in cashier's credentials.

### D. Hardcoded Currency Symbols (UX Inconsistency)
* **Bug:** The barcode detail screen and other inventory sheets use a hardcoded Indian Rupee (`"₹"`) symbol, while the cart and receipts use `"Rs"`. The symbol should be driven by the user's localized `BarcodeGeneralConfigEntity.currencySymbol` preference.

---

## 4. 🛠️ Pillar 4: Actionable Code Fixes

Here are the target replacements required to resolve the most critical sync and database bugs:

### Fix 1: Fix the REST Range Truncation in Delta Sync
Modify `SheetsRepository.kt` to query up to column `ZZ` instead of `Z` to prevent data loss on wide tables like `Vendors` and `BarcodeGeneralConfigs`.

```diff
- val raw = dataSource.readRange("$tab!A:Z")
+ val raw = dataSource.readRange("$tab!A:ZZ")
```

---

### Fix 2: Check Upload Results in `SyncWorker.kt` (Prevent Silent Sync Drop)
Update `SyncWorker.kt` to only mark records as synced if `sheetsRepository.uploadBatch` returns a `SyncResult.Success`.

```kotlin
    private suspend fun uploadPendingPurchaseOrders() {
        try {
            val poDao = appDatabase.purchaseOrderDao()
            val pendingPOs = poDao.getPendingPOs()
            val existingIds = sheetsRepository.getExistingUuids("Purchase_Orders")
            val newPOs = pendingPOs.filter { it.poId !in existingIds }
            
            if (newPOs.isNotEmpty()) {
                val result = sheetsRepository.uploadBatch("Purchase_Orders", newPOs.map { it.toSheetRow() })
                if (result is SyncResult.Success) {
                    newPOs.forEach { poDao.markSynced(it.poId) }
                } else {
                    Log.w(TAG, "PO Upload failed: $result")
                    return
                }
            }
            
            pendingPOs.forEach { po ->
                val items = poDao.getPOItems(po.poId)
                if (items.isNotEmpty()) {
                    val itemResult = sheetsRepository.uploadBatch("PO_Items", items.map { it.toSheetRow() })
                    if (itemResult is SyncResult.Success) {
                        // Mark items as synced if PO_Items has a sync status
                    }
                }
            }
        } catch (e: Exception) { Log.e(TAG, "PO Upload failed", e) }
    }
```

Apply this same checking behavior to `uploadPendingGRNs()`, `uploadPendingProductBatches()`, `uploadPendingStockAdjustments()`, and `uploadPendingTillSessions()` inside [SyncWorker.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt).

---

### Fix 3: Trigger Sync for Stock Deductions in `SyncWorker.kt`
Ensure that when a local stock level is updated due to a sale deduction, it sets `sync_status = "pending"` so the new stock levels are uploaded.

Update the `InventoryDao.kt` query:
```kotlin
    @Query("UPDATE Inventory SET current_stock = :newStock, sync_status = 'pending', updated_at = :time WHERE system_row_id = :id")
    suspend fun updateStock(id: String, newStock: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE Inventory SET current_stock = :total, sync_status = 'pending', updated_at = :time WHERE system_row_id = :productId")
    suspend fun updateTotalStock(productId: String, total: Double, time: Long = System.currentTimeMillis())
```

---

### Fix 4: Fix Manual Stock Adjustment Batch Desync
Modify `ManualStockAdjustmentUseCase.kt` so that stock corrections also apply to the product's batch (or create a correction batch), ensuring `recalculateTotalStock` doesn't overwrite it.

```kotlin
    suspend operator fun invoke(
        systemRowId: String,
        quantityChange: Double,
        reason: String,
        adjustedByUserId: String
    ) {
        val product = inventoryRepository.getItemById(systemRowId) ?: return

        // 1. Log the stock adjustment history record
        val adjustment = StockAdjustmentEntity(
            productId = systemRowId,
            adjustmentType = if (quantityChange > 0) "RECEIVED" else "CORRECTION",
            quantityChanged = quantityChange,
            reason = reason,
            adjustedBy = adjustedByUserId
        )
        stockAdjustmentRepository.insertStockAdjustment(adjustment)

        // 2. Adjust active batches to match the change
        if (product.hasBatches) {
            val oldestBatch = productBatchRepository.getOldestActiveBatch(systemRowId)
            if (oldestBatch != null) {
                // Adjust quantity on the oldest batch
                val newBatchQty = maxOf(0.0, oldestBatch.stockQty + quantityChange)
                productBatchRepository.updateBatchStock(oldestBatch.batchId, newBatchQty)
            } else {
                // Create a batch if missing
                val newBatch = ProductBatchEntity(
                    batchId = java.util.UUID.randomUUID().toString(),
                    productId = systemRowId,
                    barcodeId = product.barcode_id,
                    batchNumber = "ADJ-BATCH",
                    manufacturingDate = "",
                    expiryDate = "",
                    stockQty = maxOf(0.0, quantityChange),
                    costPrice = product.cost_price,
                    sellingPrice = product.price_per_unit,
                    isActive = true,
                    posTerminalId = product.pos_terminal_id
                )
                productBatchRepository.insertBatch(newBatch)
            }
        }

        // 3. Recalculate total stock (updates product's current_stock and sets pending status)
        inventoryRepository.recalculateTotalStock(systemRowId)
    }
```

---

### Fix 5: Unify Terminal ID Fallback in `ExpenseViewModel.kt`
Ensure `ExpenseViewModel` resolves terminal IDs consistently with `TillViewModel`.

```kotlin
// In ExpenseViewModel.kt:
- val posTerminalId = appSetupPrefs.spreadsheetId.take(20)
+ val posTerminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
```
