# TillzoPOS Audit Consolidation & Master Prompts

This document combines every single point from the 6 parts of the Tillzo Audit Reports, maps them into a master database of **37 distinct bugs, missing features, and bloat items**, and structures them into **11 sequential, ultra-detailed prompts**.

---

## 🔍 Master Index of 37 Audit Findings

Below is a trace of all findings found in the audit reports, mapped to the phase in which they are resolved:

| ID | Issue / Feature / Bloat Description | Severity | Affected Files | Phase |
|---|---|---|---|---|
| **1** | Silent sync failure: exceptions (timeouts/429s) in `SyncWorker` skip retry | 🔴 Critical | `SyncWorker.kt` | **Phase 1** |
| **2** | Synced status bypass: DB updates run before verify upload success | 🔴 Critical | `SyncWorker.kt` | **Phase 1** |
| **3** | REST range truncation: column read range hardcoded to A:Z instead of A:ZZ | 🔴 Critical | `SheetsRepository.kt` | **Phase 1** |
| **4** | Sales upload dedup mismatch: compares `sync_uuid` with `system_row_id` | 🔴 Critical | `SalesUploadUseCase.kt`, `SheetsRepository.kt` | **Phase 1** |
| **5** | Vendor upsert non-null check bug: marks synced on error responses | 🔴 Critical | `VendorUpsertUseCase.kt` | **Phase 1** |
| **6** | `sync_status` key collision: hardcoded "synced" in `toSyncMap()` values | 🟡 High | `InventoryUpsertUseCase.kt` | **Phase 1** |
| **7** | Column count mismatch: maps 25 values for 26-column inventory schema | 🟡 High | `InventoryUpsertUseCase.kt`, `SheetColumns.kt` | **Phase 1** |
| **8** | DeltaSyncManager cursor bug: settings timestamp zero locks delta sync | 🔵 Medium | `DeltaSyncManager.kt` | **Phase 1** |
| **9** | OkHttp client logging leak: Level.BODY token leak in release builds | 🔵 Medium | `SheetsApiClient.kt` | **Phase 1** |
| **10** | Stock adjustment reversion: manual adjustments erased by GRN recalc | 🔴 Critical | `ManualStockAdjustmentUseCase.kt`, `InventoryRepositoryImpl.kt` | **Phase 2** |
| **11** | Delayed stock deduction: stock only deducted after HTTP 200 upload | 🔴 Critical | `CompleteSaleUseCase.kt`, `SyncWorker.kt`, `PosViewModel.kt` | **Phase 2** |
| **12** | Stock deduction race condition: no Room transaction during deductions | 🔴 Critical | `SyncWorker.kt`, `CompleteSaleUseCase.kt` | **Phase 2** |
| **13** | Stock deduction null pointer: crashes when sold product is deleted in DB | 🟡 High | `SyncWorker.kt`, `CompleteSaleUseCase.kt` | **Phase 2** |
| **14** | JSON parse exception safety: malformed sales items crash silent deduction | 🟡 High | `SyncWorker.kt` | **Phase 2** |
| **15** | `PaymentSplitJson` mapping inconsistency: split details are null/unhandled | 🟡 High | `CompleteSaleUseCase.kt` | **Phase 2** |
| **16** | Stock Adjustment Sync missing: adjustment events never push to sheets | 🟡 High | `SyncWorker.kt`, `StockAdjustmentEntity.kt` | **Phase 2** |
| **17** | "Print Receipt" button on checkout is a dead button (empty onClick) | 🔴 Critical | `ReceiptScreen.kt` | **Phase 3** |
| **18** | Printer settings IP/MAC addresses are never saved to disk | 🔴 Critical | `PrinterSettingsViewModel.kt`, `AppSetupPrefs.kt` | **Phase 3** |
| **19** | Hardcoded test MAC address (`"00:00:00:00:00:00"`) in three print VM calls | 🔴 Critical | `HistoryViewModel.kt`, `QrGeneratorViewModel.kt`, `ZReportViewModel.kt` | **Phase 3** |
| **20** | Navigation crash: AppNavHost missing route for QR code generator | 🔴 Critical | `AppNavHost.kt` | **Phase 4** |
| **21** | Hardcoded Admin PIN check: gate hardcoded to "1234" in HomeScreen | 🔵 Medium | `HomeScreen.kt` | **Phase 4** |
| **22** | Hardcoded currency symbols: INR symbol `₹` inconsistent with `Rs` | 🔵 Medium | `BarcodePrintSettingsScreen.kt`, `InventoryCrudViewModel.kt` | **Phase 4** |
| **23** | Discount input missing: no UI elements to trigger VM discount apply | 🟠 High | `HomeScreen.kt`, `PosViewModel.kt` | **Phase 4** |
| **24** | Till Reconciliation gaps: expected cash auto-closing without cash input | 🔴 Critical | `ZReportScreen.kt`, `ZReportViewModel.kt`, `TillSessionEntity.kt` | **Phase 5** |
| **25** | Missing till locking mechanism: sales can run without open till session | 🔴 Critical | `HomeScreen.kt`, `CompleteSaleUseCase.kt` | **Phase 5** |
| **26** | History list OOM risk: no pagination when loading thousands of sales | 🔵 Medium | `SaleDao.kt`, `HistoryScreen.kt` | **Phase 5** |
| **27** | Missing cloud-reprint fallback: can't reprint synced sales cleared from DB | 🔵 Medium | `ReprintReceiptUseCase.kt`, `HistoryScreen.kt` | **Phase 5** |
| **28** | Offline indicator missing: user doesn't know if live or using local Room | 🟢 Low | `HomeScreen.kt` | **Phase 5** |
| **29** | Payment quick cash buttons: manual inputs required for cash drawer bills | 🟢 Low | `PaymentDialog.kt` | **Phase 5** |
| **30** | Hardware diagnostic dashboard: no way to test print/scan interfaces | 🟢 Low | `HardwareDiagnosticScreen.kt` (New) | **Phase 5** |
| **31** | Vendor Profile columns bloat: 56 columns (L1/L2/L3 escalations, IBAN) | 🔵 Medium | `PurchaseOrderEntity.kt` (VendorEntity), `SheetColumns.kt` | **Phase 6** |
| **32** | Barcode Layout fields bloat: 40+ layout placement DB columns | 🔵 Medium | `BarcodeGeneralConfigEntity.kt`, `BarcodeFieldConfigEntity.kt` | **Phase 6** |
| **33** | Google Drive Contract uploads: complex file attachments on Drive | 🔵 Medium | `VendorUpsertUseCase.kt`, `ConfirmGrnUseCase.kt` | **Phase 6** |
| **34** | ShardingWorker & DisasterWorker: monthly sheets sharding and backups | 🔵 Medium | `ShardingWorker.kt`, `DisasterWorker.kt`, `SyncWorker.kt` | **Phase 7** |
| **35** | RBAC / Permission gates bloat: per-user permission configurations | 🔵 Medium | `UserManagementScreen.kt`, `UserDao.kt`, `SessionGuardUseCase.kt` | **Phase 7** |
| **36** | PO/GRN multi-step approvals: complex workflow for single cashier | 🔵 Medium | `CreatePurchaseOrderScreen.kt`, `PODetailScreen.kt` | **Phase 7** |
| **37** | Empty deletion processes: dead-code loops for deleting sales/Khata events | 🟢 Low | `SalesUploadUseCase.kt`, `KhataEventUseCase.kt` | **Phase 7** |

