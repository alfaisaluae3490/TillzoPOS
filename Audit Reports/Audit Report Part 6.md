# TillzoPOS — Senior Architect & Product Audit
**Repo:** alfaisaluae3490/TillzoPOS (main, cloned 2026-07-17) · Kotlin/Jetpack Compose · Room (v23) · Google Sheets REST backend · 229 Kotlin files
**Target profile assumed throughout:** one owner, one till, acting as cashier + inventory manager.

> Note on method: this repo already contains three prior self-generated audits (`Project_Progress.txt`, `static_analysis_report.md`, `project_analysis_report_June_4_2026.md`), all dated March–June 2026. The codebase has moved substantially since (DB went from v14→v23). I did **not** trust those documents — I re-traced every claim in them against the current source and I'm reporting what is actually true today. Several previously-flagged bugs are now fixed (noted below); several are not; and I found new ones those reports missed.

---

## 0. Executive Summary

The architecture (Clean Architecture: `data/domain/ui`, offline-first Room → WorkManager → Google Sheets REST) is genuinely solid for a solo-dev POS and is **not** the problem. The problem is that this is no longer a "single-store POS" codebase — it has grown into an ERP-grade purchasing/vendor/GRN/batch system with fields (SLA response times, three tiers of vendor escalation contacts, IBAN/SWIFT banking) that no single-shop owner will ever fill in, while three things a cashier touches on **every single sale** are broken or dead: the "Print Receipt" button does nothing, the printer MAC/IP settings don't persist, and a manual stock correction gets silently erased by the next goods-received note. Fix those three before anything else — they're the difference between "usable in a real shop tomorrow" and "demo only."

---

## 1. Critical Workflows & Bugs

### 1.1 The core sale → sync → stock lifecycle (traced end-to-end)

```
HomeScreen (cashier taps checkout)
  → PosViewModel.completeSale()
      → CompleteSaleUseCase.invoke()
          1. INSERT SaleEntity into Room, sync_status = PENDING   [instant, offline-safe]
          2. Best-effort: record sale into open TillSessionEntity (non-fatal try/catch)
          3. If udhaarAmount > 0 → INSERT KhataEventEntity (PENDING)
          4. WorkManager.enqueueUniqueWork("POST_SALE_INSTANT_SYNC", REPLACE, SyncWorker)
  → SyncWorker.doWork()
      → SalesUploadUseCase: GET existing UUIDs from the Sales_<Month>_<Year> tab,
        filter out already-uploaded rows, POST append the rest
      → on HTTP 200 → mark sale sync_status = SYNCED in Room
      → ONLY THEN: deductStockForSyncedSales() walks the sold items' JSON and
        decrements InventoryEntity.current_stock (+ FIFO batch deduction)
```

This is a deliberate **"Blind Selling"** design: stock is *not* decremented at the point of sale, only after the sale has synced to Google Sheets. This is a real architectural decision, not an accident (it's commented as a rule in three places), but it has a concrete consequence worth being explicit about: **while the device is offline, the on-screen stock count for every sold item is stale** — a cashier ringing up the last 3 units of something with the shop offline will still see the pre-sale count and can oversell into negative stock the moment sync catches up (the deduction code does floor at 0, so the DB itself won't go negative, but the *displayed* count during the offline window is simply wrong). For a single cashier this is a manageable trade-off, but it should be a documented, intentional choice — not something discovered in production.

**Duplicate-write risk is low but not zero.** `CompleteSaleUseCase` enqueues sync with `ExistingWorkPolicy.REPLACE` on a unique work name. If a second sale completes while the first sync is still running, WorkManager cancels the in-flight worker. `SalesUploadUseCase` does protect against this correctly — it re-reads existing UUIDs from the sheet before every append, so a cancelled-and-retried run won't double-post. The narrower risk is `InventoryUpsertUseCase`, which does a batched `PUT` for existing rows without a UUID dedupe check on the update path (only appends are deduped) — a cancellation mid-batch-write can leave some inventory rows updated remotely and others not, and since sync_status is only flipped after the whole batch reports no failure, this self-heals on the next run. **Net: correct by design, but fragile — a queued (non-unique-named) work chain would remove this whole class of edge case for the cost of a slightly delayed sync.**

### 1.2 🔴 CRITICAL — Manual stock adjustments are silently reverted by the next GRN

**File:** `domain/usecase/inventory/ManualStockAdjustmentUseCase.kt`

```kotlin
val newStock = product.current_stock + quantityChange
val updatedProduct = product.copy(current_stock = newStock, ...)
inventoryRepository.updateItem(updatedProduct)   // ← only touches InventoryEntity
```

