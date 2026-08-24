
## DEF-35 — Concurrent SyncWorker double-append (HIGH) — FIXED ✅
- **Code:** `data/sync/options/worker/SyncWorker.kt` — periodic + manual Force Sync run CONCURRENTLY (different unique work names), both read same pending rows, both append → duplicate sheet rows (observed: HERMES-VENDOR-001 appended twice, identical vendor_id/created_at).
- **FIX:** process-wide single-flight guard (synchronized(SyncWorker::class.java) + @Volatile syncInProgress) — second worker skips. Build+install+verified: logcat "Another SyncWorker is already running — skipping this run".

## DEF-36 — Sales header row DELETED (data loss) (HIGH) — FIXED ✅
- **Code:** `domain/sync/usecase/SchemaGuardUseCase.kt` — Sales_[MMM_YYYY] tabs DYNAMIC, never in TAB_HEADERS → header check skipped entirely. Header row got deleted (rows shifted up, sale 1 landed row 1) → pulls imported corrupt rows (empty invoice_id, $0.00) → 5 sales "missing" from history.
- **FIX:** (1) SchemaGuard resolves current Sales tab + includes in header check; (2) insertRowsTop() API — if row 1 looks like DATA (UUID), INSERT empty row first, then write headers (never overwrite data); (3) delta import skips blank sync_uuid rows; (4) deleteCorruptSales() DAO purges ghost rows on every poll. Verified: header restored (invoice_id|pos_id row 1), history 8/8 clean.

## DEF-37 — Settings last_updated_timestamp reader/writer disagreement → delta poll dead (HIGH) — FIXED ✅
- **Code:** `data/repository/SheetsRepository.kt` getSettings() loop started at i=1 (skipped row 1 = where writer updates) + duplicate timestamp rows (append legacy) → reader ALWAYS saw stale value → poll ALWAYS "No remote updates" → multi-device sync silently dead.
- **FIX:** read from row 0 + take MAX across duplicate keys. Verified: logcat "Remote updates detected (remote=X, local=Y)" + delta fetch executes.

## DEF-38 — SchemaGuard repairs on API error (429/5xx empty read) (MED) — FIXED ✅
- **Code:** `SchemaGuardUseCase.kt` — HTTP 429 returns empty rows → needsRepair=true for healthy tab → phantom header row insert + rewrite.
- **FIX:** skip repair when read returns 0 rows (suspect API error). Build+install done.

## DEF-39 — Cash over-tender change never deducted from till session (HIGH)
- **Code:** `ui/home/PaymentDialog.kt` L72-75 + TillSessionDao.kt L71-93 + CompleteSaleUseCase.kt L126-142 — expectedCash += :cashIn uses full TENDERED amount; change given back never subtracted → Z-report Expected Cash overstated by change on every over-tender sale.

## DEF-40 — GRN confirm not transactional / idempotent (HIGH)
- **Code:** `domain/usecase/grn/ConfirmGrnUseCase.kt` L34-147 + CreateGrnViewModel.kt L164-232 — mid-loop failure leaves items applied while GRN stays DRAFT; retry duplicates products/batches.

## DEF-41 — hasBatches never set on GRN/adjustment batches → phantom stock (HIGH)
- **Code:** AddProductUseCase.kt L19 + InventoryRepositoryImpl.kt L26-34 + ConfirmGrnUseCase.kt L85-146 + CompleteSaleUseCase.kt L184 — product created with 0 stock + GRN batch → hasBatches false → FIFO deduction skipped → recalculateTotalStock resets to batch sum → phantom stock inflation.

## DEF-42 — Discount input unvalidated → negative-total sales (HIGH)
- **Code:** HomeScreen.kt L1163-1167 + PosViewModel.kt L333-336, L427-432 — negative discount increases total; discount > subtotal → NEGATIVE total sale still completed. Expected: clamp [0, total].

## DEF-43 — Batch stock updates silently dropped from sync (HIGH)
- **Code:** SyncWorker.kt L443-469 uploadPendingProductBatches — uploads only NEW batches, marks ALL pending (incl. updates) synced → FIFO deductions/batch edits never reach sheet.

## DEF-44 — PO receivedQty never incremented → PO never RECEIVED (HIGH)
- **Code:** ConfirmGrnUseCase.kt L171-181 + PurchaseOrderDao.kt + CreateGrnViewModel.kt L79-93 — receivedQty always 0 → allFullyReceived false → PO stuck SENT forever (DEF-10 root cause); over-receiving not prevented.

## DEF-45 — Echo-clobber protection missing on 11 of 13 delta tables (HIGH)
- **Code:** DeltaSyncManager.kt L295-668 — echo-clobber (skip locally-pending) only on Inventory + Sales; Customers/Khata/Expenses/Batches/PO/GRN/Wastage/StockAdj/Categories unconditionally REPLACED by stale remote copy every 60s poll → pending edits lost silently.

## DEF-46 — Refund of credit sale creates no KhataEvent (MED)
- **Code:** ReturnsViewModel.kt L62-142 + KhataEventDao.kt L27 — refunded UDHAAR sale leaves baqaya inflated; no double-refund guard.