---

# 🚀 The 11 Ultra Master Prompts

Use these prompts sequentially. Do not run a prompt out of order.

<!-- slide -->

## 📋 Prompt 1: Foundational Sync Integrity & API Quota Protections

```text
You are an expert Android developer. Your task is to resolve critical bugs and data integrity issues in the Google Sheets background sync pipeline of TillzoPOS.

### Targets & Code Actions:
1. **Prevent Silent Sync Failures (Ignore Upload Results)**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt`
   - **Issues [IDs 1, 2, 5]:** 
     - In the methods `uploadPendingPurchaseOrders()`, `uploadPendingGRNs()`, `uploadPendingProductBatches()`, and `uploadPendingStockAdjustments()`, the worker calls `sheetsRepository.uploadBatch(...)` but ignores the return value. It unconditionally executes the local DAO `markSynced()` database operations immediately afterward. If the network drops or the Google Sheets API returns a rate-limit error, the local DB flags rows as synced, resulting in permanent data loss on the cloud.
     - In `VendorUpsertUseCase.kt`, the upload checks if `result != null`. Since `uploadBatch` returns a `SyncResult` sealed class (which is always non-null, even on failure like `SyncResult.ServerError`), it incorrectly marks vendors as synced.
   - **Fix:** Update all upload methods inside `SyncWorker.kt` to explicitly check if `result is SyncResult.Success` before marking the database rows as synced. Wrap table uploads in a robust `try/catch` and ensure `anyFailure = true` is set on exceptions so WorkManager properly schedules a retry. Fix the check in `VendorUpsertUseCase.kt` to `if (result is SyncResult.Success)`.

2. **Fix REST range truncation**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/repository/SheetsRepository.kt`
   - **Issue [ID 3]:** In `SheetsRepository.fetchDelta(lastTimestamp)`, the API range to query updates is hardcoded to "$tab!A:Z" (26 columns). However, tables like `Vendors` (56 columns, A:BD) and `BarcodeGeneralConfigs` (45 columns, A:AS) exceed this range. This results in truncation, and when delta sync saves default values locally, it wipes out details in Room.
   - **Fix:** Modify range queries from `"$tab!A:Z"` to `"$tab!A:ZZ"`.

3. **Fix Sales Deduplication Bug**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/data/repository/SheetsRepository.kt`
     - `app/src/main/java/com/tillzo/pos/domain/sync/usecase/SalesUploadUseCase.kt`
   - **Issue [ID 4]:** The deduplication logic uses `sale.system_row_id !in existingIds`, but `getExistingUuids("Sales")` reads Column A of the Google Sheet. Column A in the sales sheet is `"invoice_id"` (the `sync_uuid`), not `system_row_id`. This mismatch causes the dedup logic to fail completely, posting duplicate sale rows to Google Sheets on every sync retry.
   - **Fix:** Update `getExistingUuids` to take an optional `columnLetter: String = "A"`. Overload or update `SalesUploadUseCase.kt` to read Column A (`sync_uuid`) and compare against `sale.sync_uuid` instead of `system_row_id`.

