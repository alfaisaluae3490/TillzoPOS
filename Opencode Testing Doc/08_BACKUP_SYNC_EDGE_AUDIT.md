# 08 — DEEP EDGE-CASE & ERROR AUDIT: SYNC / TIMEOUT / OFFLINE / BACKUP / RESTORE / SHARDING

**Run this file LAST on a FRESH install** (several tests degrade or wipe state). Mark every step with the `[PASS]/[FAIL]/[SKIP]/[BLOCKED]` convention. Reference `01` for constants.

---

## F8.1 NETWORK TIMEOUT HANDLING (Sheets API: 30s connect/read/write; 3-attempt print; WorkManager backoff)

- [ ] T1. **Slow-append simulation:** Use a proxy/Charles to delay Google Sheets responses by 35s. Trigger `"Force Sync"` with pending rows. Expected: OkHttp read timeout at 30s → sync worker retries (`runAttemptCount < 4`, EXPONENTIAL 5s→15s→1m→5m) → after 4 attempts `Result.failure()`. Record each attempt timing + final status. App must NOT crash and must remain usable.
- [ ] T2. **Timeout during sale sync:** Delay responses 35s, complete a sale. Record: receipt screen still renders (instant), `POST_SALE_INSTANT_SYNC` fails silently; pending badge retains count. Kill network delay → Force Sync → row uploads. Record.
- [ ] T3. **Timeout during restore:** Fresh install + select existing sheet, delay responses 35s → RestoreWorker retries 3× (EXPONENTIAL 10s) then failure; SheetPicker shows `"Restore Failed"` + `"Retry Restore"`. Record timing.
- [ ] T4. **401 token expiry:** Revoke token server-side (Google account → security → revoke) or set `token_expiry_ms` to past. Trigger sync → `tokenAuthenticator` retries once with new token (guarded by `Authorization-Retry` header). Record: 1 retry only, then failure → `RE_AUTH_NEEDED` → sign-in screen.
- [ ] T5. **OAuth refresh failure:** Force refresh_token invalid → `POST https://oauth2.googleapis.com/token` returns 400 `invalid_grant`. Record error surfacing.
- [ ] T6. **Zero network:** Airplane mode, Force Sync → `"Sync Failed. Please check connection."`; badge stays. Record.
- [ ] T7. **Slow printer:** Network printer with 10s lag → socket `soTimeout=3000` + 3 attempts `delay(1000, 2000, 3000)`. Record total elapsed (~9s) and final `"Print failed..."` string.

## F8.2 FIELD-LEVEL ABUSE (input matrix — execute on the Inventory "Add Product" form; record every row)

| Field | Input | Expected handling (from code) | Record actual |
|---|---|---|---|
| E1 | `Product Name` | empty | silent no-op (validation gate) |
| E2 | `Product Name` | 500 chars | accepted |
| E3 | `Product Name` | emoji/Unicode `🧪-हर्ड` | accepted |
| E4 | `SKU` | empty | silent no-op |
| E5 | `SKU` | `QA-SKU-<huge>` 200 chars | accepted |
| E6 | `Cost Price` | `abc` | `toDoubleOrNull()?:0.0` → 0, accepted |
| E7 | `Cost Price` | `-1` | `-1 >= 0` fails → no-op |
| E8 | `Cost Price` | `1e308` (overflow) | record |
| E9 | `Selling Price` | `999999999999.99` | accepted; record receipt/formatting |
| E10 | `Tax %` | `-5` | accepted (no range check) |
| E11 | `Tax %` | `9999999` | accepted; record POS cart math |
| E12 | `Current Stock` | `abc` | coerced 0 |
| E13 | `Current Stock` | `-0.5` | `>= 0` fails → no-op |
| E14 | `Current Stock` | `1e308` | record (Double infinity risk) |
| E15 | `Expiry Alert (Days)` | `abc` | `toIntOrNull()?:30` → 30 |
| E16 | `Expiry Alert (Days)` | `-100` | stored -100 |
| E17 | `Exp Date` | past date | accepted (no DatePicker range) |
| E18 | `Mfg Date` | after Exp Date | accepted |
| E19 | `Batch Number` | blank | silent no-op (validation gate) |
| E20 | `GTIN` | `123` | accepted (no checksum) |
| E21 | `GTIN` | `ABC-123-XYZ` | accepted |
| E22 | `GTIN` | 30 digits | accepted |
| E23 | `Damaged Qty` | `-5` | `?:0.0` → -5 accepted |
| E24 | `Damaged Qty` | greater than stock | accepted |
| E25 | `Unit` | `Manage Units...` | navigates to units screen |
| E26 | duplicate GTIN add | same value twice | record list state |