## DEF-47 — Loyalty points update not synced (MED)
- **Code:** CustomerDao.kt L36-37 + CompleteSaleUseCase.kt L107-124 — updateLoyalty doesn't set sync_status=pending → points device-local forever; read-modify-write race loses points.

## DEF-48 — Till session no terminal filter (MED-HIGH)
- **Code:** TillSessionDao.kt L23-24 + TillViewModel.kt L27-29 + ZReportViewModel.kt L64 — getOpenSessionFlow() no posTerminalId filter → multi-terminal confusion, day close reconciles WRONG session.

## DEF-49 — openTill no guard / closeTill stale session (MED)
- **Code:** TillViewModel.kt L33-70 + TillSessionDao.kt L32-48 — unconditional insert → two OPEN sessions possible; re-close already-closed session re-uploads.

## DEF-50 — Z-Report silent skip on null activeSession (MED)
- **Code:** ZReportViewModel.kt L131-146 + L113-116 — null session → "Day Closed Successfully!" without closing.

## DEF-51 — Z-Report NET IN DRAWER wrong (MED)
- **Code:** ZReportViewModel.kt L86-111, L171 — ignores opening cash/payIn/payOut/change; includes card/wallet/udhaar in cash drawer; collectors race-write.

## DEF-52 — Expense update/delete never adjusts till session (MED)
- **Code:** ExpenseViewModel.kt L66-77 — addExpense deducts expectedCash but edit/delete don't reverse → stale expectedCash at day close.

## DEF-53 — Settings createNewSheet incomplete tab list + no headers (MED-HIGH)
- **Code:** SettingsViewModel.kt L225-253 — missing Product_Units/Stock_Adjustments/Till_Sessions/Time_Clock/Barcode tabs, no headers → broken sync on Settings-created sheets.

## DEF-54 — Vendor markSynced unconditional on failure (MED)
- **Code:** VendorUpsertUseCase.kt L140-144 — batchWrite failed still marks synced → edits lost.

## DEF-55 — Search collectors leak coroutines / stale results (MED)
- **Code:** WastageViewModel.kt L88-100 + StockAdjustmentScreen.kt L74-81 + CrmViewModel.kt L69-80 — infinite Room Flow collectors never cancelled.

## DEF-56 — History paged flow duplicates last page (MED)
- **Code:** HistoryViewModel.kt L69-97 — Room flow re-emits on ANY Sales change; loadMore appends each emission.

## DEF-57 — Time clock OUT state broken (MED)
- **Code:** PunchClockViewModel.kt L37-63 — isClockedIn = last IN exists → after OUT still "clocked in"; duplicate OUT records; can't clock in again.

## DEF-58 — Payment amounts lost on rotation + UDHAAR clobber (MED)
- **Code:** PaymentDialog.kt L55-84 — remember not rememberSaveable; LaunchedEffect(balanceForKhata) overwrites typed credit; stale udhaar remains when toggle off.

## DEF-59 — Inventory no total_stock column (MED)
- **Code:** Constants.kt L78-104 + DeltaSyncManager.kt L274 + InventoryCrudViewModel.kt L347-351 — totalStock never uploaded, restore sets 0.0 → wrong addBatch math.

## DEF-60 — Returns restock uses stale batch list (MED)
- **Code:** ReturnsViewModel.kt L97-111 — fetches batches BEFORE update, recomputes from stale → totalStock undercounts.

## DEF-61 — GRN/PO number race (MED)
- **Code:** GrnRepositoryImpl.kt L35-39 + CreatePurchaseOrderViewModel.kt L134-135 — COUNT(*)+1 read-then-insert no unique constraint → duplicate doc numbers.

## DEF-62 — Sync TOCTOU duplicate rows (MED) — partially fixed by DEF-35
- **Code:** CompleteSaleUseCase.kt L229-247 + SyncOrchestrator.kt L116-131 + SalesUploadUseCase.kt L37-52 — getExistingUuids-then-append window; DEF-35 single-flight mitigates.

## DEF-63 — Delta timestamp parse fragile (MED)
- **Code:** SheetsRepository.kt L169-188 — toLongOrNull on exponent-notation strings → 0 → rows excluded from delta forever.

## DEF-64 — Time_Clock tab missing in createWorkspace (MED)
- **Code:** SheetsRepository.kt L47-57 — fresh workspace has no Time_Clock tab → time clock 400s (reopened DEF-09).

## DEF-65 — ManualStockAdjustment negative batch (MED)
- **Code:** ManualStockAdjustmentUseCase.kt L24-38 — negative qty creates negative-stock batch (dead code, future risk).

## DEF-66 — LowStock expiring badge stale (MED)
- **Code:** LowStockViewModel.kt L52 — map over nearExpiry while reading expiredItems — stale count.

## DEF-67 — SystemLogs search no-op (LOW)
- **Code:** SystemLogsViewModel.kt L74-77 — query ignored in filtered branch.

## DEF-68 — currencySymbol stale after settings change (LOW-MED)
- **Code:** PosViewModel.kt L142 + HomeScreen.kt L99 — captured at construction, needs restart.