4. **Fix Inventory Upsert Schema Count and Key Collision**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/domain/sync/usecase/InventoryUpsertUseCase.kt`
     - `app/src/main/java/com/tillzo/pos/data/local/entity/InventoryEntity.kt`
     - `app/src/main/java/com/tillzo/pos/data/sync/options/columns/SheetColumns.kt`
   - **Issues [IDs 6, 7]:**
     - In `toSyncMap()` of `InventoryEntity.kt`, `sync_status` is mapped to `"synced"`. If sheets write succeeds but local mark fails, it stays `"pending"` locally. Next retry, dedup check (`getExistingUuids`) filters it out, so it never gets updated locally to `"synced"` - stuck in pending.
     - The `InventoryUpsertUseCase` maps 25 values for 26-column inventory schema (`updated_at` at the end), dropping `"updated_at"` and causing data drift. Same pattern exists in `CategoryUpsertUseCase.kt` and `ProductUnitUpsertUseCase.kt`.
   - **Fix:** Remove `sync_status` from the maps/values written. Make `sync_status` a fixed column value written directly at the Sheet level (always `"synced"` in the sheets column). Align the column mappings and values array so that `updated_at` is preserved correctly.

5. **Fix DeltaSyncManager First-Run Zero Timestamp Cursor**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/sync/options/delta/DeltaSyncManager.kt`
   - **Issue [ID 8]:** If the Settings tab is not populated on first run, it returns 0 for remote timestamp, making local delta cursor save 0 and never sync again.
   - **Fix:** If `remoteTimestamp == 0L` on the first run, set the local cursor to `0L` and allow subsequent sync iterations to check for changes rather than skipping entirely.

6. **HttpLoggingInterceptor Duplication and Release Leak**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/remote/client/SheetsApiClient.kt`
   - **Issue [ID 9]:** Full request/response bodies (including OAuth headers and tokens) are logged via `HttpLoggingInterceptor.Level.BODY` in production.
   - **Fix:** Configure the logging interceptor level to `HttpLoggingInterceptor.Level.BODY` only if `BuildConfig.DEBUG` is true, otherwise use `HttpLoggingInterceptor.Level.NONE`.

Review the implementation, compile, and run tests to ensure background synchronization works reliably under network loss and doesn't write duplicate rows.
```

---

<!-- slide -->

## 📋 Prompt 2: Local-First Stock Deduction Architecture

```text
You are an expert Android developer. Your task is to refactor the stock deduction lifecycle of TillzoPOS from "Blind Selling" to a robust Local-First architecture.

### Targets & Code Actions:
1. **Deduct Stock Immediately on Sale**:
   - **File:** `app/src/main/java/com/tillzo/pos/domain/usecase/CompleteSaleUseCase.kt`
   - **Issues [IDs 11, 12, 13, 15]:** 
     - Currently, stock is only deducted in `SyncWorker.kt` (using `deductStockForSyncedSales`) after receiving an HTTP 200 OK from the Google Sheets API. If the device is offline, on-screen stock remains unchanged, allowing cashiers to oversell.
     - Split details (`payment_split_json`) are null or unhandled.
   - **Fix:** Inject `InventoryDao` and `ProductBatchDao` (or repositories) into `CompleteSaleUseCase.kt`. Inside the Room transaction block (where the `SaleEntity` is inserted), deduct the sold quantities from local inventory (`InventoryEntity.current_stock`) and update batch inventory in FIFO order if the item has batches.
   - Ensure the query to update the stock sets `sync_status = 'pending'` on the modified product record so the new stock levels will be synced back to the cloud on the next sync cycle.
   - Ensure `payment_split_json` is always initialized with a default empty JSON string instead of `null` to avoid serialization/deserialization exceptions. Add a null check for `PaymentSplit` maps.

2. **Remove Post-Sync Stock Deduction**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt`
   - **Issues [IDs 11, 12, 13, 14]:**
     - Deducting stock post-sync can fail due to database lock/conflicts, causing data drift (sale synced but stock unchanged).
     - Deleting products in Room causes a Null Pointer Exception if a synced sale tries to deduct a deleted product.
     - Broad `try/catch` block ignores JSON parse errors of `items_json`.
   - **Fix:** Remove the call to `deductStockForSyncedSales()` inside the sales upload worker execution block to prevent double-deducting stock when online sync catches up.
   - If keeping a fallback method, ensure it runs under an atomic Room database transaction (`appDatabase.withTransaction`) and handles JSON parsing of `items_json` safely, catching `JsonSyntaxException` without skipping transaction rollbacks on DB failure. Add proper logs if a product in a sale is missing from the local inventory.