Execute the same abuse matrix on: Customer dialog (E27: phone `abc` accepted; E28: name 300 chars; E29: WhatsApp `+92300` accepted), Expense dialog (E30: amount `1e309`; E31: category unselected — default Rent), Khata dialog (E32: amount `abc`; E33: `0`; E34: `-1`), Vendor dialog (E35: creditLimit `abc`→0; E36: name blank), PO item qty (E37: `abc`; E38: `0`; E39: `-1`), GRN received qty (E40: `abc`; E41: `-2`), Stock Adjustment qty (E42: `abc`; E43: `-999` on stock 1 → clamps 0), Wastage qty (E44: `0`; E45: `-3`; E46: > stock), Z-Report physical count (E47: `abc`; E48: `-100`; E49: `1e15`).

## F8.3 SHEET CORRUPTION / MEDDLING (Google Sheets direct edits — verify app tolerance)

- [ ] T1. **Header row deleted:** remove row 1 (headers) of `Inventory` tab → app behavior on next sync/read (delta upsert by index). Record.
- [ ] T2. **Tab deleted:** delete `Customers` tab in Sheets → make a customer change → Force Sync. Record: `addSheet` recreation? Error? (createWorkspace only on provisioning; SyncWorker does NOT recreate missing tabs). Record exact failure.
- [ ] T3. **Tab renamed:** rename `Inventory` → `InventoryX` → record sync failure + pending rows stuck.
- [ ] T4. **Cell type corruption:** set `stock_qty` cell of a product to `"not-a-number"` → delta upsert `toDoubleOrNull` handling → record crash vs skip.
- [ ] T5. **Duplicate system_row_id rows:** copy a product row (same UUID) → delta upsert REPLACE → record which row wins.
- [ ] T6. **`SYS_DB_DO_NOT_TOUCH` unhidden:** unhide the tab in Sheets → run sync → `verifyAndHideSysDbTab()` must re-hide it. Record.
- [ ] T7. **Settings cell edit:** set `last_updated_timestamp` to a far-future epoch → delta sync should skip until remote advances. Record poll behavior.
- [ ] T8. **18k-row overflow:** if achievable (bulk insert), verify `MonthlyShardWorker` creates `Sales_MMM_YYYY_OVF` at `ROW_LIMIT=18_000` and new sales go there. Record.

## F8.4 DELTA SYNC CONFLICT & CURSOR (two-device test recommended)

- [ ] T1. **Local pending wins (Inventory/Sales):** Device A creates product (pending). Device B edits the SAME product in Sheets. Device A runs delta poll before upload → row skipped (pending wins); after A's upload, B's edit is LOST (uploaded snapshot overwrites). Record end state.
- [ ] T2. **Other tables REPLACE:** Device B edits a Customer in Sheets (updated_at future) while A has a pending same-customer edit → REPLACE → B's version wins, A's local pending edit LOST. Record.
- [ ] T3. **Cursor never advances on failure:** corrupt a row in one tab (F8.3 T4) → poll fails → cursor NOT advanced → same failure every 60s until fixed. Record log line `"Delta upsert had failures — cursor NOT advanced, will retry"`.
- [ ] T4. **Cursor skip:** remote `last_updated_timestamp <= local` → poll skipped entirely. Record ~60s cycle timing.
- [ ] T5. **Fresh install restore:** wipe app → select same sheet → restore pulls ALL rows (timestamp filter `lastTimestamp==0` keeps all) → counts match Sheet. Record duration for 1,000-row sheet.
- [ ] T6. **Restore mid-write race:** on fresh install, select sheet WHILE another device is uploading → record partial data + cursor state.