## DEF-69 — cartTotal taxInclusive non-reactive (LOW)
- **Code:** PosViewModel.kt L116-125, L160-166 — toggle with non-empty cart keeps old total.

## DEF-70 — deductStockForSyncedSales dead code (LOW)
- **Code:** CompleteSaleUseCase.kt L100-103 + SyncWorker.kt L193-259 — never called.

## DEF-71 — GTINs unscannable (LOW-MED)
- **Code:** InventoryDao.kt L23-24 — getItemByBarcode matches barcode_id only, ItemGtins ignored.

## DEF-72 — Old-month invoice reprint fails (LOW-MED)
- **Code:** ReprintReceiptUseCase.kt L49-58 — determines only newest Sales tab.

## DEF-73 — Auto-GTIN collision (LOW-MED)
- **Code:** InventoryCrudViewModel.kt L196-205 — MAX(item_number)+1 no transaction; non-standard mask.

## DEF-74 — DbEncryption fallback passphrase derivable (HIGH — security)
- **Code:** DbEncryption.kt L40-45 — fallback "tillzo-db-fallback-"+packageName.hashCode → anyone computes, decrypts all data. Expected: fail closed.

## DEF-75 — RBAC empty-users = full access (MED — security)
- **Code:** SessionGuardUseCase.kt L37-61 — empty Users table → true for everything; sheet editor deletes rows → privilege escalation.

## DEF-76 — Backup ZIP PII in public Documents (LOW-MED — privacy)
- **Code:** LocalBackupManager.kt L45-100 — full CSV snapshots (sales, customers, khata) world-readable.

## DEF-77 — ForceUpdate off-by-one + remote kill switch (LOW)
- **Code:** CheckForceUpdateUseCase.kt L65-77 — blocks day 3 not day 4; min_app_version remote hard-block.

## DEF-78 — runBlocking token refresh ANR risk (LOW)
- **Code:** AuthRepositoryImpl.kt L100-108 — getAccessToken uses runBlocking network refresh.

## GAP-1 — PrinterSettingsScreen orphaned (HIGH) — no route, no caller
- **Code:** ui/hardware/printer/PrinterSettingsScreen.kt + ViewModel — zero references. Receipt print snackbar "No printer configured. Set MAC in Printer Settings." points to unreachable screen. Bluetooth/Wi-Fi printing never configurable.

## GAP-2 — BarcodeScannerScreen unwired (MEDIUM)
- **Code:** ui/hardware/scanner/BarcodeScannerScreen.kt + ScannerViewModel — referenced only by comment; hardware diagnostic says "Go to Settings > Hardware > Scanner Testing" — path doesn't exist.

---

## WATCHDOG RUN 2026-08-22 22:12–23:35 — DEF-81/82 FIXED, GAP-3/4 NEW, CRUD VERIFICATION COMPLETE

### Fixes (source patch → build → install → regression)
1. **DEF-81 (HIGH) — Stale cached access token → permanent 401 sync death.** Root cause: tokenAuthenticator par 401 → getValidToken() hi wapas — cached token expiry clock-check pass karta tha lekin token server-side invalid → refresh chain kabhi trigger nahi → HAR request 401 forever. Fix: authenticator mein sirf ACCESS token invalidate (refresh token intact). Verified: saare requests 200, Categories/Units/Inventory/Wastage/Returns sync green.
2. **DEF-82 (LOW) — Units list FAB overlap.** ProductUnitsScreen + CategoryManagementScreen contentPadding bottom 96dp. Verified: WatchdogUnit delete tap ab accessible.

### Micro-test results (har flow: open → add/update/delete → Force Sync → sheet verify)
- **Categories CRUD** ✅ — ADD WD-CAT-0812 → sheet row; UPDATE → WD-CAT-0812-UPD same row (no dup); DELETE → "Deleted remote row" + sheet row removed. **Sheet-verified 3/3.**
- **Product Units CRUD** ✅ — ADD WatchdogUnit/WU; UPDATE → WU2; DELETE → list se gone. Sheet final state consistent (row removed). **Verified 3/3.**
- **Inventory CRUD** ✅ — ADD WatchdogItemWD (WD-SKU-0822, 75/150, stock 5, WD-BATCH-0822) sheet-verified; UPDATE description WD-UPD-DESC→-V2 sheet mein; DELETE → sheet row removed, PROD-001/002 intact. **Sheet-verified 3/3.**
- **Wastage ADD** ✅ — HERMES-PROD-002 1.0 KG THEFT, Wastage_Ledger sheet row verified. DELETE: UI mein entry-delete action NAHI (GAP-4).
- **Returns** ✅ — SALE D3950BEA $110 (CASH $120, change $10) → Returns → full UUID lookup → "Refund Processed" → sheet par negative sale row `REFUND_OF_d3950bea_Restock` (total -110). **Sheet-verified.** Returns tab vestigial (GAP-3).

### Test data cleanup note
- WD-* records: WD-CAT-0812-UPD (deleted, sheet se removed), WatchdogUnit (deleted), WatchdogItemWD (deleted) — cleanup complete via delete flows. D3950BEA sale + REFUND negative row sheet par (real audit trail).