3. **Enable Real-Time Stock UI Checks**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/home/PosViewModel.kt`
   - **Fix:** Change the checkout/cart addition logic to check local `current_stock` before adding an item or proceeding to payment. Provide a warning UI state if the quantity exceeds available stock.

4. **Add Stock Adjustment Sync**:
   - **Files:** `SyncWorker.kt`, `StockAdjustmentEntity.kt`
   - **Issue [ID 16]:** Stock adjustments are defined and logged locally but are never pushed to Sheets in the sync chain.
   - **Fix:** Wire `uploadPendingStockAdjustments()` into the sync chain. After uploading stock adjustments, update the local inventory (`current_stock`) and mark adjustments as synced.

5. **Unify Terminal ID Fallback**:
   - **Files:** `app/src/main/java/com/tillzo/pos/ui/home/PosViewModel.kt`, `app/src/main/java/com/tillzo/pos/ui/home/ExpenseViewModel.kt` (if applicable), `SyncWorker.kt`
   - **Fix:** Standardize and resolve the POS terminal ID everywhere using a central helper or by writing `appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }`.

Verify that completing a sale offline immediately updates the on-screen inventory stock, flags the inventory as pending sync, and doesn't perform duplicate stock deductions when connection is restored.
```

---

<!-- slide -->

## 📋 Prompt 3: Manual Stock Adjustments & Batch Desync Fixes

```text
You are an expert Android developer. Your task is to resolve critical data corruption in manual stock adjustments and batch recalculations.

### Context:
When an operator makes a manual stock correction, the system updates `current_stock` directly in the `Inventory` table. However, when a Goods Received Note (GRN) is confirmed via `ConfirmGrnUseCase.kt`, it calls `recalculateTotalStock()`, which sums the quantities of all active batches in the `ProductBatchEntity` table and overwrites `current_stock`. Because the manual adjustment was never recorded in the batch table, the correction is silently erased.

### Targets & Code Actions:
1. **Update Batches in Manual Adjustments**:
   - **File:** `app/src/main/java/com/tillzo/pos/domain/usecase/inventory/ManualStockAdjustmentUseCase.kt`
   - **Issue [ID 10]:** Manual stock adjustments are silently reverted by the next GRN.
   - **Fix:** Refactor `ManualStockAdjustmentUseCase.kt` to modify stock at the batch level.
     - If the product has active batches, apply the quantity adjustment to the oldest or newest active batch.
     - If no active batch exists, insert a new `ProductBatchEntity` representing the adjustment (e.g., batch number "ADJ-BATCH") with the adjusted quantity.
     - After batch modification, call `inventoryRepository.recalculateTotalStock(productId)` to update `current_stock` and mark `sync_status = 'pending'`.

2. **Add Missing Batch DAO Query**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/local/dao/ProductBatchDao.kt`
   - **Fix:** Add a query to retrieve the newest active batch for a product:
     ```kotlin
     @Query("SELECT * FROM product_batches WHERE productId = :productId AND isActive = 1 AND isDeleted = 0 ORDER BY createdAt DESC LIMIT 1")
     suspend fun getNewestActiveBatch(productId: String): ProductBatchEntity?
     ```

Test this flow by creating a product, executing a manual stock adjustment (-5 units), and then confirming a GRN restock. Verify that the final stock is correct (retaining the manual adjustment subtraction).
```

---

<!-- slide -->

## 📋 Prompt 4: Persistent Printer Settings & Receipt Printing

```text
You are an expert Android developer. Your task is to connect the Bluetooth/Network printer settings to the actual receipt checkout flow.

### Targets & Code Actions:
1. **Persist Printer Configuration in Preferences**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/local/prefs/AppSetupPrefs.kt`
   - **Issue [ID 18]:** Printer MAC/IP address settings are only saved in temporary view model memory flow and clear on app restart.
   - **Fix:** Add persistent properties `printerMac` (default `""`) and `printerIp` (default `"192.168.1.100"`) using the EncryptedSharedPreferences wrapper.
   - **File:** `app/src/main/java/com/tillzo/pos/ui/hardware/printer/PrinterSettingsViewModel.kt`
   - **Fix:** Refactor the view model to read and write directly to `AppSetupPrefs` when updating MAC and IP addresses, ensuring settings persist across application restarts.

2. **Wire the "Print Receipt" Button on Checkout Completion**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/home/ReceiptScreen.kt`
   - **Issue [ID 17]:** The "Print Receipt" button on the screen after checkout is a dead shell with an empty `onClick` handler.
   - **Fix:** Find the `Print Receipt` outlined button. Replace the empty `onClick` lambda with code that:
     - Retrieves the saved `printerMac` from `AppSetupPrefs`.
     - Displays a Snackbar if no printer is configured.
     - Spawns a coroutine to call `escPosPrinter.printViaBluetooth(mac, receiptText)` or the appropriate print helper using the completed sale entity metadata.

3. **Remove Hardcoded Printer MAC Addresses**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/ui/history/HistoryViewModel.kt`
     - `app/src/main/java/com/tillzo/pos/ui/qr/QrGeneratorViewModel.kt`
     - `app/src/main/java/com/tillzo/pos/ui/reports/ZReportViewModel.kt`
   - **Issue [ID 19]:** Three screens print hardcoded barcode/z-report tickets to placeholder MAC `"00:00:00:00:00:00"`.
   - **Fix:** Replace all hardcoded `"00:00:00:00:00:00"` MAC address strings with the dynamic `appSetupPrefs.printerMac` preference value.

Verify that saving a MAC address in printer settings keeps it saved after closing the app, and that clicking "Print Receipt" after checkout triggers the Bluetooth connection with the correct MAC address.
```

---

<!-- slide -->

## 📋 Prompt 5: Core POS Features & Navigation Crash Fixes