## F8.5 BACKUP / RESTORE (LocalBackupManager + workers)

- [ ] T1. **ZIP export (Settings → "Local Backup"):** CreateDocument → progress strings `"Exporting backup..."` → `"Writing ZIP file..."` → `"Backup complete!"`; ZIP contains EXACTLY 6 CSVs with exact headers:
  - `Inventory.csv`: `system_row_id, item_name, category, barcode_id, unit, price_per_unit, current_stock, low_stock_threshold, sku, cost_price, tax_percent`
  - `Sales.csv`: `system_row_id, sync_uuid, cashier_id, timestamp, subtotal, tax, discount, total, payment_method, cash_amount, card_amount, wallet_amount, udhaar_amount, customer_id`
  - `Customers.csv`: `system_row_id, name, phone, whatsapp, email, address`
  - `KhataEvents.csv`: `system_row_id, customer_id, event_type, amount, note, reference_sale_id`
  - `Expenses.csv`: `system_row_id, category, amount, description, timestamp, logged_by_user_id`
  - `TillSessions.csv`: `sessionId, cashierName, openingCash, closingCash, expectedCash, totalCashSales, totalCardSales, totalWalletSales, totalUdhaarSales, totalSalesCount, totalRefunds, netCash, status, shiftDate, openedAt, closedAt`
- [ ] T2. CSV escaping: create a product with `,` and `"` and newline in description → verify escaped (`"` → `""`, wrapped in quotes).
- [ ] T3. **Public Documents backup ("Back Up Now"):** files `tillzo_backup_<yyyyMMdd_HHmmss>.zip` + overwritten `latest_tillzo_backup.zip` in `Documents/TillzoPOS`; string `"Backup saved: Documents/TillzoPOS/<file>"`. Run twice → both files exist; timestamped file accumulates (NO rotation count).
- [ ] T4. **No restore path exists** — confirm absence of any import/restore-from-zip UI → `[PASS]`.
- [ ] T5. **Nightly worker:** `NightlyBackupWorker` daily at 23:59 + `auto_local_backup` 00:15 — record log entries at next scheduled run (or force via `adb shell cmd jobscheduler run` if applicable).
- [ ] T6. Backup with open output URI failure → `Exception("Could not open output URI")` → record surfaced error.

## F8.6 ENCRYPTION / DB INTEGRITY

- [ ] T1. **SQLCipher:** `tillzo_pos_db` must be encrypted (verify via `adb shell` — file should NOT be plaintext SQLite). Passphrase = `DbEncryption.getOrCreatePassphrase`: AES-256-GCM key in AndroidKeyStore alias `tillzo_db_encryption_key`, passphrase = 64 hex chars. Record passphrase stability across app restarts (deterministic per install).
- [ ] T2. **Keystore failure fallback:** if unreachable → `"tillzo-db-fallback-<pkgHash>"` + log `"DbEncryption" / "Keystore failed, using fallback: ..."`. Record via logs if reproducible.
- [ ] T3. **Data survives process kill:** create data → force-stop app → relaunch → data intact (prefs + Room + key same).
- [ ] T4. **Tamper check:** DEBUG build logs tag `"TAMPER_CHECK"` verifying APK signature — record log line present.

## F8.7 APP CRASH & LOG ROTATION