This writes directly to `InventoryEntity.current_stock` and never touches `ProductBatchEntity`. But `InventoryRepositoryImpl.recalculateTotalStock()` — called every time a GRN is confirmed (`ConfirmGrnUseCase.kt:104,145`) — **recomputes `current_stock` from scratch as the sum of active batch quantities**:

```kotlin
// InventoryRepositoryImpl.kt
override suspend fun recalculateTotalStock(productId: String) {
    val total = productBatchDao.getAllBatchesForProduct(productId)
        .filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
    inventoryDao.updateItem(product.copy(totalStock = total, current_stock = total, ...))
}
```

Sequence: owner does a physical stock count, finds 2 units broken, adjusts down by -2 (`current_stock` now correct). Two days later they receive a routine delivery of a *different* batch and confirm the GRN. `recalculateTotalStock` fires, sums the batches (which never knew about the -2), and **silently restores the 2 phantom units**. This is a genuine, current data-integrity bug — it corrupts inventory counts without any error, any log the owner would see, or any way to detect it happened short of noticing the number looks wrong.

**Fix:** the adjustment must move stock at the batch level, not the product level.

```kotlin
class ManualStockAdjustmentUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productBatchDao: ProductBatchDao,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(systemRowId: String, quantityChange: Double, reason: String, adjustedByUserId: String) {
        val product = inventoryRepository.getItemById(systemRowId) ?: return

        if (product.hasBatches) {
            // Apply the delta to the most recent active batch (or FIFO-deduct across
            // batches if quantityChange is negative and exceeds the newest batch).
            var remaining = -quantityChange // negative adjustment = stock leaving
            if (remaining > 0) {
                while (remaining > 0) {
                    val batch = productBatchDao.getOldestActiveBatch(systemRowId) ?: break
                    val deduct = minOf(remaining, batch.stockQty)
                    val newQty = batch.stockQty - deduct
                    val now = System.currentTimeMillis()
                    if (newQty <= 0.0) productBatchDao.deactivateBatch(batch.batchId, now)
                    else productBatchDao.updateBatchStock(batch.batchId, newQty, now)
                    remaining -= deduct
                }
            } else if (quantityChange > 0) {
                val newest = productBatchDao.getNewestActiveBatch(systemRowId)
                if (newest != null) {
                    productBatchDao.updateBatchStock(newest.batchId, newest.stockQty + quantityChange, System.currentTimeMillis())
                }
            }
            inventoryRepository.recalculateTotalStock(systemRowId) // now consistent
        } else {
            val newStock = product.current_stock + quantityChange
            inventoryRepository.updateItem(product.copy(current_stock = newStock, sync_status = "pending"))
        }

        stockAdjustmentRepository.insertStockAdjustment(
            StockAdjustmentEntity(productId = systemRowId,
                adjustmentType = if (quantityChange > 0) "RECEIVED" else "CORRECTION",
                quantityChanged = quantityChange, reason = reason, adjustedBy = adjustedByUserId)
        )
    }
}
```
(`getNewestActiveBatch` doesn't currently exist on `ProductBatchDao` — add a one-line `@Query` sorted by `createdAt DESC LIMIT 1`.)

### 1.3 🔴 CRITICAL — "Print Receipt" is a dead button on the actual checkout flow

**File:** `ui/home/ReceiptScreen.kt` (the screen shown after every completed sale)

```kotlin
OutlinedButton(
    onClick = {
        // Delegates to existing BT printer module (M5)
        // For now show a snackbar (printer integration is in M5)
    },
    ...
) { Text("Print Receipt") }
```

The `onClick` is empty. This is not a hypothetical edge case — printing the receipt is the single most frequent action in the app's core loop, happening once per sale. Meanwhile the printer drivers themselves are fully built and functional elsewhere (`EscPosPrinter`, `TsplPrinter` — Bluetooth socket + network TCP:9100, retry logic) and are wired up correctly on the **standalone printer test screen** and Z-Report screen. The receipt screen simply never calls them.

**Fix:**
```kotlin
OutlinedButton(
    onClick = {
        scope.launch {
            val mac = printerPrefs.getSavedMac()   // see 1.4 — currently doesn't exist either
            if (mac.isNullOrBlank()) {
                snackbarHostState.showSnackbar("No printer configured — set one up in Settings")
            } else {
                val ok = escPosPrinter.printViaBluetooth(mac, buildReceiptText(sale, invoiceId))
                snackbarHostState.showSnackbar(if (ok) "Printed" else "Print failed — check printer")
            }
        }
    },
    ...
) { Text("Print Receipt") }
```

### 1.4 🔴 CRITICAL — Printer MAC/IP settings are never saved; three screens print to a hardcoded fake address

**File:** `ui/hardware/printer/PrinterSettingsViewModel.kt`

```kotlin
class PrinterSettingsViewModel @Inject constructor() : ViewModel() {
    private val _ipAddress = MutableStateFlow("192.168.1.100")
    private val _macAddress = MutableStateFlow("00:11:22:33:44:55")
    // updateIpAddress()/updateMacAddress() only ever touch these in-memory StateFlows.
    // There is no save(), no AppSetupPrefs write, nothing persisted to disk.
```

Whatever the owner types into Settings evaporates on the next app restart. And separately, three other places don't even try to read it — they print to a **hardcoded placeholder MAC**:
- `HistoryViewModel.kt:98` → `"00:00:00:00:00:00" // Hardcoded test MAC`
- `QrGeneratorViewModel.kt:62` → same
- `ZReportViewModel.kt:154` → `tsplPrinter.printBarcodeLabel("00:00:00:00:00:00", ...)`

Net effect: physical printing does not work anywhere in the app today, on any real device, for any workflow (receipts, Z-Reports, or barcode labels), despite the driver code being complete.

**Fix — persist to `AppSetupPrefs` (already used everywhere else in the app for exactly this purpose) and read from it everywhere printing happens:**
```kotlin
// AppSetupPrefs.kt — add:
var printerMac: String
    get() = prefs.getString("printer_mac", "") ?: ""
    set(value) = prefs.edit().putString("printer_mac", value).apply()
var printerIp: String
    get() = prefs.getString("printer_ip", "192.168.1.100") ?: "192.168.1.100"
    set(value) = prefs.edit().putString("printer_ip", value).apply()

// PrinterSettingsViewModel.kt
@HiltViewModel
class PrinterSettingsViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {
    private val _macAddress = MutableStateFlow(appSetupPrefs.printerMac)
    val macAddress: StateFlow<String> = _macAddress
    fun updateMacAddress(mac: String) {
        _macAddress.value = mac
        appSetupPrefs.printerMac = mac   // persist immediately, no separate Save button needed
    }
    // same pattern for IP
}
```
Then replace all three `"00:00:00:00:00:00"` literals with `appSetupPrefs.printerMac`.

### 1.5 🟠 HIGH — Disaster-recovery backup silently does nothing

**File:** `data/sync/options/worker/DisasterWorker.kt`

The worker fetches a full data snapshot every night, resolves the backup spreadsheet ID from the URL the owner configured, logs `"Disaster backup framework ready — backupId=..., rows=..."`, and then:

```kotlin
// TODO: Write delta.rows to backupId via REST API
// This requires a SheetsRepository method that accepts a custom spreadsheetId
// Will be wired in M3 when SaleEntity exists and data volumes are real
Log.i(TAG, "M3+: Wire actual write call here via sheetsRepository.writeToCrossSheet(backupId, rows)")
Result.success()
```

It returns `Result.success()` even though **no data was ever written anywhere**. An owner who configures a backup sheet believes they have disaster recovery; they have a nightly no-op. This is worse than not having the feature at all, because it fails silently and confidently.

**Fix (minimum viable):** add a `writeToCrossSheet(spreadsheetId, rows)` method to `SheetsRepository` that reuses the existing `SheetsApiClient`/`SheetsRemoteDataSource` batch-write path against an arbitrary spreadsheet ID instead of the app's own, and call it from step 3 instead of just logging. Until that exists, the worker should return `Result.failure()` (or better, surface a persistent "Backup not yet implemented" warning in Settings) rather than reporting success.

### 1.6 🟡 MEDIUM — Sync-status bookkeeping is misleading for 3 tables

`SyncWorker.uploadTable()`'s `when` block only recognizes `Sales`, `Inventory`, `KhataEvents`, `Categories`, `Product_Units`. `Vendors`, `BarcodeGeneralConfigs`, and `BarcodeFieldConfigs` are registered via `syncLogDao.ensureTableRegistered(...)` and pulled into the generic `tables` loop, but hit the `else` branch:
```kotlin
else -> { Log.w(TAG, "Unknown table name for sync: $tableName"); true }
```
which trivially returns `true` and calls `syncLogDao.markTableSynced(...)` — regardless of whether the *actual* upload (done separately, correctly, via `uploadPendingVendors()` etc. earlier in `doWork()`) succeeded or failed. The real sync works fine; it's the `Sync_Log` bookkeeping for these three tables that's fictional. Low functional impact today, but if a future "last synced" indicator in Settings reads from `SyncLogDao`, it will lie for these three tables. **Fix:** remove `Vendors`/`BarcodeGeneralConfigs`/`BarcodeFieldConfigs` from `ensureTableRegistered` (they don't need the generic path since they already have dedicated upload functions), or add real cases for them in the `when`.

### 1.7 🟡 MEDIUM — `Returns` and `Wastage` sheet columns defined without upload wiring for Returns

Wastage now syncs correctly (`uploadPendingWastage()` exists and is called). **Returns does not** — `SheetColumns` defines a header row for a `Returns` tab, but there is no `ReturnEntity`/DAO/upload path anywhere in `SyncWorker`. Returns currently only affect local inventory and, indirectly, the Sales table (as a negative-total sale row per the March report's note) — there is no dedicated audit trail of returns synced to the cloud. Worth deciding deliberately: either build the entity+sync, or delete the dead `Returns` header definition so it doesn't imply a feature that doesn't exist.

---

## 2. Missing Critical Features (prioritized for a single-shop POS)

1. **Wire the printer settings + Print Receipt button** (see 1.3/1.4). Nothing else matters if the till can't print.
2. **Fix the stock-adjustment/batch desync** (see 1.2). Silent inventory corruption undermines the entire ERP side of the app.
3. **Real backup writes** (see 1.5), or clearly label the feature as unavailable so the owner doesn't rely on it.
4. **Wire the discount input in the UI.** The backend is fully built (`PosViewModel.setDiscount()`, `SaleEntity.discount`, receipt display), but `setDiscount()` is never called from anywhere in `HomeScreen` — grep confirms zero UI callers. A cashier has no way to actually apply a discount; the field will always be `0.0`. This is a one-field fix (add a tappable discount row/dialog in the cart summary that calls `viewModel.setDiscount(amount)`), high value for almost no engineering cost.
5. **Tax-inclusive vs. tax-exclusive pricing toggle.** Currently every product has a flat `tax_percent` that's always added on top at checkout (`PosViewModel`: `total * taxPercent / 100.0`). Many single-shop retailers price tax-inclusive (what's on the shelf sticker is what's charged). There's no per-store or per-product toggle for this — worth a single app-level setting (`AppSetupPrefs.taxMode`) rather than per-product complexity.
6. **Role-based UI enforcement.** `UserEntity.role` exists in the schema and is written/synced, but nothing in `ui/` gates on it (`grep ".role =="` across the whole `ui/` package returns zero hits). For the stated single-user audience this is genuinely low priority — see §3 for the recommendation to actually remove most of the multi-user surface rather than build out enforcement for it.
7. **Currency symbol is still hardcoded in a few places** (`"₹"` in `InventoryCrudViewModel.kt`, `BarcodePrintSettingsScreen.kt`, `BarcodeGeneralConfigEntity.kt`) while the receipt/cart path uses `"Rs"`. Six occurrences total — cheap fix, but it means a barcode label printed for a product will show a different currency symbol than the receipt for the same sale. Centralize into one `AppSetupPrefs.currencySymbol` and reference it everywhere instead of literals.
8. **A "restore from cloud backup" UI**, once #3 is real — currently there is no way to rebuild the local Room DB from the Sheets backend at all if a phone is lost/reset, even though the sync mechanism theoretically has everything needed to do a full pull.

---

## 3. Features to Remove / Simplify (bloat for this audience)

This is where the codebase most visibly outgrew its stated audience. For a **single owner acting as both cashier and inventory manager**, the following are enterprise-procurement-grade features nobody in that role will ever fill in completely, and they add real maintenance and UI surface area for no return:

- **`VendorEntity` has 50+ fields**, including: separate Primary Manager / Tech Support / Billing contact triples, **three tiers of escalation contacts (L1/L2/L3, each with name/phone/email — 9 fields)**, `slaResponseTimes`, `warrantyTerms`, `complianceCertificates`, full banking (`bankIban`, `bankSwiftCode`, `bankBranch`), and four separate tax-ID fields (`ntnNumber`, `cnicNumber`, `trnNumber`, `registrationNumber`) — a schema built for enterprise supplier-relationship management, not a corner shop's list of 3–10 suppliers it calls on the phone. **Recommendation:** collapse to `name, phone, whatsapp, address, city, notes, creditLimit` (~7 fields) for the primary UI; if the multi-contact/compliance data genuinely needs to exist for some users, put it behind an "Advanced" expandable section that's collapsed by default rather than a wall of form fields every time someone adds a supplier.
- **User/Role management (`UserEntity.role`, `PermissionManagerViewModel`, `UserManagementViewModel`)** — the stated scope is explicitly single-user/single-till. Multi-user permission management is real engineering surface (schema + sync + UI) serving a use case that's explicitly out of scope. **Recommendation:** either strip it down to "owner PIN vs. no PIN" (which is really all a single-person shop needs for privacy from customers), or keep the schema (cheap, already synced) but delete the User Management screen entirely rather than half-build role gating for it.
- **Multi-till session plumbing is fine as-is** — `posTerminalId` is consistently hardcoded to `"terminal_1"` throughout (`SyncWorker`, `CompleteSaleUseCase`, etc.), so despite the DB schema technically supporting multiple terminals, the app doesn't actually run multi-till today. This one is *not* bloat — it's already appropriately simple; just don't build out the unused multi-terminal ID plumbing further.
- **Purchase Order → GRN → Vendor 3-tier workflow** is heavier than most single-shop owners need (formal PO creation, "Save & Share" as a draft/sent PDF, separate GRN confirmation step). It's not wrong to have it, but consider whether "log what I bought and received" (a single GRN-style entry, skip the formal PO step) would cover 90% of real single-shop restocking without the extra screens — worth validating against how the intended owner actually restocks (phone call to supplier → goods arrive → log it) rather than assuming they issue formal purchase orders.

---

## 4. Code Quality Findings

- **Threading is generally correct.** `SyncWorker.doWork()` explicitly wraps in `Dispatchers.IO`; the previously-flagged `InlineCameraBox` main-thread-blocking `.get()` call (from the March static-analysis doc) **is already fixed** — it now correctly uses `suspendCancellableCoroutine` with the CameraX listener on `ContextCompat.getMainExecutor`.
- **Exception handling is inconsistent but mostly non-fatal-by-design.** Most sync use cases wrap operations in `try/catch` and return `Boolean` for retry — good pattern. A few genuinely empty catch blocks exist (`InlineCameraBox.kt:147,160`, `BarcodeScannerScreen.kt:101` — a haptic tone-generator call), all low-risk (cosmetic/audio feedback, safe to swallow), not a systemic issue.
- **Only 3 `TODO`/`FIXME` markers in the whole codebase** — the DisasterWorker one (§1.5) is the only one with real user-facing consequence; the other two (`UserManagementViewModel` terminal ID, `HomeViewModel` a stale checkout-navigation comment referencing an already-built flow) are cosmetic.
- **Room migrations are fully explicit** — v1→v23, no `fallbackToDestructiveMigration`, all entities registered, all DAOs Hilt-provided. This is good discipline for an offline-first app where losing local data would be catastrophic.
- **Sales upload has correct idempotency** (UUID dedupe against the remote sheet before every append) — this is the one place in the sync layer that most needs to be crash/retry-safe, and it is.
- **A real Google OAuth `client_secret_*.json` (installed-app type) is committed to the repo root.** It's an "installed" application credential (client_id + public endpoints, no `client_secret` string inside it, so the blast radius is smaller than a web-app secret), but committing any OAuth client file is bad hygiene — add it to `.gitignore` and rotate/regenerate the client via Google Cloud Console if this repo has ever been public, since anyone who can read the repo can now attempt an OAuth flow impersonating this app's identity.

---

## Summary Table

| # | Item | Severity | Status |
|---|---|---|---|
| 1.2 | Manual stock adjustment reverted by next GRN | 🔴 Critical | **Confirmed present** |
| 1.3 | "Print Receipt" button is a no-op | 🔴 Critical | **Confirmed present** |
| 1.4 | Printer settings never persist; hardcoded fake MAC in 3 places | 🔴 Critical | **Confirmed present** |
| 1.5 | DisasterWorker backup is a stub that reports success | 🟠 High | **Confirmed present** |
| 1.6 | Sync-log status fabricated for 3 tables | 🟡 Medium | **Confirmed present** |
| 1.7 | Returns not synced to cloud (Wastage is fine) | 🟡 Medium | **Confirmed present** |
| — | Discount field has no UI input | 🟠 High (feature gap) | **Confirmed dead code** |
| — | Committed OAuth client file | 🟡 Medium (hygiene) | **Confirmed present** |
| — | Phantom initial-stock batch bug (March report) | — | ✅ **Already fixed** |
| — | Inverted Udhaar/Jama ledger signs (March report) | — | ✅ **Already fixed** |
| — | Camera ANR on main thread (March report) | — | ✅ **Already fixed** |
| — | Till-session lock on POS screen (March report: missing) | — | ✅ **Already fixed** |

I can generate the exact `git diff`-ready patches for §1.2–1.4 if you want them as ready-to-apply commits rather than snippets — let me know which one to start with.