```text
You are an expert Android developer. Your task is to resolve critical POS feature gaps, navigation crashes, and hardcoded items.

### Targets & Code Actions:
1. **Fix Missing QR Navigation Route**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/AppNavHost.kt`
   - **Issue [ID 20]:** Tapping the QR generator item causes a crash because the route `"qr/{barcodeId}"` is not registered in the Jetpack Compose `NavHost`.
   - **Fix:** Register the `"qr/{barcodeId}"` route in `AppNavHost.kt`, extracting the argument and launching `QrGeneratorScreen` with the correct navigate-back listener.

2. **Add Discount Input to Checkout Cart**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/ui/home/HomeScreen.kt`
     - `app/src/main/java/com/tillzo/pos/ui/home/PosViewModel.kt`
   - **Issue [ID 23]:** The discount fields exist in the backend (`SaleEntity.discount`, `PosViewModel.setDiscount()`) but are completely un-wired in the UI. Cashiers cannot apply discounts.
   - **Fix:** Add a button/row in the checkout cart summary card on `HomeScreen` (e.g. "Apply Discount"). Tapping it should show a dialog prompting for a discount amount and call `posViewModel.setDiscount(amount)`. Update the total summary calculations.

3. **Secure Admin PIN Gate**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/home/HomeScreen.kt`
   - **Issue [ID 21]:** Admin PIN check in `HomeScreen.kt:L866` is hardcoded to `1234`.
   - **Fix:** Read the stored passcode preference from settings. If not set, prompt to register one. Replace the hardcoded `"1234"` literal check with a dynamic preference check.

4. **Centralize Currency Symbol**:
   - **Issues [ID 22]:** INR symbol `₹` is hardcoded in `InventoryCrudViewModel.kt` and `BarcodePrintSettingsScreen.kt` while receipts use `Rs`.
   - **Fix:** Add a store-wide preference for the currency symbol (e.g. `currencySymbol` default `"Rs"`) in `AppSetupPrefs.kt`. Update all UI references to read this property rather than using hardcoded literals.

5. **Quick Tender Payment Buttons**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/pos/PaymentDialog.kt`
   - **Issue [ID 29]:** Quick exact-cash/tender buttons ($10, $20, $50) are missing, requiring manual digits entry.
   - **Fix:** Add standard bill buttons in the cash payment tab for quick exact change input.

Verify that navigating to the QR generator no longer crashes, discounts apply to the checkout total, and hardcoded literals are replaced with dynamic preferences.
```

---

<!-- slide -->

## 📋 Prompt 6: Till Reconciliation, Pagination, & Offline States

```text
You are an expert Android developer. Your task is to implement Till Reconciliation flows, history pagination, and offline states.

### Targets & Code Actions:
1. **Till Reconciliation (Expected Cash vs Physical Count)**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/ui/reports/ZReportScreen.kt`
     - `app/src/main/java/com/tillzo/pos/ui/reports/ZReportViewModel.kt`
     - `app/src/main/java/com/tillzo/pos/data/local/entity/TillSessionEntity.kt`
   - **Issues [IDs 24, 25]:** 
     - Z-Report Close Day flow automatically sets `closingCash = expectedCash` and `netCash = 0.0`. Cashiers cannot enter counted cash, hiding shortages or surpluses.
     - Sales can run without an open till session.
   - **Fix:** Update the Close Day flow to display a Dialog asking the cashier to input the actual physical cash counted in the drawer. Calculate the variance (`physicalCash - expectedCash`). Save the physical count and variance into the `TillSessionEntity`, mark the session as `RECONCILED`/`CLOSED`, and block further sales until a new till session is opened.
   - Inject a global middleware or check inside `HomeScreen` that displays a locking overlay if no till session is active, requiring an opening balance float input.

2. **Add Pagination to Sale History**:
   - **Files:** 
     - `app/src/main/java/com/tillzo/pos/data/local/dao/SaleDao.kt`
     - `app/src/main/java/com/tillzo/pos/ui/history/HistoryViewModel.kt`
   - **Issue [ID 26]:** `getAllSales()` returns a Flow of all sales without limit. In production with thousands of sales, this causes Out-Of-Memory (OOM) failures.
   - **Fix:** Implement query parameters `limit` and `offset` in `SaleDao.getAllSales()` and implement simple scroll pagination or offset loading in the `HistoryViewModel` and list UI.

3. **Cloud-Reprint Fallback**:
   - **Files:** `ReprintReceiptUseCase.kt` (New), `HistoryScreen.kt`
   - **Issue [ID 27]:** Synced sales cleared from local Room cache cannot be reprinted since they are no longer in DB.
   - **Fix:** Build `ReprintReceiptUseCase.kt` that checks Room. If null, queries the Google Sheets API via invoice ID, converts the matching sheet row back into a `SaleEntity`, and returns it for print formatting.

4. **Offline State Indicator**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/home/HomeScreen.kt`
   - **Issue [ID 28]:** Cashier does not know at a glance if they are offline or synced.
   - **Fix:** Add a status indicator (Green/Red icon) in the header showing whether Room database has pending updates.

5. **Hardware Diagnostic Screen**:
   - **File:** `app/src/main/java/com/tillzo/pos/ui/hardware/HardwareDiagnosticScreen.kt` (New)
   - **Issue [ID 30]:** No diagnostic UI to test scanner/printer APIs.
   - **Fix:** Create a diagnostics panel allowing connection test/pings to thermal printers.

Verify that closing the day prompts for counted cash, sale history loads efficiently in pages, and offline sync state is visible.
```