- [ ] T1. Crash: `adb shell am crash com.tillzo.pos` → relaunch → app_logs contains FATAL entry tag `"APP_CRASH"` with stacktrace + device info. Record.
- [ ] T2. Log rotation: generate >512KB of logs → verify `app.log` → `app.1.log` ... `app.5.log` rotation (max 5 files). Record file list in `filesDir`.
- [ ] T3. 48h purge: backdate a log entry's timestamp >48h → app restart → purged (`"Log retention cleanup complete"`). Record.
- [ ] T4. Notification channels: verify channel IDs exist: `low_stock_channel` ("Low Stock Alerts", HIGH), `out_of_stock_channel` ("Out of Stock Alerts", HIGH), `expiry_channel` ("Expiry Alerts", DEFAULT). Record notification titles:
  - Low: `"⚠️ Low Stock: <name>"` / `"Only <qty> <unit> remaining. Time to reorder."`
  - Out: `"🚫 Out of Stock: <name>"` / `"<name> is completely out of stock."`
  - Expiring ≤0: `"❌ EXPIRED: <name>"` / `"Expired on <date>"`
  - Expiring >0: `"⏰ Expiring Soon: <name>"` / `"Expires in <n> days (<date>)"`
  - `nearExpiryAlert` = expiryNotification(daysLeft 7); `expiredAlert` = 0.
- [ ] T5. NO sync-failure / update notifications exist (verified absent) — confirm → `[PASS]`.

## F8.8 PERMISSIONS / LIFECYCLE / ORIENTATION

- [ ] T1. Runtime permission flows: CAMERA (first-run grant/deny), POST_NOTIFICATIONS (API 33+), BLUETOOTH_CONNECT/SCAN (API 31+), WRITE_EXTERNAL_STORAGE ≤28. Record each denial path.
- [ ] T2. Rotation on every dialog: PaymentDialog, AddProduct, VendorForm, AddBatch, DatePicker, AdminPinDialog — record state retention (local `remember` vs ViewModel).
- [ ] T3. Background: mid-payment → home button → 10 min → resume. Record session/token state (token cache expiry buffer 5 min).
- [ ] T4. App in background while printer printing → record.
- [ ] T5. Multi-window/split-screen → record layout.
- [ ] T6. Date/time change (set clock forward 2 days mid-session) → expiry alerts, Z-Report window, shard month-switch. Record.

## F8.9 RACE CONDITIONS

- [ ] T1. Sale + concurrent Force Sync (start sync, then complete sale) → record both complete without duplicate rows (dedupe via `getExistingUuids`).
- [ ] T2. Two devices complete sales for the SAME product simultaneously → record stock divergence + delta sync resolution (last writer).
- [ ] T3. Delete product → immediate re-add same name → record UUID distinctness and sync.
- [ ] T4. GRN confirm double-tap → record duplicate `GRN_Headers` rows (F4.5 E8).
- [ ] T5. Expense save double-tap → duplicate rows (F5.2 E7).
- [ ] T6. Pay In while Z-Report dialog open → record.
- [ ] T7. Restore worker running while user creates data → record local data survival (restore REPLACE could clobber — flag outcome).

## F8.10 GLOBAL TIMEOUT & RESILIENCE SUMMARY CHECKS

- [ ] T1. App cold-start with zero network → must reach home screen (data local-first). Record any startup dialog failure.
- [ ] T2. App cold-start with revoked token → sign-in re-prompt latency. Record.
- [ ] T3. Sheet spreadsheet_id pref pointing to DELETED spreadsheet → sync behavior (repeated retries). Record `sync_status="failed"` rows in `Sync_Log` tab (cols: `sync_uuid, pos_id, status, timestamp, error_msg`).
- [ ] T4. Device storage full (fill internal storage) → Room insert / ZIP backup behavior. Record crash vs graceful.
- [ ] T5. Locale change (device language → Urdu/Arabic) — strings.xml exists for `values-ur` and `values-ar` → record RTL + date formatting (`dd MMM yyyy` Locale behavior).
- [ ] T6. App upgraded in place (install APK over old) — Room version 31 migration. Record data survival.
