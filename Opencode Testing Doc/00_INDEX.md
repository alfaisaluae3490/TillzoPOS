# TILLZO POS — PRODUCTION-READY TESTING PROTOCOL (for Hermes)

**Executing agent:** Hermes
**Target app:** Tillzo POS (Android, Kotlin + Jetpack Compose + Room + Google Sheets API v4 + Drive API)
**Workspace:** `C:\Users\Faisal Khan\Desktop\Tillzo`
**Protocol files (execute in this order):**

| # | File | Scope |
|---|---|---|
| 0 | `00_INDEX.md` (this file) | Conventions, prerequisites, log format |
| 1 | `01_SHEET_SCHEMA_AND_API.md` | Phase 1 Micro-Mapping Matrix: sheets, columns, endpoints, sync, DB |
| 2 | `02_POS_MODULE.md` | Home / Cart / Payment / Receipt / Complete Sale |
| 3 | `03_INVENTORY_MODULE.md` | Product CRUD, Batches, Categories, Units, Stock Adjust, Wastage, OCR, QR, Barcode Print Settings, Stock Alerts |
| 4 | `04_PO_GRN_VENDOR.md` | Purchase Orders, Goods Receipts, Vendors |
| 5 | `05_STORE_MODULE.md` | CRM/Khata, Expenses, History, Returns, Statement, Time Clock, Verify QR, Z-Report |
| 6 | `06_SETTINGS_AND_SECURITY.md` | Settings, PIN, Billing, Logs, Data Viewer, Users, Till, Force Update, Root Block |
| 7 | `07_SETUP_HARDWARE_AUTH.md` | Sign-in, Onboarding, Sheet selection, Printers, Scanner, Diagnostics |
| 8 | `08_BACKUP_SYNC_EDGE_AUDIT.md` | Deep edge-case commands: timeouts, offline, corruption, concurrency |
| 9 | `09_KNOWN_DEFECTS_REGISTRY.md` | Expected-Failure checks for confirmed code defects |
| 10 | `10_EXTERNAL_REPORT_ANALYSIS.md` | QA-lead analysis of the 2 external AI audit reports (what to trust, what is wrong) — read, do not execute |
| 11 | `11_SUPPLEMENTAL_TEST_STEPS.md` | Net-new test steps extracted from the external reports (S-1 … S-12) — execute after files 02–09 |

---

## 1. TEST-DATA CONVENTIONS (MANDATORY — zero exceptions)

- Every record Hermes creates MUST be uniquely identifiable. Tag names with the prefix **`HERMES`** plus a sequence, e.g. `HERMES-PROD-001`, `HERMES-CUST-001`, `HERMES-VEND-001`, `HERMES-PO-001`, `HERMES-EXP-001`.
- Never reuse a sequence number. The sequence counter is global across the whole session (`001`, `002`, ...).
- Monetary test values: use `1234.56` for a primary record and `0.01` for the cheapest possible record.
- Quantities: primary record `3.5` (if unit is KG/GM/ML/L) or `3` (if PC).
- Barcodes: use the app's auto-generated GTIN (leave the GTIN field blank on create) and record it from the product card. Never hand-type a barcode unless a step explicitly says so.
- Dates: always use tomorrow's date for "expiry" and "expected delivery"; always use today for "mfg" / "sale".
- Every Google Sheets verification MUST record: `TAB`, `COLUMN`, `ROW NUMBER`, `CELL VALUE` in the log.
- After every sync-dependent step, wait for the 15-minute periodic `AUTO_SYNC_WORKER` OR trigger **Force Sync** (Home → Advanced Options → `"Force Sync"`) and wait for the sync dialog to complete.

## 2. PASS/FAIL LOGGING RULES

Every step in this protocol ends with one of:

```
[PASS] <step id> — observed: <exact value observed>
[FAIL] <step id> — expected: <value from protocol> | actual: <value observed>
[SKIP] <step id> — reason: <why>
[BLOCKED] <step id> — reason: <hardware/environment missing>
```

- A step is PASS only when the observed value EXACTLY matches (case-sensitive, symbol-sensitive) the expected value.
- Partial matches are FAIL with both values recorded.
- Never auto-correct a step; report and move to the next step. Defects are collected in the file `09_KNOWN_DEFECTS_REGISTRY.md` at the end.
- For UI steps, screenshots are required on every FAIL. For Sheets steps, record the cell reference.

## 3. PREREQUISITES & ENVIRONMENT

- Physical Android device (NOT an emulator for printer/scanner tests; emulator allowed for pure data/sync tests).
- Device must NOT be rooted — the app blocks rooted devices with the `RootBlockedScreen` ("EXIT APP" only).
- Google account with an empty Google Drive (or the dedicated QA Gmail account `tillzo.qa.<env>@gmail.com`).
- App installed from the current build (`BuildConfig.VERSION_NAME` recorded at session start).
- One Google Sheet will be created by the app itself during first run. Record its spreadsheet ID (shown in Settings → "Connected Sheet ID:").
- For printer tests: a TSPL-compatible Bluetooth thermal printer (40mm) and an ESC/POS Wi-Fi printer on port 9100, OR mark steps `[SKIP]`.
- For network-failure tests: ability to toggle device Airplane mode on/off.
- Wipe strategy between test suites: Settings → no "Clear Local Data" button exists — use `adb uninstall com.tillzo.pos` + reinstall, or clear app data, then re-run onboarding.

## 4. EXECUTION ORDER RULES

1. Run `01` (mapping familiarization) first — read it, do NOT execute.
2. Execute `02` → `03` → `04` → `05` in order (each depends on products created earlier).
3. Execute `06` → `07` (environment/toggles).
4. Execute `08` edge-case matrix LAST on a FRESH install (it wipes/degrades state).
5. Execute `11` (supplemental steps S-1 … S-12) — includes re-verifications of the external reports' claims.
6. At the end, compile all `[FAIL]` and `[XF]` (expected-failure) results into `09_KNOWN_DEFECTS_REGISTRY.md`. Steps that surface new defect candidates write `DEF-25+` entries.

## 5. GLOBAL VERIFICATION POINTS (used by many steps)

- **Sync dialog:** HomeScreen shows sync status via `syncOrchestrator.getManualSyncWorkInfo()`; strings: `"Sync in progress..."`, `"Sync Completed Successfully!"`, `"Sync Failed. Please check connection."`.
- **Pending-sync badge:** HomeScreen top bar red badge with count = pending rows across Sales + Till_Sessions + Expenses + KhataEvents + Customers + Inventory. Max displayed `99`.
- **Sync dot:** green when 0 pending, amber (`WarningAmber`) when pending > 0.
- **Google Sheets row identity:** the `system_row_id` column (column A on most tabs) is a UUID; Sales tab column A is `invoice_id` (also a UUID, equal to the value shown on the receipt truncated to 8 uppercase chars).
- **`sync_status` column values:** `pending` → after upload `synced`; on failure `failed`. Soft-deleted rows keep `is_deleted = 1` and `deleted_at` set — the row is NOT physically removed except for PO/GRN/Wastage/StockAdjustments/Batches/Till which use append-only + `markSynced`.

## 6. SHORT NAMES USED THROUGHOUT

- `POS` = HomeScreen ("TillzoPOS" top bar)
- `Sheet` = the app-created Google Spreadsheet
- `AUTO_SYNC` = the 15-minute periodic `SyncWorker`
- `Force Sync` = Advanced Options → `"Force Sync"`
- `sync_status`: `pending` / `synced` / `failed` / `never`