---

<!-- slide -->

## 📋 Prompt 7: Codebase Pruning & ERP Bloat Removal

```text
You are an expert Android developer. Your task is to simplify the database schemas, UI screens, and background jobs of TillzoPOS to align with a single-user, single-store POS profile.

### Targets & Code Actions:
1. **Reduce Vendor Profile Fields**:
   - **File:** `app/src/main/java/com/tillzo/pos/data/local/entity/PurchaseOrderEntity.kt` (or wherever `VendorEntity` is defined)
   - **Issue [ID 31, 33]:** 
     - `VendorEntity` has 56 columns (escalations L1/L2/L3, IBAN/SWIFT, compliance certificates, warranty terms, contract files on Google Drive). This is enterprise bloat.
   - **Fix:** Refactor `VendorEntity` to remove excessive columns. Strip out L1/L2/L3 escalation contacts (9 fields), primary manager, tech support, compliance certificates, bank details (IBAN, SWIFT, branch), and contract URLs on Google Drive. 
   - Keep only: `vendorId`, `name`, `phone`, `whatsapp`, `email`, `address`, `city`, `creditLimit`, `isActive`, `isDeleted`, `syncStatus`, `createdAt`, `updatedAt`.
   - Update `VendorEntity.toSyncMap()`, `SheetColumns.kt` and sheets schema mappings to reflect this simplified structure. Delete Drive integration references from upload use cases.

2. **Simplify Barcode Layout Configuration**:
   - **Files:** `BarcodeGeneralConfigEntity.kt`, `BarcodeFieldConfigEntity.kt`, `app/src/main/java/com/tillzo/pos/data/local/db/AppDatabase.kt`
   - **Issue [ID 32]:** Barcode config entities have 40+ granular positioning parameters (titleX, titleY, priceX, etc.).
   - **Fix:** Replace these complex configuration tables. Create a simplified entity or write layout settings as a simple JSON string to standard SharedPreferences. Delete the database tables and write a Room migration to drop these tables cleanly.

3. **Remove ShardingWorker & DisasterWorker**:
   - **Files:** `SyncWorker.kt`, `DisasterWorker.kt`, `ShardingWorker.kt` (and WorkManager initialization files)
   - **Issue [ID 34]:** Monthly spreadsheet sharding and nightly disaster backup clones are enterprise-level optimizations that add latency and consume API quotas.
   - **Fix:** Delete `ShardingWorker.kt` and `DisasterWorker.kt`. Clean up their registration, scheduling, and references inside `SyncWorker.kt` and `SyncOrchestrator.kt`. Rely on a single daily or real-time backup check.

4. **Bypass Role-Based Access Control (RBAC)**:
   - **Files:** `UserManagementScreen.kt`, `UserManagementViewModel.kt`, `UserDao.kt`, `SessionGuardUseCase.kt`
   - **Issue [ID 35]:** The app implements multi-user roles (Admin, Manager, Cashier) and permission gates.
   - **Fix:** Delete the `UserManagementScreen` and `PermissionManagerScreen`. Simplify authentication to assume an authorized administrator session by default. Remove permissions check logic from view models and composable navigation screens.

5. **Simplify Purchase Order Workflow**:
   - **Files:** `CreatePurchaseOrderScreen.kt`, `PODetailScreen.kt`, `PurchaseOrderDao.kt`
   - **Issue [ID 36, 37]:** Multi-step PO drafting and empty deletion processes add complexity.
   - **Fix:** Condense POs/GRNs. Provide a simple restocking log screen, bypass the multi-stage PO approvals, and clean up empty deletion hooks in `SalesUploadUseCase.kt` and `KhataEventUseCase.kt`.

Rebuild the project, ensure all Room migrations compile correctly, and verify the database initializes successfully.
```

---

<!-- slide -->

## 📋 Prompt 8: Rolling Local Logger (CCTV Style) with App Crashes, UI Clicks, and Sync Tracking

