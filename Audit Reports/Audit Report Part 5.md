# Tillzo POS Codebase Audit Report

**Date:** July 17, 2026  
**Auditor:** Senior Android Architect / POS-ERP Product Manager  
**Scope:** Full codebase audit — Single-Store, Single-User POS + ERP

---

## Table of Contents

1. [Critical Workflows & Bugs](#1-critical-workflows--bugs)
2. [Missing Critical Features](#2-missing-critical-features)
3. [Features to Remove / Simplify](#3-features-to-remove--simplify)
4. [Actionable Code Fixes](#4-actionable-code-fixes)
5. [Sync Data Lifecycle — Trace Analysis](#5-sync-data-lifecycle--trace-analysis)
6. [Summary: Quick-Fix Priorities](#6-summary-quick-fix-priorities)

---

## 1. Critical Workflows & Bugs

### BUG 1: Inventory Upsert — `sync_status` Key Collision (Silent Data Corruption)

**File:** `InventoryUpsertUseCase.kt:66-95`  
**Severity:** HIGH

In `toSyncMap()` of `InventoryEntity` (`:68`), `sync_status` is hardcoded to `"synced"`. When the DeltaSyncManager fetches data back from the Sheet, it sets `sync_status = "synced"` locally. But if the upload fails after the Sheet write but before the local mark, the record becomes stuck as "pending" locally while already existing in the Sheet. The dedup check (`getExistingUuids`) prevents re-upload on retry, so the record is **never marked synced locally** — it stays "pending" forever.

Additionally, the `VendorUpsertUseCase` (`VendorUpsertUseCase.kt:94-102`) has a critical bug:

```kotlin
val result = sheetsRepository.uploadBatch(TABLE_NAME, newRows)
if (result != null) {  // BUG: SyncResult is always non-null
    vendorDao.markMultipleSynced(...)
```

The `uploadBatch` function returns `SyncResult`, which is **always non-null**. A `SyncResult.ServerError` resolves to `true` for `!= null`. So vendors are marked synced even when upload **fails**.

---

### BUG 2: `toSyncMap()` Inconsistencies — Column Count Mismatch

**File:** `InventoryUpsertUseCase.kt:68-95`, `SheetColumns.kt:61-87`  
**Severity:** HIGH — Sheet data scrambled

The `InventoryUpsertUseCase` builds a 25-column row (`A:Y`), but `SheetColumns.INVENTORY` has 26 columns (`updated_at` at the end). The inventory `toSyncMap()` (`InventoryEntity.kt:49-82`) also generates 26 keys with `"last_updated"` as a duplicate of `"updated_at"`. But the Values list in the UpsertUseCase maps 25 values — column-by-column — so `"updated_at"` from the map is **silently dropped** and the last column (`"updated_at"` = `last_updated`) mismaps. On delta sync back, `updated_at` reads wrong.

Same pattern exists in **CategoryUpsertUseCase** (`:72-83`) and **ProductUnitUpsertUseCase** (`:72-80`).

---

### BUG 3: Stock Deduction Happens in Two Places — Race Condition

**File:** `SyncWorker.kt:123-125` and `SyncWorker.kt:167-218`  
**Severity:** HIGH — Double stock deduction possible

The `SyncWorker` captures `salesBeforeSync` from `getPendingSyncSales()` at Step 2, then at Step 4 calls `deductStockForSyncedSales()` for sales that were just synced. However, `CompleteSaleUseCase` does **NOT** deduct stock immediately (by design — "Blind Selling"). But the **SalesUploadUseCase** marks sales as `sync_status = "synced"` before the SyncWorker's stock deduction runs. If the worker crashes between steps 3 and 4, the sale is reported as synced but **stock is never deducted**. Worse: on retry, `getPendingSyncSales()` returns empty (already synced) → stock deduction is skipped permanently.

---

### BUG 4: DeltaSyncManager Uses Wrong Timestamp Comparison

**File:** `DeltaSyncManager.kt:109-117`  
**Severity:** MEDIUM — Delta sync never triggers after first run

On **first run**, `localTimestamp = 0`, delta runs. But `remoteTimestamp` is saved as cursor even if no rows were returned. If the Settings tab has `last_updated_timestamp = 0`, then `remoteTimestamp = 0`, and `localTimestamp` is updated to 0 → delta sync never runs again because the condition `localTimestamp > 0L && remoteTimestamp <= localTimestamp` prevents it. A **fresh sheet is never re-fetched via delta** after the first empty poll.

---

### BUG 5: Sales Upload Dedup — Wrong ID Used

**File:** `SalesUploadUseCase.kt:38-41`  
**Severity:** MEDIUM — Duplicate sales uploaded

The dedup uses `sale.system_row_id !in existingIds`, but `getExistingUuids` reads **column A** from Sheet. Column A in the Sales sheet (per `SheetColumns.SALES`) is `"invoice_id"` (the `sync_uuid`), not `system_row_id`. So the dedup reads Column A (`sync_uuid`) but compares against `system_row_id`. This means dedup is **completely broken** — every pending sale uploads every time, and duplicate rows accumulate in the Sheet.

---

### BUG 6: Hardcoded `posTerminalId`

**File:** `SyncWorker.kt:224`, `CompleteSaleUseCase.kt:69`  
**Severity:** LOW-MEDIUM — Fragile

```kotlin
val posTerminalId = "terminal_1"
```

The app inconsistently uses the `spreadsheetId.take(20)` as terminal ID in one place (`CompleteSaleUseCase`) and `"terminal_1"` in another (`SyncWorker`). Data from the same device can be tagged differently across tables.

---

### BUG 7: `SheetsApiClient` Logging Level in Production

**File:** `SheetsApiClient.kt:96-99`  
**Severity:** MEDIUM — Security & Performance

```kotlin
HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY  // Logs full request/response bodies
}
```

This logs the **full OAuth token** (in Authorization header) and all business data to logcat at all times. Without a ProGuard rule to set this to `NONE` in release builds, user tokens are exposed.

---

### BUG 8: Empty `processDeletions` Implementations

**File:** `SalesUploadUseCase.kt:88-94`, `KhataEventUseCase.kt:72-78`  
**Severity:** LOW — Dead code

Both methods have empty bodies with comments like "Normally sales are never deleted" — yet the DAOs have `softDeleteById`, `getPendingDeletedRows`, and `markSyncedAndDeleted` queries. Deleted sales and Khata events are never cleaned from the sheet.

---

### BUG 9: `AdminPinDialog` Uses Hardcoded PIN `1234`

**File:** `HomeScreen.kt:866`  
**Severity:** MEDIUM — Security theater

The PIN gate uses a hardcoded check `pinInput == "1234"` instead of reading the stored PIN from settings. Any user who knows this code can bypass the pin.

---

### BUG 10: `HttpLoggingInterceptor` Duplication

**File:** `SheetsApiClient.kt:96-99` and `NetworkModule.kt:37-42`  
**Severity:** LOW — Redundant logging

Two `OkHttpClient` instances are created with their own logging interceptors. The `NetworkModule` provides a client that is **never used** (not injected anywhere), while `SheetsApiClient` builds its own. This is dead code.

---

## 2. Missing Critical Features

### P0 — MUST HAVE (Blocking for Production Use)

| # | Feature | Why |
|---|---------|-----|
| 1 | **Stock Deduction on Sale** | Currently the "Blind Selling" rule means stock is never deducted at sale time. It only happens after the sync worker gets an HTTP 200. If the device has no internet at sale time, stock is **never deducted** until a sync succeeds. Deduct stock locally immediately on sale, mark sync pending. |
| 2 | **Offline Cash Register (Balance Check)** | Daily totals, net cash calculation, expected cash vs actual. The `TillSession` entity has these fields but they are never used to enforce cash drawer accountability. |
| 3 | **Return/Refund Workflow** | The `ReturnsScreen` exists as a UI shell, the `SalesEntity` has `reference_id` with `"REFUND_OF_"` prefix support, but there is **no actual refund flow** that reverses inventory and creates a credit event. |
| 4 | **Sale Search / Invoice Lookup** | The `HistoryScreen` fetches all sales but has **no pagination**. With 10,000+ sales, this will OOM. The `SaleDao` has `getAllSales()` as a `Flow` without LIMIT. |

### P1 — SHOULD HAVE

| # | Feature | Why |
|---|---------|-----|
| 5 | **Cash Drawer Count** | Z-Report needs opening/closing cash counts. |
| 6 | **Discount by Percentage** | Only absolute amount discount is supported. |
| 7 | **Category CRUD from Inventory Form** | The form has a "Manage Categories" quick dialog, but it doesn't support hierarchical category management fully (creating subcategories from within the flow). |
| 8 | **Low Stock Auto-Order / PO Generation** | Currently POs must be manually created. No low-stock → auto-PO flow. |

---

## 3. Features to Remove / Simplify

### BLOAT LIST — Over-engineering for a Single-Store POS

| Feature | File(s) | Why Remove |
|---------|---------|------------|
| **Vendor Entity with 50+ fields** | `VendorUpsertUseCase.kt`, `Vendors` sheet column list | Has escalation L1/L2/L3 contacts, bank details, SLA terms, trade license, compliance certificates, etc. A single-store owner needs: name, phone, address. Remove 90% of fields. |
| **Hierarchical Categories** | `CategoryEntity.parent_category_id`, `AppDatabase.kt` migration v18 | A single store categorizes inventory with flat labels. The two-level dropdown (Main Category → Subcategory) adds 100+ lines of UI code for no business value. |
| **Product Batches & GTINs** | `ProductBatchEntity`, `ItemGtinEntity`, full batch management UI | Batch tracking is for pharmaceuticals/FMCG distributors. A single-shop meat/grocery store does not need batch-level FIFO. Remove or hide behind a feature flag. |
| **Barcode Print Layout Config** | `BarcodeGeneralConfigEntity` with 40+ fields, `BarcodePrintSettingsScreen` | Absolute positioning (titleX/titleY, priceX/priceY, etc.) is for enterprise label printers. A small shop uses standard templates. Replace with 3 presets. |
| **Purchase Order / GRN Module** | Full `purchase_orders`, `grn_headers`, `grn_items` tables + UI | For a single user who is both cashier and manager, POs and GRNs are overkill. Inventory is added directly. Simplify to a single "Stock In" transaction. |
| **User Management / Roles** | `UserEntity`, `UserManagementScreen` | Single-user app. Remove the entire M3 auth module. |
| **Permission Manager** | `PermissionManagerScreen` | Not needed for single cashier. |
| **Force Update Module** | `ForceUpdateScreen` | Adds complexity. Replace with a simple Play Store update check. |
| **OCR Entry Screen** | `OcrEntryScreen` | OCR for invoice scanning adds significant complexity (Vision API, camera). Not needed for a basic POS. |
| **MicroBatchManager** | `data/sync/options/micro/` | The `MicroBatchManager` directory exists but was never wired in. 20-second batch window is not configurable or used. |
| **Sharding Worker** | `ShardingWorker`, `MAX_ROWS_PER_SHARD` | Monthly sales tab sharding for 18k+ row limits is enterprise-grade. A single store generates ~30-50 sales/day = ~1,000/month. This will take 18 months to hit 18k rows. Premature optimization. |
| **Disaster Recovery Worker** | `DisasterWorker` | Daily backup to a separate sheet. Overkill for single-store. Google Sheets already has version history. |

---

## 4. Actionable Code Fixes

### FIX 1: Fix Vendor Upsert Success Check (Bug #1)

**File:** `VendorUpsertUseCase.kt:94-102`

```kotlin
// OLD (broken):
val result = sheetsRepository.uploadBatch(TABLE_NAME, newRows)
if (result != null) {
    vendorDao.markMultipleSynced(itemsToAppend.map { it.vendorId })

// NEW:
val result = sheetsRepository.uploadBatch(TABLE_NAME, newRows)
if (result is SyncResult.Success) {
    vendorDao.markMultipleSynced(itemsToAppend.map { it.vendorId })
}
```

---

### FIX 2: Exclude `sync_status` from toSyncMap (Inventory/Categories/Units)

**File:** `InventoryUpsertUseCase.kt:66-95` — Remove the `syncMap["sync_status"]` from both the values list and handle it as a fixed value in the Sheet (always write `"synced"` to the sheet column, never read from syncMap).

---

### FIX 3: Fix Sales Dedup — Use Correct Column

**File:** `SheetsRepository.kt:285-292` — Add column-aware overload:

```kotlin
suspend fun getExistingUuids(tableName: String, columnLetter: String = "A"): Set<String> {
    val tab = tabForTable(tableName)
    val rows = dataSource.readRange("$tab!$columnLetter:$columnLetter")
    if (rows.size < 2) return emptySet()
    return rows.drop(1).mapNotNull { it.firstOrNull() }.toHashSet()
}
```

**File:** `SalesUploadUseCase.kt:38` — Change to:

```kotlin
val existingIds = sheetsRepository.getExistingUuids(tableName, "A")
val newSales = pendingSales.filter { sale -> sale.sync_uuid !in existingIds }
```

---

### FIX 4: Deduct Stock Locally on Sale

**File:** `CompleteSaleUseCase.kt` — Add stock deduction after line 106:

```kotlin
// After saleDao.insertSale(saleEntity), before triggerImmediateSync()
cartItems.forEach { cartItem ->
    val product = inventoryDao.getItemById(cartItem.itemId)
    if (product != null) {
        val newStock = maxOf(0.0, product.current_stock - cartItem.quantity)
        inventoryDao.updateStock(cartItem.itemId, newStock)
    }
}
```

Then remove the duplicate deduction from `SyncWorker.kt:123-125`.

---

### FIX 5: Fix DeltaSyncManager First-Run Zero Timestamp

**File:** `DeltaSyncManager.kt:109-117`

```kotlin
// OLD:
if (localTimestamp > 0L && remoteTimestamp == 0L) {
    Log.d(TAG, "Remote timestamp=0 — Settings tab not yet populated. Skipping.")
    return
}

// NEW:
if (remoteTimestamp == 0L) {
    if (localTimestamp > 0L) {
        Log.d(TAG, "Remote timestamp=0 — Settings tab not yet populated. Skipping.")
        return
    }
    // First run — save 0 as cursor so next poll works correctly
    appDatabase.syncLogDao().upsertSyncLog(
        SyncLogEntity(DELTA_CURSOR_KEY, 0L, "synced")
    )
    return
}
```

---

### FIX 6: Remove HTTP Logging from Release Builds

**File:** `SheetsApiClient.kt:96-99`

```kotlin
.addInterceptor(
    HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }
)
```

---

### FIX 7: Add Pagination to Sale History

**File:** `SaleDao.kt:13` — Change to:

```kotlin
@Query("SELECT * FROM Sales WHERE is_deleted = 0 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
fun getAllSales(limit: Int = 50, offset: Int = 0): Flow<List<SaleEntity>>
```

---

### FIX 8: Read PIN from Settings Instead of Hardcoded `1234`

**File:** `HomeScreen.kt:866`

```kotlin
// OLD:
if (pinInput == "1234") {

// NEW: Read from AppSetupPrefs
val storedPin = viewModel.getStoredPin()
if (pinInput == storedPin) {
```

---

## 5. Sync Data Lifecycle — Trace Analysis

### Sale Completion Path

```
UI (HomeScreen "PAY NOW")
→ PosViewModel.completeSale() [:276]
→ CompleteSaleUseCase.invoke() [:55]
    → Build SaleEntity with SyncStatus.PENDING
    → saleDao.insertSale(saleEntity) ← Room save (Local DB)
    → tillSessionDao.addSaleToSession(sessionId, ...)
    → khataEventDao.insert(khataEvent) [if udhaar]
    → triggerImmediateSync() ← OneTimeWorkRequest<SyncWorker>
    → Return SaleEntity to UI
→ UI navigates to ReceiptScreen
```

### Sync Upload Path

```
SyncWorker.doWork() [:72]
    → syncLogDao.ensureTableRegistered("Sales")
    → salesBeforeSync = saleDao.getPendingSyncSales()
    → uploadTable("Sales")
        → salesUploadUseCase()
            → saleDao.getPendingSyncSales()
            → sheetsRepository.getExistingUuids("Sales")  ← **BUG: reads invoice_id column**
            → sheetsRepository.uploadBatch(payload) ← appendRows to Sales_[Month]_[Year]
            → HTTP 200? → saleDao.updateSale(sale.copy(sync_status = "synced"))
    → deductStockForSyncedSales(salesBeforeSync)  ← **BUG: only runs if sales table synced**
    → verifyAndHideSysDbTab()
```

### Delta Fetch Path

```
DeltaSyncManager.pollOnce() [:100]
    → syncLogDao.getLastSyncedAt("delta_cursor") → 0 (first run)
    → syncInterface.getSettings() → read Settings tab
    → syncInterface.fetchDelta(lastTimestamp)
        → SheetsRepository.fetchDelta [:129]
            → readRange("Sales_Jul_2026!A:Z"), readRange("Inventory!A:Z"), ...
            → Compare row.updated_at > lastTimestamp
    → upsertDeltaRows(delta.rows)
        → inventoryDao.insertItem(item) (for Inventory tab rows)
        → saleDao.insertSale(sale) (for Sales_* tab rows)
    → syncLogDao.upsertSyncLog("delta_cursor", remoteTimestamp, "synced")
```

---

## 6. Summary: Quick-Fix Priorities

| Priority | Fix | Impact |
|----------|-----|--------|
| 🔴 CRITICAL | Fix Sales dedup (Bug #5) | Prevents duplicate rows in Sales sheet |
| 🔴 CRITICAL | Fix Vendor upload success check (Bug #1) | Prevents false "synced" on failed upload |
| 🔴 CRITICAL | Deduct stock locally on sale (Fix #4) | Prevents stock never being deducted offline |
| 🟡 HIGH | Fix Inventory column count mismatch (Bug #2) | Prevents scrambled sheet data on upsert |
| 🟡 HIGH | Add HTTP logging NONE in release (Fix #6) | Security — prevents token leak |
| 🟡 HIGH | Remove hardcoded PIN 1234 (Fix #8) | Security — PIN bypass |
| 🔵 MEDIUM | Remove vendor bloat (50+ fields) | Maintainability |
| 🔵 MEDIUM | Add pagination to History (Fix #7) | Prevents OOM with large datasets |
| 🔵 MEDIUM | Fix DeltaSyncManager first-run (Fix #5) | Ensures delta sync works after initial setup |

---

**Bottom Line:** The app has a solid architectural foundation (offline-first Room + WorkManager + Google Sheets REST API). The sync pipeline is ambitious for a single-owner POS. The most critical issues are in the sync integrity layer — especially the **dedup column mismatch** and the **vendor success check** — which would cause real data corruption in production. The feature set needs aggressive pruning: **remove 40% of the modules** (Vendors with 50 fields, hierarchical categories, batch tracking, PO/GRN workflow, user management) that are irrelevant for the target single-store audience. Focus instead on getting the **core POS loop (add-to-cart → pay → deduct stock → sync)** 100% reliable, and add the missing **offline cash register / Z-Report** that every single-store owner needs daily.