```text
You are an expert Android developer. Your task is to implement a robust, offline-only, real-time Rolling Logging System (CCTV style) inside TillzoPOS.

### Requirements:
1. **Local-Only Rolling Retention**:
   - Save all logs locally in Room database on the device's storage. Do not upload logs to the cloud.
   - Implement automatic circular buffer deletion where logs older than 48 hours (2 days) are deleted automatically during app startup and at the end of every sync cycle.

2. **Complete Event Capturing**:
   - **UI Interactions & Click Logs:** Log every button click event (e.g. cash checkout, print receipt, scan barcode, apply discount) including timestamps and button identifiers.
   - **Google Sheets Sync Actions:** Log sync start/stop status, table name, response HTTP code, sync successes, failures, and network retry errors.
   - **App Crash & Fatal Errors:** Log uncaught application crashes with complete stack traces, exception messages, and device details.

### Implementation Steps:
1. **Create the Database Schema (`AppLogEntity`)**:
   - Define a Room entity `AppLogEntity` with fields:
     - `logId` (Long, Autogenerated Primary Key)
     - `timestamp` (Long)
     - `tag` (String, e.g., "UI_CLICK", "SYNC_PROCESS", "APP_CRASH", "DATABASE")
     - `logLevel` (String, e.g., "INFO", "WARN", "ERROR", "FATAL")
     - `message` (String)
   - Add the entity to `AppDatabase.kt` and provide an explicit Room database migration.
   - Create `LogDao` containing `insertLog()`, `getAllLogs()` returning a Flow, and `deleteLogsOlderThan(cutoffTime: Long)`.

2. **Implement the Local Logger Helper**:
   - Create `AppLogger` utility (or class injected via Hilt) that exposes `logInfo(tag, msg)`, `logWarn(tag, msg)`, and `logError(tag, msg, throwable?)`.
   - Ensure it executes inserts asynchronously on a coroutine utilizing `Dispatchers.IO`.

3. **Register Uncaught Exception Crash Handler**:
   - In the application's base initialization class (`TillzoApplication.kt`), create a custom `Thread.UncaughtExceptionHandler`.
   - When an unhandled crash occurs, capture the stack trace string, execute a blocking database query to insert a `FATAL` level log entry into `LogDao` (blocking is required since the OS thread is terminating), and then delegate execution to Android's default exception handler to finalize the crash behavior.

4. **Hook Clicks & Sync Lifecycles**:
   - In key UI composables (`HomeScreen.kt`, `CheckoutScreen.kt`, `ReceiptScreen.kt`), log tap triggers through `AppLogger`.
   - In `SyncWorker.kt` and `SheetsRepository.kt`, log starting sync, HTTP results, exceptions, and successes.

5. **Rolling Retention Cleanup Trigger**:
   - Inside `TillzoApplication.onCreate()` and at the completion block of `SyncWorker.doWork()`, run the rolling cleanup query to delete logs older than 2 days:
     ```kotlin
     val limitTime = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
     logDao.deleteLogsOlderThan(limitTime)
     ```

6. **Log Viewer Settings UI Panel**:
   - Under Settings, create a "System Logs" screen.
   - Add a paginated, scrollable list showing logs sorted by timestamp DESC.
   - Add search and drop-down filters for levels (Info, Warn, Error, Fatal) and tags.
   - Add an "Export Logs" button to save the entire 48-hour log buffer as a `.txt` file into the device's local standard `Downloads` directory.

Compile the application, verify that clicks and sync events write to the log table, throw a mock runtime exception to verify the crash trace saves to the database, and verify logs older than 2 days are cleaned up.
```

---

<!-- slide -->

## 📋 Prompt 9: Log Viewer UI Contrast, Expandable Logs, & Exception Details

```text
You are an expert Android developer. Your task is to fix the text color contrast and details accessibility issues in the TillzoPOS Log Viewer UI screen.

### Background:
Currently, the log entries display in a card layout where ERROR and FATAL logs use a light pink background (Color(0xFFFFF0F0)). However, the text inside the card uses dynamic M3 theme colors (like MaterialTheme.colorScheme.onSurfaceVariant or onSurface). In Dark Mode, these dynamic colors resolve to light gray/white, creating a low-contrast "white-on-white" text bug that makes error logs completely unreadable. In addition, long exception stack traces and full log messages are truncated with maxLines = 5 and ellipsis, with no way for the operator to view the full details.

### Targets & Code Actions:
1. **Fix Color Contrast of Log Cards**:
   - **File:** app/src/main/java/com/tillzo/pos/ui/settings/options/logviewer/SystemLogsScreen.kt
   - **Fix:** In LogEntryCard, replace the hardcoded background colors and dynamic text colors with proper contrasting combinations:
     - For ERROR and FATAL logs, use MaterialTheme.colorScheme.errorContainer as the card's background color, and MaterialTheme.colorScheme.onErrorContainer for the text colors of the tag, level, timestamp, and message.
     - Alternatively, if using custom light/dark colors, ensure the text colors are explicitly hardcoded (e.g. Color(0xFF333333) or Color.Black on a light background) so they do not invert in dark mode.
     - For standard logs (INFO, WARN, etc.), use MaterialTheme.colorScheme.surfaceVariant for the container and MaterialTheme.colorScheme.onSurfaceVariant / onSurface for the text.

2. **Add Expandable Log Details**:
   - **File:** app/src/main/java/com/tillzo/pos/ui/settings/options/logviewer/SystemLogsScreen.kt
   - **Fix:** Allow the user to view the full, non-truncated message and stack trace.
     - Add a click action (Modifier.clickable) to the Card in LogEntryCard.
     - Maintain an in-memory expanded boolean state for each log item. When clicked, toggle the expanded state.
     - If expanded is true, display the log message text with maxLines = Int.MAX_VALUE (no truncation) and render a button to "Copy Log to Clipboard".
     - Alternatively, open a scrollable AlertDialog when a card is clicked, showing the complete message/stack trace, timestamp, tag, and a copy button.

Verify that in both Light and Dark mode, the log text is highly readable with high contrast, and tapping any log entry successfully displays the full exception stack trace or detailed message.
```

---

<!-- slide -->

## 📋 Prompt 10: Bubble Up Sync Errors & Resolve http_code=-1

```text
You are an expert Android developer. Your task is to resolve the Google Sheets synchronization failure (specifically on the Till_Sessions table) and fix the logging pipeline so that real network/HTTP errors are bubbled up instead of swallowing them as a hardcoded -1 code.

### Background:
The logs show Upload failed: Till_Sessions, http_code=-1. This is caused by two compounding issues:
1. The "Till_Sessions" table definition and header columns are completely missing from the provisioning mapping in SheetsRepository.kt and Constants.kt. Because the sheet tab was never created during app setup, the Google Sheets API returns a 400 Bad Request range error when trying to write to it.
2. SheetsRemoteDataSource.appendRows() catches any retrofit exception or error response and returns a simple false boolean. The calling repository SheetsRepository.kt then logs a hardcoded "http_code=-1" error string, hiding the actual HTTP status code and exception details.

### Targets & Code Actions:
1. **Define Till Sessions Schema Columns**:
   - **File:** app/src/main/java/com/tillzo/pos/utils/Constants.kt
   - **Fix:** Inside the SheetColumns object, define the column headers list for till sessions:
     ```kotlin
     val TILL_SESSIONS = listOf(
         "session_id", "cashier_id", "cashier_name", "pos_terminal_id",
         "opening_cash", "closing_cash", "expected_cash", "total_cash_sales",
         "total_card_sales", "total_wallet_sales", "total_udhaar_sales",
         "total_sales_count", "total_refunds", "net_cash", "status",
         "notes", "shift_date", "opened_at", "closed_at", "sync_status",
         "created_at", "updated_at"
     )
     ```

2. **Add Header Provisioning & Mapping**:
   - **File:** app/src/main/java/com/tillzo/pos/data/repository/SheetsRepository.kt
   - **Fix:** In buildHeaders(), register the "Till_Sessions" sheet name and map it to com.tillzo.pos.utils.SheetColumns.TILL_SESSIONS. This ensures the sheet is dynamically created in Google Sheets during the startup verification loop.

3. **Bubble Up Real Network / HTTP Errors**:
   - **File:** app/src/main/java/com/tillzo/pos/data/remote/SheetsRemoteDataSource.kt
   - **Fix:** Refactor appendRows() and readRange() to return more descriptive results or the raw Retrofit Response<Map<String, Any>> object instead of a simple boolean. 
     - If the response is not successful, capture resp.code() and the error body: resp.errorBody()?.string().
     - If an exception (e.g. UnknownHostException, SocketTimeoutException) is thrown, catch it and preserve the exception message.
   - **File:** app/src/main/java/com/tillzo/pos/data/repository/SheetsRepository.kt
   - **Fix:** In uploadBatch(), extract the real HTTP status code and error messages from the data source response. Replace the hardcoded http_code=-1 log message with dynamic logs:
     - E.g., appLogger.logError("SYNC_PROCESS", "Upload failed: $tableName, http_code=${response.code()}, error=${response.errorBody()?.string()}")
     - If a connection exception was thrown: appLogger.logError("SYNC_PROCESS", "Upload failed: $tableName, exception=${e.message}")

Verify that running sync now correctly attempts to create the "Till_Sessions" sheet, and if any other upload fails, it logs the real HTTP code (e.g. 400, 429, 401) and error details instead of -1.
```

---

<!-- slide -->

## 📋 Prompt 11: Auto-Healing Missing Sheets Tab Creator (SchemaGuard Integration)

```text
You are an expert Android developer. Your task is to update the auto-healing database checker (SchemaGuard) inside TillzoPOS so that it automatically detects missing sheets (such as "Till_Sessions") and generates them with their correct column headers on app startup and sync.

### Background:
During sync operations, Google Sheets API returned a 400 Bad Request error with message "Unable to parse range: Till_Sessions". This occurs because the sheet tab "Till_Sessions" does not exist in the spreadsheet. While the app has SchemaGuardUseCase.kt to check and self-heal missing database tabs, it does not currently have "Till_Sessions" in its checklist of required sheets and header structures.

### Targets & Code Actions:
1. **Register Till Sessions in SchemaGuard Checklist**:
   - **File:** app/src/main/java/com/tillzo/pos/domain/sync/usecase/SchemaGuardUseCase.kt
   - **Fix:** Update the list of requiredTabs inside the invoke method to include "Till_Sessions":
     ```kotlin
     val requiredTabs = listOf(
         "Inventory", "Customers", "Khata_Events",
         "Expenses", "Returns", "Wastage_Ledger", "Users_Permissions",
         "Settings", "Sync_Log", "Dashboard", "SYS_DB_DO_NOT_TOUCH",
         "Purchase_Orders", "PO_Items", "GRN_Headers", "GRN_Items", "Vendors",
         "Product_Batches", "Product_Units", "Till_Sessions" // <-- Add here
     )
     ```
   - **Fix:** Register "Till_Sessions" in the TAB_HEADERS companion map:
     ```kotlin
     "Till_Sessions" to SheetColumns.TILL_SESSIONS
     ```

Verify that running the application or triggering a background synchronization loop checks if the "Till_Sessions" tab exists. If missing, it should log: "Missing required tab: Till_Sessions" and then immediately execute dataSource.addSheet("Till_Sessions") and populate it with the headers from SheetColumns.TILL_SESSIONS (such as session_id, cashier_id, etc.), resolving the 400 bad argument parse range error.
```


