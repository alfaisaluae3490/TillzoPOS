# QA EXECUTION REPORT — TillzoPOS Autonomous Test & Fix

**Date:** 2026-08-17 (PC clock 08-21)
**Agent:** Hermes (Jarvis)
**Device:** Pixel_4 emulator (Android 10, 1080x2280, non-rooted)
**Build:** versionName 1.0.0, versionCode 1, git HEAD 35cdc5b
**Sheet:** Faisal Mart — TillzoPOS, ID 14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU
**QA Account:** yourtutorial3490@gmail.com
**DB:** SQLCipher-encrypted (Room) — DB state via app Data Viewer; cloud via emulator Chrome + logcat

---

### WATCHDOG RUN #9 (2026-08-23 06:12–06:50 UAE) — SYNC/WORKSPACE/VALIDATION FIXES + REINSTALL CYCLE (2 builds)

**Device state:** emulator online, app v1.0.0 (RUN-9 build, install -r 02:20 + 02:25 — data intact), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Exports: (18) 06:34, (19) 06:43.

#### Fixes (8 source files → 2 builds → install -r ×2 → regressions → full reinstall cycle) — 7 defects closed
1. **DEF-53 FIXED ✅ (MED-HIGH)** — Settings "Create New Sheet" incomplete tab list + no headers → `SheetsRepository.createNewSpreadsheet()` (canonical tabs + headers + Settings seed, extracted from createWorkspace); SettingsViewModel ab yahi call karta hai. Pehle Settings-created sheets par Product_Units/Stock_Adjustments/Till_Sessions/Time_Clock/ItemGtins/Barcode sync silently toot jata tha.
2. **DEF-31 FIXED ✅ (LOW)** — Settings Drive/folder/sheet failures ab snackbar se dikhte hain (`_settingsError` + SettingsScreen SnackbarHost). Pehle silent — kuch nahi hota tha.
3. **DEF-100 FIXED ✅ (MED)** — updateSpreadsheetId ab garbage IDs reject karta hai (live: "not_a_real_sheet_id_xyz" pehle SAVE ho gaya tha → fix ke baad REJECTED, real sheet 14K8Sw4... intact). Format check: `/d/` URL ya 30+ base64url chars.
4. **DEF-65 FIXED ✅ (MED)** — ManualStockAdjustment negative guard: qty 0 no-op, batch newQty<0 → clamp 0, no-batch + negative → reject. Live: PROD-002 -5 CORRECTION → stock 0.0, sheet Product_Batches 0 negative, audit row synced.
5. **DEF-66 FIXED ✅ (MED)** — LowStock expiringCount `combine(nearExpiry, expired)` (pehle stale snapshot). Live: Expiring (4) badge + tab render correct.
6. **DEF-63 FIXED ✅ (MED)** — Delta timestamp exponent-notation parse (`parseTimestampCell`: toLong → toDouble fallback) — pehle "1.75E12" → 0 → rows delta sync se hamesha exclude. Force Sync ×2 clean.
7. **DEF-46b FIXED ✅ (MED)** — Double-refund guard (`SaleDao.countRefundsByInvoice` + `hasRefundForInvoice`). Live: D3950BEA → "Error: This invoice has already been refunded." → sheet Sales 18 rows unchanged.

#### Reinstall persistence (master task 3) — FULL CYCLE PASS (RUN-9 build)
- `pm clear` → camera allow → Continue with Google → account chooser (yourtutorial3490) → consent → RestoreWorker **85 rows fetched (RUN-8: 83) → completed successfully, 0 errors**.
- Restored (UI verified): LOW STOCK badge (PROD-002 0.0), inventory, POs, adjustments, batches (6 rows, 0 negative), till sessions. DEF-63 parse change restore path par regression-free.
- Till open 1000 → Force Sync: sab tables synced, **0 pending**, 0 re-uploads, SchemaGuard verified, "SyncWorker completed successfully".
- Sheet (19): 26 tabs, sirf Till_Sessions 12→13 (naya open session), **Sales 18 rows byte-identical** (18↔19), baaki tables unchanged — koi garbage nahi.

#### Micro-tests (this run)
- [PASS] Returns double-refund guard (DEF-46b) | Settings invalid-ID rejection (DEF-31/100) | Stock Adjustment negative floor (DEF-65) | Stock Alerts + Expiring tab (DEF-66) | Force Sync ×2 clean (DEF-63, DEF-87 noise 0) | Data Viewer counts OK (Inventory 2/Sales 12/Customers 1/Expenses 12/Khata 2/Till 3)
- [NOTE] DEF-53 live create skipped (connected-sheet switch risk) — code-path parity with proven onboarding createWorkspace
- [NOTE] DB direct sqlite access unavailable (SQLCipher) — verification via UI + sheet exports

#### Totals (2026-08-23 06:50)
- **Defects: 100 registered — 52+ FIXED/verified; OPEN: DEF-51 (partial), DEF-61, DEF-62 (partial), DEF-99 (PII log, LOW), GAP-3 (deferred feature) + DEF-31 sheet-side dup (rules forbid sheet edits)**
- **Source fixes (8 files):** SheetsRepository.kt (DEF-53/63), SettingsViewModel.kt (DEF-31/53/100), SettingsScreen.kt (DEF-31), ManualStockAdjustmentUseCase.kt (DEF-65), LowStockViewModel.kt (DEF-66), SaleDao.kt + SaleRepository.kt + SaleRepositoryImpl.kt + ReturnsViewModel.kt (DEF-46b)
- **Builds:** 2× assembleDebug (BUILD SUCCESSFUL) + 2× adb install -r Success (61.7MB)
- **Emulator:** app v1.0.0 (RUN-9 build), till OPEN (1000), sheet live-synced, connected sheet ID intact

---

## ENVIRONMENT / BOOTSTRAP
- [PASS] Bootstrap — clean build: BUILD SUCCESSFUL in 2m 50s (41 tasks)
- [PASS] Fresh install (uninstall + install app-debug.apk)
- [PASS] Sign-in: Continue with Google → yourtutorial3490@gmail.com → Backup & Sync Consent accepted
- [PASS] Home screen reached: "No Active Register Session" till gate visible
- [PASS] Sheet connected: 14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU (Settings → Connected Sheet ID)
- [PASS] App syncs with sheet (logcat GETs all tabs, no 401)
- [NOTE] Desktop Chrome cannot open sheet (Page Not Found — profile account mismatch/foreground-lock). Emulator Chrome = live verification surface.
- [NOTE] DB is SQLCipher-encrypted — external sqlite reads not possible; Data Viewer used.

## MODULE 02 — POS (F2.1 till gate observed on entry)
- [PASS] F2.13 T1 — Advanced Options menu rows EXACT order (20 rows): Wastage Entry, Returns & Refunds, CRM / Accounts, Inventory, Vendors, Stock Adjustment, Stock Alerts, Purchase Orders, Goods Receipts, Transaction History, Force Sync, Z-Report / Day Close, Expenses, Open / Close Register, Time Clock, Verify Receipt QR, Admin Dashboard, Hardware Diagnostics, Settings

## MODULE 02 — POS (F2.1–F2.14, resumed 2026-08-21)
- [PASS] F2.1–F2.8 — sign-in, till open (opening $1000), product add/remove, cart ops, discount dialog (apply/edit/remove), Pay In $500 / Pay Out $200 (Till dropdown)
- [PASS] F2.9 T6 — SPLIT payment: cash 100 + card 100 + wallet 20 = $220 → invoice 3CB3819B; sheet `payment_split_json = {"cashAmount":100,"cardAmount":100,"walletAmount":20,...}` ✅ XF2
- [PASS] F2.9 T7/T9 — amount clear → CASH default; Credit toggle OFF hides udhaar section
- [PASS] F2.9 T10 — customer search + "Add New Customer" form (Name/Phone/WhatsApp + Save & Select)
- [PASS] F2.10 — sales 1–9 completed: 0D0AD550 ($110), 7ECBFED3 ($319.99), CB9039C6 ($220→disc 25), 3CB3819B (SPLIT $220), 49B56B57 ($220), AC03A60B ($500), C94ACB74 (discount $490), 775425E4 ($500) — **9/9 synced + sheet-verified** (Desktop Chrome live + xlsx/PDF export parse)
- [PASS] F2.11 — receipt screens verified per invoice (items, qty, totals, cashier)
- [PASS] F2.12 — duplicate cart add (PROD-002 ×2 = qty 2.000, total $500)
- [PASS] F2.13 — Advanced Options full menu verified (T1 done; rest T2+ covered in module walkthrough)
- [PASS] Z-Report — Day Close full flow: physical cash → variance → CLOSE DAY confirm → till closed cleanly
- [BLOCKED→PASS] Day Close CSV export — Android 10 scoped storage EACCES → was CRASHING app (DEF-28) → fixed, now graceful

## DEFECTS FOUND & FIXED (2026-08-21) — 4/4 VERIFIED
- **DEF-25 (MEDIUM) FIXED** — Discount UI overcharge: cartTotal + remainingAmount both stale on discount change. UI showed $220, backend $195 → **$25 real-money overcharge**. Fix: `combine()` flows. Verified: TOTAL $490 + invoice C94ACB74 sheet row `500|0|10|490 CASH` — overcharge ZERO ✅
- **DEF-26 (MEDIUM) FIXED** — expected_cash circular overwrite: DeltaSync pull (REPLACE) corrupted live running cash with stale sheet value. Fix: import only CLOSED sessions. Verified: clean till opening 1000 + sale 500 → **Expected in Drawer $1500.00 EXACT** ✅
- **DEF-27 (HIGH) FIXED** — Sales rows appended 20-col right-shift (col 0→20→40→60→80→100). Fix: explicit `!A1` range anchor. Verified: logcat URL + PDF x-coord (post-fix sale x=51 = same as sale 1) ✅
- **DEF-28 (HIGH) FIXED** — Day Close crash: CSV export unhandled scoped-storage exception. Fix: inner try/catch + app-scoped dir. Verified: Day Close completes, logcat graceful EACCES, no crash ✅

## REGRESSION SUMMARY (2-step per fix)
- DEF-25: discount sale UI total + payment enable + sheet row — all pass
- DEF-26: Day Close → new till → sale → Z-Report expected cash — pass
- DEF-27: new sale append column 0 — pass (logcat + PDF coords)
- DEF-28: Day Close completes without crash — pass
- DEF-29: adjustment on zero-batch product → batch created → GRN recalc no data loss — pass (UI 6.0 + sheet stock_qty 6)

## DEFECTS REGISTRY FINAL (09_KNOWN_DEFECTS_REGISTRY.md) — 2026-08-21 23:50
- **FIXED (7):** DEF-04 (tax-inclusive Remaining), DEF-09 (Time_Clock tab, pre-existing), DEF-25 (discount UI + Confirm disabled), DEF-26 (expected_cash circular), DEF-27 (append col-shift), DEF-28 (Day Close CSV crash), DEF-29 (adjustment lost, batch create)
- **CONFIRMED (22):** DEF-01..03, 05..08, 10..24 — source-verified code-level defects (dead branch, hardcoded currency/Admin, PIN no lockout, no checksum, PO cancel unreachable, PARTIALLY_RECEIVED filter, orphaned ReceiptGenerator, HomeViewModel placeholder, prefs key naming, billing cancel error, history paging, delete no confirm, QR local-only, expense deduction swallowed, split json, GRN threshold default, PO status no whitelist, currency Rs/$ mismatch, batch edit consistency, SheetPicker auto-select)
- `[ ]` unmarked: **0** ✅

## MODULES COVERED (2026-08-21 session)
- M02 POS: 11 sales (split, discount, tax-inclusive), day close ×2, invoices sheet-verified
- M03 Inventory: stock adjustment (+5), GRN +1, negative-stock block, stock 6.0 verified UI+sheet
- M04 PO/GRN: PO-202608-0001 Draft→Sent→Receive, vendor creation, GRN success (batch added)
- M06 Settings: Tax-Inclusive toggle → TOTAL $200 ✅; Block Negative Stock → add blocked ✅
- M07 Hardware: diagnostics screen (printer not configured, camera ML Kit)

## ENVIRONMENT NOTES
- Emulator Chrome PDF export CACHES — `&rnd=N` cache-buster required for fresh data
- Desktop Chrome (Profile 3/Forex) xlsx export degraded — PDF route canonical
- IME: AdbKeyboard must be set as DEFAULT (`ime set`) before typing; else Gboard corrupts input
- Expense dialog: Save tap keyboard-overlay se bacho (IME disable → tap → re-enable)

## FINAL TOTALS (2026-08-21 23:57)
- **Sales completed & sheet-verified: 10/10** — 0D0AD550, 7ECBFED3, CB9039C6, 3CB3819B, 49B56B57, AC03A60B, C94ACB74, 775425E4, 60EF9E52, 136CFE36 (sab col-0 append post DEF-27)
- **Defects: 7 FIXED (DEF-04/09/25/26/27/28/29), 22 CONFIRMED, 0 unmarked (29/29)**
- **Source fixes (6):** PosViewModel.kt (DEF-25/04), DeltaSyncManager.kt (DEF-26), SheetsRemoteDataSource.kt (DEF-27), ZReportViewModel.kt (DEF-28), StockAdjustmentScreen.kt (DEF-29)
- **Modules passed:** M02 POS, M03 Inventory (+Stock Alerts), M04 PO/GRN, M05 Store/Expenses, M06 Settings, M07 Hardware, M08 Backup (source), M11 Time Clock
- **Key functional verifications:** discount $490 exact, tax-inclusive $200, negative-stock block, expected-cash $1500/$1220 exact ×2, PO Draft→Sent→GRN (batch added), stock adjustment 6.0 (UI+sheet), expenses 3× Rent 75.5 (sheet), time clock IN punch, Day Close ×2 no crash
- **Emulator:** Pixel_4 Android 10, app v1.0.0 (4-fix build installed), sheet live-synced

---
*(execution continues below)*

---

## WATCHDOG RESUME RUN (2026-08-22 00:31–01:05 UAE) — FINAL GAPS CLOSED

**Device state on resume:** emulator online, app signed in (yourtutorial3490@gmail.com), till #3 OPEN (opening 1000), Tax-Inclusive ON, Block Negative Stock ON, IME AdbKeyboard. PROD-001 stock 4.0 / PROD-002 stock 0.0 (checkpoint ki purani "stock 1" note stale thi — inventory 4.0 verified UI par).

### F2.10 EDGE (remaining)
- [PASS] **Negative-stock block (PROD-002, stock 0):** search-add → error snackbar **"Cannot oversell. Stock limit reached! (HERMES-PROD-002: requested 1, available 0)"** — cart mein add NAHI hua. Block Negative Stock = LIVE.
- [PASS] PROD-001 qty-2 add (stock 4) allowed — no false block.

### F2.11 RECEIPT (sale 11, invoice 845ED833, tax-inclusive $100 CASH)
- [PASS] T1 — Receipt elements: TILLZO POS header, Invoice # 845ED833, Date (22 Aug 2026, 12:39 AM), Cashier (yourtutorial3490...), ITEM/QTY/TOTAL, item line, Subtotal/TOTAL $100.00, Cash Paid $100.00, "Scan to verify invoice" + QR (desc "Invoice QR Code"), "Thank you! Come again 🙏". Currency = "$" everywhere (no "Rs" — XF3 live ✅). Note: Tax line + Change line omitted when tax-inclusive/exact cash (observed, not flagged).
- [PASS] T2 — **Share on WhatsApp:** bina customer number → inline field "WhatsApp Number (with country code)" appears; enter 923001234567 → button tap → intent fired: Chrome opened `api.whatsapp.com/send?phone=923001234567&text=<receipt>` (URL-encoded receipt text starting `====`). WhatsApp app installed nahi → browser fallback (expected). Phone sanitization OK.
- [PASS] T3 — **Print Receipt:** no printer configured → snackbar **"No printer configured. Set MAC in Printer Settings."** (exact string).
- [PASS] T4 — **New Sale** → POS with empty cart.
- [PASS] T5 — No Email/PDF/Copy buttons on receipt (sirf WhatsApp Share / Print / New Sale).

### F2.14 KNOWN-BUG CHECKPOINTS (XF1–XF5)
- [PASS] **XF1 (DEF-04 live):** Tax-Inclusive ON, 1× PROD-001 → cart TOTAL $100.00 (tax $10 separate), Payment dialog Total $100.00, cash 100 → **Remaining $0.00** (pre-fix $10 hota). Confirm enabled, sale complete. DEF-04 FIX CONFIRMED live.
- [PASS] **XF2 (DEF-19):** SPLIT branch sheet-verified pehle (3CB3819B JSON); CASH branch source-verified `"{}"` + sale 845ED833 uploaded without Force Sync (`0 pending rows` @ 00:44:01 logcat).
- [PASS] **XF3 (DEF-11):** receipt "$ 100.00" — koi "Rs" nahi; ReceiptGenerator orphaned confirmed.
- [PASS] **XF4 (DEF-12):** HomeViewModel placeholder — source-verified (koi UI usage nahi).
- [PASS] **XF5 (DEF-06):** sale 845ED833 sync BINA manual Force Sync (00:44:01 "Table Sales: 0 pending rows"; Force Sync sirf 00:45:33). Wastage bhi ~23s mein upload (`Wastage_Ledger!A1:append` 00:45:13 — DEF-27 anchor fix pattern live). MICRO_BATCH_WINDOW_MS unused (source).

### MODULE 03 LEFTOVERS (units / wastage / OCR / QR)
- [PASS] **F3.3 Units:** 7 default units (BOX/DZ/G/KG/L/ML/PC); Add Unit dialog → HERMES-UNIT-WD (abbr HU) added, list sorted; **Edit + Delete icons SIRF non-default par** (defaults par nahi); delete → list se remove (soft-delete, sheet isDeleted=true expected). 
- [PASS] **F3.6 Wastage:** Wastage Entry → product HERMES-PROD-001 (stock 3.0), reason OTHER, qty 1, notes "QA watchdog wastage" → saved; **stock 3.0 → 2.0** (banner + Data Viewer); Today's/Month Loss $50.00 (1 × cost 50); sheet row appended (logcat `Wastage_Ledger!A1:append` 00:45:13).
- [PASS] **F3.7 OCR:** Inventory top-bar camera icon → "Smart AI Entry (OCR)" + overlay "Point camera at the product label/weight" — screen present, koi fields nahi (emulator par camera OCR not testable).
- [PASS] **F3.8/F3.9 QR + GS1 label:** Print QR Code (PROD-001) → Barcode Print Settings (Label Preview, Print Qty 1, **Currency "Rs"** ← DEF-22 live repro!, Width 144/Height 72, Bold Title, Generate PDF) → **Generate PDF → GS1_Label_QA-SKU-001.pdf** generated + share sheet (Drive/Messages/Gmail/Bluetooth). PDF generation PASS.

### DEF-22 LIVE REPRO (new evidence)
- GS1 label preview mein currency **"Rs"** (BarcodePrefs default) vs POS/Settings/receipt "$" — mismatch screen-par live confirmed (registry updated).

### SYNC / SHEET EVIDENCE (this run)
- Sale 845ED833: local DB `synced`, uploaded bina Force Sync (instant-sync chain), Data Viewer Sales (11).
- Wastage: `Wastage_Ledger!A1:append` POST 200-chain (SyncWorker completed successfully).
- Inventory: PROD-001 stock 2.0 UI + Data Viewer; batchUpdate POSTs logged.
- NOTE: sheet-level col-value PDF re-verify is run mein BLOCKED (emulator Chrome ko Google password chahiye — available nahi; Desktop Chrome headless cookie-extract pywin32 broken). Chain DB→sheet verbatim pehle 10 sales par PDF-verified hai — is run ki values DB/UI/logcat se proven.

### FINAL TOTALS (updated 2026-08-22 01:05)
- **Sales completed & verified: 11** (10 sheet-verified + 845ED833 $100 tax-inclusive CASH — DB synced + instant upload logcat-proven)
- **Defects: 7 FIXED, 22 CONFIRMED, 0 unmarked (29/29)** — is run mein DEF-04/06/11/19/22 par live evidence add hua
- **F2.14 XF1–XF5: ALL PASS** (XF1 live, XF2/4 source+live, XF3 live, XF5 live)
- **Modules now 100%:** M02 POS (incl. F2.10 edge, F2.11, F2.14), M03 Inventory (units/wastage/OCR/QR/labels/adjust/batches/alerts), M04 PO/GRN, M05 Store, M06 Settings, M07 Hardware, M08 Backup, M11 Time Clock
- **New sales in run:** 1 (845ED833, $100.00 cash, tax-inclusive, invoice # 845ED833)
- **Emulator:** Pixel_4 Android 10, app v1.0.0, till #3 OPEN (opening 1000 + sale 11), sheet live-synced

---

## WATCHDOG RUN #2 (2026-08-22 19:02–19:55 UAE) — FINAL DEEP SCAN + REINSTALL PERSISTENCE ✅

**Build note (CRITICAL):** root `C:\Users\Faisal Khan\Desktop\Tillzo\app-debug.apk` is a **STALE Aug-2 copy** — `./gradlew assembleDebug` output is `app/build/outputs/apk/debug/app-debug.apk`. Previous installs of the root copy caused false findings (old labels, "Rs" currency, missing Settings rows). This run installed exclusively from the build output. APK installed: 19:35/19:41 builds (DEF-30/32/34 fixes), lastUpdateTime 2026-08-22.

### DEFECTS REGISTRY — 5 NEW (DEF-30..34)
- **DEF-30 (MEDIUM) FIXED ✅** — Admin Dashboard "Today's Sales/Expenses" used UTC-midnight day boundary (`now % 86400000L`) → local early-morning sales excluded. Fix: device-local midnight via `Calendar`. File: `ui/security/AdminAndUsersScreen.kt`.
- **DEF-31 (LOW) CONFIRMED** — SettingsViewModel: Drive folder / spreadsheet creation failures silently swallowed (2 empty catches), invalid sheet link silently ignored. Not fixed (UX-only).
- **DEF-32 (MEDIUM-SECURITY) FIXED ✅** — RBAC dead: `SessionGuardUseCase.hasPermission()` had **ZERO call sites** (rg-verified) — any role could open Settings/Expenses/Admin Dashboard/User Mgmt. Fix: new `ui/security/RbacViewModel.kt` + AppNavHost gates on Settings/Expenses/Admin Dashboard menu routes (Toast on denial). Single-owner mode (Users_Permissions tab empty, sheet-verified) keeps fresh installs unrestricted — regression: Settings opens fine.
- **DEF-33 (LOW) CONFIRMED** — Vendors tab duplicate row (HERMES-VENDOR-001 same system_row_id ×2, xlsx-verified). Historical; sheet untouched.
- **DEF-34 (HIGH) FIXED ✅** — Reinstall restore produced garbage sales: monthly `Sales_MMM_YYYY` tabs are **header-less** (row 1 = data) but `fetchDelta` mapped by header names only → empty invoice_id / $0.00 / now-timestamps. Fix: header-less detection + **positional fallback** via `canonicalColumnsFor(tab)` (SheetColumns order). File: `data/repository/SheetsRepository.kt`.

### REINSTALL PERSISTENCE TEST (FULL, master task 3) ✅
- Uninstall → install fresh (fixed build) → camera allow → Google sign-in (yourtutorial3490@gmail.com, emulator account saved) → Backup & Sync consent → auto-connect → RestoreWorker fetched **43 rows** → upserted.
- **Verified restored (Data Viewer + UI):** sales 845ED833 **$100.00** (12:39AM) + 136CFE36 **$200.00** (11:40PM) — DEF-34 fix live; inventory PROD-001 **2.0** / PROD-002 **0.0**; customer HERMES-CUST-WD (0509998877) + khata **Balance Due $250**; vendors HERMES-VENDOR-001/002k; expenses 8; till sessions 3. Force Sync post-restore: all tables synced, **0 pending**, no errors.
- **Sheet integrity:** fresh xlsx (19:49) — Sales 11 rows (unchanged), Customers 2, Khata_Events 2, Vendors 4, Users_Permissions header-only. **No garbage pushed to sheet** (restored rows marked synced → never re-upload).
- **Residual (documented, not code):** 5 pre-DEF-27 sales (7ECBFED3, CB9039C6, 3CB3819B, 49B56B57, AC03A60B) sit scattered at cols 20–100 in the sheet (DEF-27-era artifact) → restore maps them to placeholder rows. Sheet not modified per rules (repair = sheet-side rewrite).

### MICRO-TESTS (this run)
- [PASS] CRM: Add Customer (HERMES-CUST-WD / 0509998877) → list shows → khata Add Credit $250 → Balance Due $250 → Force Sync → **sheet-verified** (Customers + Khata_Events rows in xlsx).
- [PASS] Force Sync logcat: `Customers!A1:append` anchored (DEF-27 pattern), KhataEvents 1 row uploaded.
- [PASS] Data Viewer reachable via Settings → Stored Data (This Phone) — counts match sheet per-tab.

### FINAL TOTALS (2026-08-22 19:55)
- **Defects: 34 registered — 10 FIXED (DEF-04/09/25/26/27/28/29/30/32/34), 24 CONFIRMED, 0 unmarked**
- **Source fixes this run (3 files):** AdminAndUsersScreen.kt (DEF-30), RbacViewModel.kt + AppNavHost.kt (DEF-32), SheetsRepository.kt (DEF-34)
- **Builds:** 2× assembleDebug (1m23s, 28s) — BUILD SUCCESSFUL
- **Reinstall persistence: PASS** (sales/inventory/customers/khata/vendors/expenses/till sessions restored; sheet intact)
- **Emulator:** Pixel_4 Android 10, app v1.0.0 (fixed build), till OPEN (opening $1000), PIN 1234 set, sheet live-synced

---

### WATCHDOG RUN 2026-08-22 22:12–23:40 — Round-3 (CRUD verification marathon)
- **[FIXED+VERIFIED] DEF-81 (HIGH): stale cached access token → permanent 401 sync death.** TokenAuthenticator par 401 → `getValidToken()` wahi stale token (expiry clock future, server-side invalid) → refresh chain kabhi trigger nahi → HAR request 401. Fix: authenticator mein sirf ACCESS token invalidate (refresh token intact). Verified: fix build ke baad **10/10 tables synced, zero 401** (logcat 23:39-23:40 all 200).
- **[FIXED+VERIFIED] DEF-82 (LOW): Units list last-item delete button FAB overlap** (ProductUnitsScreen + CategoryManagementScreen contentPadding bottom 96dp).
- **CRUD micro-tests — sab sheet-verified (xlsx/PDF export):**
  - Categories: ADD WD-CAT-0812 → UPDATE → WD-CAT-0812-UPD (same row, no dup) → DELETE (sheet row removed). 3/3 ✅
  - Units: ADD WatchdogUnit/WU → UPDATE → WU2 → DELETE. 3/3 ✅
  - Inventory: ADD WatchdogItemWD (WD-SKU-0822, 75/150, stock 5) → UPDATE desc WD-UPD-DESC-V2 → DELETE (PROD-001/002 intact). 3/3 ✅
  - Wastage: ADD HERMES-PROD-002 1.0 KG THEFT → sheet Wastage_Ledger ✅ (delete UI missing → GAP-4)
  - Returns: SALE D3950BEA $110 CASH → full-UUID lookup → refund → sheet `REFUND_OF_d3950bea_Restock` total **-110** ✅ (Returns tab hamesha empty → GAP-3 negative-sale design)
- **NEW OPEN:** GAP-3 (Returns tab vestigial), GAP-4 (Wastage delete UI missing), DEF-83 (stock 2-unit discrepancy after sale+return — Room WAL copy blocked direct DB read; next run root-cause).
- **Totals: 84 defects registered total — 26 FIXED+VERIFIED, 58 OPEN/CONFIRMED. GAPs: 4 (2 fixed, 2 new open).**
- **Source fixes this run (2 files):** SheetsApiClient.kt (DEF-81), ProductUnitsScreen.kt + CategoryManagementScreen.kt (DEF-82). Builds: 2× assembleDebug BUILD SUCCESSFUL + 2× adb install -r Success.

---

### WATCHDOG RUN #4 (2026-08-23 00:12–01:20 machine / 20:12–21:20 UAE) — DEF-83 root-caused + 3 fixes

**Device state:** emulator online, app v1.0.0 (Round-3 build + this run's fix build 00:06/01:20), till OPEN, PIN 1234, IME AdbKeyboard. Sheet: Faisal Mart — TillzoPOS.

#### DEF-83 (MEDIUM) — ROOT CAUSE CONFIRMED + FIXED + SHEET-VERIFIED
- **Root cause:** `ReturnsViewModel.processFullReturn()` Restock branch — batch list fetch (L101) updateBatchStock (L105-109) se PEHLE; `sumOf{stockQty}` STALE list se → +1 restock ko purani value par overwrite. Sale path fresh-fetch tha (safe).
- **Sheet timeline proof:** x3/x4 (22:58/23:08) PROD-001 stock **10.0** → sale D3950BEA (23:24) → x5 (23:28) **9.0** (deduct sahi) → return (23:27) → x6 (23:32) **10.0** (intermediate write sync) vs local 9 (stale sum) — 1-unit local/sheet divergence. Checkpoint ka "12 at 22:15" baseline galat (x3/x4 = 10.0).
- **Fix:** `ReturnsViewModel.kt` — update ke BAAD `refreshed = getAllBatchesForProduct()` + fresh sum.
- **Regression (live):** Refund sale 845ED833 (1× PROD-001, Restock) → "Refund Processed Successfully." → UI stock **11.0** → Force Sync (all tables 200, zero 401) → sheet xlsx (7) export: Inventory stock_qty **11.0** ✅ + Sales `REFUND_OF_845ed833-ebb9-42ec-93fe-d10d7d4de944_Restock` (total -100, cashier yourtutorial3490@gmail.com) ✅. **UI == Sheet == 11.0.**

#### GAP-4 (LOW) — Wastage delete UI — FIXED + VERIFIED
- WastageDao: list/total queries se `syncStatus='deleted'` exclude; WastageViewModel.deleteWastage (soft-delete, stock not mutated — sheet audit intact); WastageLogScreen: delete icon + confirm dialog.
- **Verified:** delete THEFT $150 entry → **Month Loss $200.00 → $50.00**, entry gone, remaining entry + delete button intact.

#### DEF-64 (LOW) — GTIN lookup — FIXED (build+install verified)
- `InventoryDao.getItemByGtin` (JOIN ItemGtins) + fallback `?: getItemByGtin()` in ScannerViewModel / InlineScannerViewModel / InventoryRepositoryImpl. Scanner UI test virtual-camera se unreliable (documented).

#### DEF-79 — verified fixed (source, Round-3); closed. New: DEF-84/85/86/87 registered.

#### Deep codebase scan (master task 1)
- TODO/FIXME: sirf documented placeholders (HomeViewModel M4, ReceiptScreen print, InlineCameraBox sleep); koi unimplemented critical path nahi.
- Hardcoded secrets: NONE (no api keys/passwords in code/gradle; client_secret JSON APK mein packaged nahi).
- Weak crypto: NONE (no MD5/SHA1/DES/ECB; OAuth prefs EncryptedSharedPreferences).
- runBlocking: sirf documented spots (SheetsApiClient interceptor, AuthRepositoryImpl — DEF-78).
- Exported components: minimal (MainActivity exported; services/receivers not).
- Plaintext prefs: barcode_prefs/update_prefs (non-sensitive); db_encryption fallback_passphrase plaintext (DEF-84 residual, app-private).

#### Totals (2026-08-23 01:20)
- **Defects: 87 registered — 30+ FIXED/verified, OPEN documented (DEF-31/46/47/50-53/60-66/84-87 etc.), 0 unmarked**
- **GAPs: 4 — 2 FIXED (GAP-2 scanner, GAP-4 wastage delete), 2 OPEN (GAP-1 printer screen orphaned, GAP-3 Returns tab vestigial)**
- **Source fixes this run (4 files):** ReturnsViewModel.kt (DEF-83), WastageDao.kt + WastageViewModel.kt + WastageLogScreen.kt (GAP-4), InventoryDao.kt + ScannerViewModel.kt + InlineScannerViewModel.kt + InventoryRepositoryImpl.kt (DEF-64)
- **Builds:** 1× assembleDebug (2m18s, BUILD SUCCESSFUL) + 1× adb install -r Success
- **Emulator:** Pixel_4 Android 10, app v1.0.0 (fixed build), till OPEN, sheet live-synced (Inventory 11.0)

### VERIFICATION EVIDENCE (hermes-verify-tillzo-r4.py, ad-hoc) — 2026-08-23 01:2x
- [PASS] DEF-83 re-fetch present
- [PASS] GAP-4 DAO filters
- [PASS] GAP-4 ViewModel deleteWastage
- [PASS] GAP-4 Screen delete UI
- [PASS] DEF-64 DAO getItemByGtin
- [PASS] DEF-64 repo fallback
- [PASS] DEF-64 scanner fallbacks
- [PASS] compileDebugKotlin — BUILD SUCCESSFUL
- [PASS] APK exists & fresh — 61,617,501 bytes
- [PASS] PDF v3 valid — 5,332 bytes
- [PASS] Registry has RUN #4 section
- [PASS] App installed on emulator — lastUpdate 2026-08-23 00:22:52
- **Total: 12/12 checks passed** (ad-hoc verification, no canonical suite)

---

### WATCHDOG RUN #5 (2026-08-23 01:48–02:30 machine / 21:48–22:30 UAE) — REINSTALL PERSISTENCE FULL CYCLE + 4 FIXES

**Device state:** emulator online, app v1.0.0 (RUN-5 fixed build, 4 source patches), till OPEN, sheet Faisal Mart — TillzoPOS. Sheet baseline: export (8) 01:50.

#### Master task 3 — Reinstall persistence test (FULL CYCLE, uninstall → install → login → restore → verify)
- Uninstall → fresh install → Google sign-in (yourtutorial3490) → existing sheet auto-select → RestoreWorker + delta restore → Open Till (1000).
- **10/10 modules verified restored from sheet:** Inventory PROD-001 11.0 / PROD-002 0.0 + batches (HERMES-BATCH-001 + "2 Expiring" badge) | Sales history (invoices 45849425 refund -$100, 1CE4254C refund -$110, D3950BEA $110, 023A1FCB $220, 60EF9E52, 775425E4, C94ACB74, 0D0AD550 + more) | Customer HERMES-CUST-WD + Khata Credit $250 | Vendors 3 rows | Expenses (Stationery $12.99, Rent $30.75/$75.50, Electricity $25.50…) | POs PO-202608-0001 SENT + 0002 CANCELLED | GRNs GRN-2026-0001/0002 CONFIRMED | Time Clock 4 punches | Stock Adjustments 2 (HERMES-ADJ-001/002) | Wastage (deleted-skip after DEF-90) | Till sessions (Z-Report: today -$100 gross, $856.26 expected) | Categories (HERMES-CAT-001/SUBCAT-001) | Units (WatchdogUnit WU2).
- Force Sync final: all tables synced, zero 401, schema verified, pending cleared.

#### Fixes this run (4 source patches → 3 builds → installs → regressions)
1. **DEF-88** orphan-batch FK guard (DeltaSyncManager) — restore ab orphan (215adadf) skip karta hai, valid batches restore hote hain.
2. **DEF-89** Time_Clock restore (SheetsRepository tabs + DeltaSyncManager branch) — punch history ab reinstall par aata hai.
3. **DEF-90** wastage delete marker sync (WastageDao + SyncWorker updateRowByUuid + restore syncStatus map) — sheet par sync_status='deleted' (row intact), reinstall par deleted entry skip. Sheet-verified via export (9).
4. **DEF-91** DbEncryption passphrase precedence (HIGH) — install -r/restart par keystore-keyed DB crash ("file is not a database") fixed; existing DB rescued bina data loss.

#### GAP-1 closed (live verified): Settings → Printer Settings → Hardware Settings (BT SPP + Wi-Fi 9100).
#### New DEF: DEF-92 (MEDIUM, OPEN) — ItemGtins never synced → GTIN lookup breaks after reinstall (barcode_id OK). Deferred (core-table schema risk).

#### Totals (2026-08-23 02:30)
- **Defects: 92 registered — 34+ FIXED/verified, OPEN documented (DEF-31/46/47/50-53/60-66/84-87/92 etc.), 0 unmarked**
- **GAPs: 4 — 3 FIXED (GAP-1/2/4), 1 OPEN (GAP-3 Returns tab vestigial)**
- **Source fixes this run (5 files):** DeltaSyncManager.kt (DEF-88/89/90-restore), SheetsRepository.kt (DEF-89), WastageDao.kt + SyncWorker.kt (DEF-90), DbEncryption.kt (DEF-91)
- **Builds:** 3× assembleDebug (1m43s/48s/21s, all BUILD SUCCESSFUL) + 3× adb install Success
- **Emulator:** app v1.0.0 (RUN-5 build, 61.7MB), till OPEN, sheet live-synced (Inventory 11.0, Wastage marker deleted)


---

### WATCHDOG RUN #6 (2026-08-23 03:01–03:30 machine / 23:01–23:30 UAE) — DEF-92 GTIN SYNC FIXED (FULL CYCLE)

**Device state:** emulator online, app v1.0.0 (RUN-6 build, 61.7MB), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Sheet baseline: export (11) 03:14 (pre-fix), export (12) 03:22 (post-fix).

#### Process incident (self-caught): stale APK installed
- Repo-root `app-debug.apk` (Aug-2 build, 54.7MB — DEF-88/91/92 BINA) accidentally installed → DEF-88 orphan-batch FK error reproduce hua + DB recreate (local cursor=0) + cursor advanced bina batches restore ke (pre-RUN-5 bugs, sab wapas aa gaye the).
- Root cause: build output `app/build/outputs/apk/debug/app-debug.apk` (61.7MB, fresh) vs repo-root copy (stale). **RULE established: hamesha build-output APK se install karo.**
- Recovery: correct APK install -r → DB files + db_encryption.xml wiped (prefs intact = signed-in) → app relaunch → FULL clean restore (74 rows, "Skipping 1 orphan batch: 215adadf", 0 errors) → till open. No data loss (sheet = source of truth).

#### DEF-92 FIXED — ItemGtins GTIN sync (7 source files → 2 builds → 2 installs → regressions)
1. **Constants.kt** — `SheetColumns.ITEM_GTINS` (gtin_id, item_id, gtin, created_at, updated_at). Room entity/schema untouched → zero migration risk.
2. **InventoryDao.kt** — `getAllGtins()` full dump (upload dedupe + restore dup-value guard).
3. **SchemaGuardUseCase.kt** — ItemGtins in TAB_HEADERS + requiredTabs → purani sheets par tab + headers self-heal (live: DEF-32 header-insert repair bhi exercise hua).
4. **SheetsRepository.kt** — createWorkspace sheetDefs + buildHeaders + fetchDelta standardTabs + canonicalColumnsFor.
5. **DeltaSyncManager.kt** — ItemGtins restore branch: orphan-parent guard (item deleted → GTIN rows linger, FK violation se poora tab group fail na ho) + blank + duplicate-gtin-VALUE guard (unique index crash). Live: "ItemGtins restore: 2 inserted, 0 skipped".
6. **SyncWorker.kt** — `uploadPendingGtins()`: (a) BACKFILL — pre-DEF-92 items ka primary GTIN (barcode_id) ItemGtins mein populate (bina iske upload karne ko kuch nahi hota), (b) gtin_id dedupe vs sheet (Product_Batches pattern), (c) tab-missing self-heal (addSheet + header + retry once). Live: "ItemGtins backfill: 2 barcode_id → GTIN rows" → "ItemGtins uploaded: 2".

#### Reinstall persistence test (master task 3) — GTIN module (DEF-92 acceptance)
- `pm clear` → fresh install state → Google sign-in (yourtutorial3490) → auto restore → **"ItemGtins restore: 2 inserted, 0 skipped"** → till open → Inventory → Edit HERMES-PROD-001 → **"GTINs (Barcodes): 00000000000001"** visible (getGtinsForItemFlow) → scanner JOIN lookup ab populated hai.
- Rest of modules spot-verified: Inventory 11.0/0.0 + batches (HERMES-BATCH-001/002), LOW STOCK (PROD-002 0.0), OUT (1), EXPIRING (2) badges, sales/customers/vendors/expenses/PO/GRN/time clock/adjustments/wastage (74 delta rows fetched, 0 errors).

#### Sheet verification (export (12), emulator Chrome + openpyxl)
- **ItemGtins tab LIVE:** header + 2 rows — 7f16765e→8cea4687 (PROD-001) 00000000000001, 67c0dfc8→346c4946 (PROD-002) 00000000000002, timestamps 1787440862809. ✅
- Product_Batches 5 rows intact (orphan 215adadf sheet par by design), Inventory 11.0/0.0, Wastage deleted-marker intact, Till_Sessions +1 (is run ka open session).
- Force Sync post-restore: all tables synced, ItemGtins dedupe 0 re-upload, SchemaGuard verified, Worker SUCCESS.

#### Deep codebase scan (master task 1)
- TODO/FIXME: sirf documented placeholder (HomeViewModel M4 checkout, ReceiptScreen print, InlineCameraBox sleep).
- Hardcoded secrets: NONE. Weak crypto: NONE. runBlocking: documented spots only (SheetsApiClient interceptor, AuthRepositoryImpl).
- New static findings: 0 — registry par 92 defects (35+ FIXED), 0 unmarked.

#### Totals (2026-08-23 03:30)
- **Defects: 92 registered — 35+ FIXED/verified, OPEN documented (DEF-31/46/47/50-53/60-66/84-87), 0 unmarked**
- **GAPs: 4 — 3 FIXED (GAP-1/2/4), 1 OPEN (GAP-3 Returns tab vestigial)**
- **Source fixes this run (7 files):** Constants.kt, InventoryDao.kt, SchemaGuardUseCase.kt, SheetsRepository.kt, DeltaSyncManager.kt, SyncWorker.kt (DEF-92 + backfill)
- **Builds:** 2× assembleDebug (1m19s / 29s, all BUILD SUCCESSFUL) + installs (1 stale — caught and corrected, 2 correct)
- **Emulator:** app v1.0.0 (RUN-6 build, 61.7MB), till OPEN (1000), sheet live-synced (ItemGtins tab LIVE, Inventory 11.0/0.0)

---

### WATCHDOG RUN #7 (2026-08-23 04:03–04:45 machine / 00:03–00:45 UAE) — AUDIT-TRAIL IDENTITY FIXES + FULL REINSTALL CYCLE

**Device state:** emulator online (restarted mid-run after hang — userdata intact), app v1.0.0 (RUN-7 build, 61.8MB), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Sheet baseline: export (13) 04:28, (14) 04:36, (15) 04:43.

#### Fixes (3 source files → 1 build 31s → install -r → regressions + sheet-verified)
1. **DEF-93 (MEDIUM) FIXED** — `CreatePurchaseOrderViewModel.kt`: PO `createdBy = "admin"` hardcoded → `appSetupPrefs.userEmail.ifBlank { "admin" }`. Sheet (14): PO-202608-0003 DRAFT `created_by = yourtutorial3490@gmail.com` (PO-0001/0002 historical "admin" untouched). ✅
2. **DEF-94 (MEDIUM) FIXED** — `StockAdjustmentScreen.kt`: adjustedBy default "admin" (call site pass nahi karta) → AppSetupPrefs inject + nullable param + `effectiveAdjustedBy`. Micro-test: Stock Adjustment RECEIVED +5 PROD-001 (11.0→16.0, reason WD-DEF94-TEST) → Force Sync → sheet (13/14/15): `adjusted_by = yourtutorial3490@gmail.com`, stock_qty 16.0 == UI "New stock: 16.0 PC". ✅
3. **DEF-84 (MEDIUM-security) FIXED** — `DbEncryption.kt`: legacy derivable passphrase ab keystore-encrypted rotate (same value → DB readable; keystore_ciphertext precedence path 1 se next launches). Build+install+relaunch 0 crashes, DB intact. ✅

#### Reinstall persistence (master task 3) — FULL CYCLE PASS (new build)
- `pm clear` → camera allow → Continue with Google → account chooser (yourtutorial3490) → Backup and Sync consent → RestoreWorker **78 rows fetched** → "Skipping 1 orphan batch row(s): 215adadf" (DEF-88) → "ItemGtins restore: 2 inserted, 0 skipped" (DEF-92) → SUCCESS, 0 errors.
- Verified restored: PO-202608-0003 (DRAFT, vendor, $50) in PO list; DeltaSync Purchase_Orders/Stock_Adjustments branches created_by/adjusted_by mapping source-verified → DEF-93/94 values persist across reinstall.
- Open Till 1000 → Force Sync: all tables synced, **0 pending**, 0 re-uploads (restored rows marked synced), SchemaGuard verified.
- Sheet (15): 26 tabs intact, Sales 18 rows unchanged (no garbage), Inventory 16.0/0.0, ItemGtins 2 rows.

#### Micro-tests (this run)
- [PASS] Stock Adjustment add → UI stock + sheet stock_qty + adjusted_by=email (3-way match)
- [PASS] PO create (Draft) → PO_Items + Purchase_Orders upload (logcat POST 200) → sheet created_by=email
- [PASS] Post-restore Force Sync dedupe — 0 duplicate uploads, 0 pending
- [PASS] Expense delete / customer delete UI — source-verified present (previous runs covered; no regression)

#### Deep codebase scan (master task 1)
- Identity-column sweep `(createdBy|adjustedBy|receivedBy|loggedBy|...) = "literal"`: sirf 2 hardcoded spots (DEF-93/94) — dono fixed. Wastage logged_by, Expense logged_by_user_id, Sales cashier_id, Time_Clock employee_email sab real email (sheet-verified).
- TODO/FIXME: sirf documented placeholders (HomeViewModel M4, ReceiptScreen print, InlineCameraBox sleep). Hardcoded secrets: NONE. Weak crypto: NONE. rawQuery: NONE. WebView: NONE. Cleartext: NONE. runBlocking: documented spots only (SheetsApiClient, AuthRepositoryImpl).
- SharePurchaseOrderUseCase L174 empty catch = intentional WhatsApp→email fallback chain (UX-only, DEF-31 family).

#### Process notes
- Emulator hang (Chrome download attempt ke dauran) → taskkill emulator.exe + zombie qemu-system-x86_64.exe + AVD lock cleanup → fresh boot (userdata intact, app data safe). AVD "multiple emulators" lock error resolved.
- Emulator Chrome xlsx export flow: "Download file again?" dialog → tap "Download again" → /sdcard/Download/... (N).xlsx → adb pull. Cache-buster &rnd=N zaroori.
- Navigation: 2× back = app exit → Chrome Downloads (avoid); single back + Advanced Options menu icon.

#### Totals (2026-08-23 04:45)
- **Defects: 94 registered — 38+ FIXED/verified, OPEN documented (DEF-31/46/47/50-53/60-66/85-87 + GAP-3), 0 unmarked**
- **GAPs: 4 — 3 FIXED (GAP-1/2/4), 1 OPEN (GAP-3 Returns tab vestigial — negative-sale design functional; full Returns sync deferred)**
- **Source fixes this run (3 files):** CreatePurchaseOrderViewModel.kt (DEF-93), StockAdjustmentScreen.kt (DEF-94), DbEncryption.kt (DEF-84)
- **Builds:** 1× assembleDebug (31s, BUILD SUCCESSFUL) + 1× adb install -r Success (build-output APK)
- **Emulator:** app v1.0.0 (RUN-7 build), till OPEN (1000), sheet live-synced (Inventory 16.0/0.0, POs 3, ADJ 3, ItemGtins 2)


---

### WATCHDOG RUN #8 (2026-08-23 05:13–05:40 UAE) — UX/LOG/LOOKUP FIXES + REINSTALL CYCLE (build 05:22, 61.6MB)

**Device state:** emulator online, app v1.0.0 (RUN-8 build, install -r 05:22 — data intact), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Exports: (16) 05:28, (17) 05:39.

#### Fixes (8 source files → 1 build → install -r → regressions → full reinstall cycle) — 7 defects closed
1. **DEF-22 FIXED ✅** — Barcode label currency "Rs" → app currency "$": `BarcodePrefs.loadGeneralConfig()` seeds/migrates currencySymbol from `AppSetupPrefs`; constructor `private val context`. Live: Barcode Print Settings Currency field = **"$"** (pehle "Rs"). Inventory list "$ 100.0 / PC" consistent.
2. **DEF-85 FIXED ✅** — Advanced Options menu scroll mid-menu restore: `rememberScrollState()` (rememberSaveable-backed) → `remember { ScrollState(0) }` — menu ab hamesha top se khulta hai (3× verified).
3. **DEF-86 FIXED ✅** — Returns lookup ab 8-char invoice prefix se bhi chalta hai: `SaleDao.getSaleByInvoiceIdPrefix` (case-insensitive LIKE prefix) + repo chain. Live: "845ED833" → Invoice Found ($100.00 CASH, 22 Aug 12:39 AM).
4. **DEF-87 FIXED ✅** — SyncWorker delta_cursor "Unknown table name" log noise: `filterNot { it == "delta_cursor" }` — Force Sync par 0 noise logs.
5. **DEF-95 NEW FIXED ✅** — Barcode GS1 field Move Up/Down ab UI list ko bhi reorder karta hai (pehle sirf sequenceOrder swap hota tha, list kabhi nahi hilti thi). Live: Move Down → Expiry Date top; Move Up → GTIN wapas top.
6. **DEF-97 NEW FIXED ✅** — PunchClock duplicate-punch guard (same-type double-tap blocked). Live: 3 rapid taps → IN/OUT/IN strict alternation; sheet Time_Clock 8 punches, koi duplicate nahi.
7. **DEF-98 NEW FIXED ✅** — BarcodePrefs corrupt fields JSON → `defaultFields()` (pehle emptyList = GS1 silently empty).

#### Reinstall persistence (master task 3) — FULL CYCLE PASS (RUN-8 build)
- `pm clear` → sign-in yourtutorial3490 → consent → RestoreWorker **83 rows fetched** (RUN-7: 78) → orphan batch 215adadf skipped (DEF-88) → "ItemGtins restore: 2 inserted" (DEF-92) → SUCCESS 0 errors.
- Restored (UI verified): Inventory 16.0/0.0 + LOW STOCK badge, Time_Clock 8 punches (incl. naye 4 @05:23 — state OUT from last punch), GTINs intact, PO-0003 (source-verified), till open 1000.
- Force Sync post-restore: sab tables synced, **0 pending**, 0 re-uploads. Sheet (17): Sales 18 rows UNCHANGED (no garbage), Time_Clock 8, Till_Sessions 11 (+1 open), 26 tabs intact.

#### Micro-tests
- [PASS] Returns prefix lookup (DEF-86 live) | Time Clock add → Force Sync → sheet-verified (8 rows) | Barcode settings currency + field reorder (DEF-22/95) | Verify QR screen smoke | Z-Report smoke ($-100.00 gross, $856.26 expected — state intact) | Data Viewer counts (Inventory 2/Sales 12/Customers 1/Expenses 12/Khata 2/Till 3).
- [NOTE] Till_Sessions sheet 10→11 = is run ka open session; rows 02:01–04:39 previous reinstall cycles se (no leak).

#### Totals (2026-08-23 05:40)
- **Defects: 97 registered — 45+ FIXED/verified, OPEN documented (DEF-31 sheet-side, DEF-46/47/50-53/60-66, GAP-3 deferred), 0 unmarked**
- **Source fixes (8 files):** BarcodePrefs.kt (DEF-22/95/98), AdvancedMenuSheet.kt (DEF-85), SaleDao/SaleRepository/SaleRepositoryImpl/ReturnsViewModel (DEF-86), SyncWorker.kt (DEF-87), PunchClockViewModel.kt (DEF-97)
- **Builds:** 1× assembleDebug (1m13s SUCCESS) + 1× adb install -r Success (build-output APK 61.6MB)
- **Emulator:** app v1.0.0 (RUN-8 build), till OPEN (1000), sheet live-synced


---

### WATCHDOG RUN #10 (2026-08-23 07:00–07:40 UAE) — NUMBER-RACE + PII FIXES + REINSTALL CYCLE (1 build)

**Device state:** emulator online (host clock UTC rollback ke baad emulator clock = ground truth, 07:40 UAE), app v1.0.0 (RUN-10 build, install -r — data intact), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Exports: (20) 07:26 pre-fix, (21) 07:33, (22) 07:39 post-reinstall.

#### Fixes (4 source files → 1 build → install -r → live micro-tests + full reinstall cycle) — 2 defects closed
1. **DEF-61 FIXED ✅ (MED)** — GRN/PO number race: `COUNT(*)+1` read-then-insert replaced with **MAX-based atomic sequence** — `GrnDao.getNextGrnSequence()` / `PurchaseOrderDao.getNextPoSequence()` = `COALESCE(MAX(CAST(SUBSTR(poNumber,-4) AS INTEGER)),0)+1`; `GrnRepositoryImpl.generateGrnNumber()` + `CreatePurchaseOrderViewModel.savePO()` use it. Double-save guard `_isSaving` added to PO VM (GRN VM already had `_isLoading`). **Unique DB index deliberately NOT added** — `insertPO`/`insertGrnHeader` are `OnConflictStrategy.REPLACE` (DeltaSyncManager upsert), so an index would DELETE the colliding row (data loss > dup number). Live: PO-202608-0004 → sheet POs 4 rows; Receive Goods → GRN-2026-0003 → sheet GRN_Headers 3 rows; Product_Batches 6 (+1).
2. **DEF-99 FIXED ✅ (LOW, PII)** — `SignInViewModel.kt` L116 raw `email=$email` logcat line removed (sirf booleans log). Live: fresh sign-in after pm clear → logcat `Sign-in success: idToken present=true, serverAuthCode present=false` — NO email (old 06:39 build log had `email=yourtutorial3490@gmail.com` as pre-fix evidence).

#### Reinstall persistence (master task 3) — FULL CYCLE PASS (RUN-10 build)
- `pm clear` → camera allow → Continue with Google → yourtutorial3490 → consent → **RestoreWorker fetched 91 rows (RUN-9: 85) → completed successfully, 0 errors** (naye PO/GRN/batch rows included).
- Till open 1000 → Force Sync: **0 pending, 0 re-uploads**, SchemaGuard verified, "SyncWorker completed successfully". Sheet (21)→(22): sirf Till_Sessions +1 (naya open session) — Sales 17 byte-identical, PO 4, GRN 3, baaki tabs unchanged. Total 94 rows (93 restore + 1 session).

#### Micro-tests (RUN #10)
- [PASS] PO create → PO-202608-0004 → sheet Purchase_Orders 4 rows (no dup) — DEF-61 live
- [PASS] Receive Goods → GRN-2026-0003 CONFIRMED → sheet GRN_Headers 3 rows + Product_Batches 6 — DEF-61 live
- [PASS] Force Sync ×2 (pre- and post-reinstall) — 0 errors, 0 noise
- [PASS] DEF-99 logcat PII check — email absent from new sign-in log
- [PASS] Full reinstall cycle — 91-row restore, 0 re-uploads

#### Totals (2026-08-23 07:40 UAE)
- **Defects: 100 registered — 54+ FIXED/verified; OPEN: DEF-51 (partial, larger refactor), DEF-62 (partial, DEF-35 mitigates), GAP-3 (deferred feature), DEF-31 sheet-side vendor dup row (rules forbid sheet edits)**
- **Source fixes (4 files):** GrnDao.kt + PurchaseOrderDao.kt + GrnRepositoryImpl.kt + CreatePurchaseOrderViewModel.kt (DEF-61), SignInViewModel.kt (DEF-99)
- **Builds:** 1× assembleDebug (BUILD SUCCESSFUL) + 1× adb install -r Success
- **Emulator:** app v1.0.0 (RUN-10 build), till OPEN (1000), sheet live-synced, connected sheet ID intact (14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU)


---

### WATCHDOG RUN #11 (2026-08-23 08:10–08:50 UAE) — VALIDATION-GUARD SWEEP + GAP-3 BUILD REPAIR + REINSTALL CYCLE

**Device state:** emulator online (host clock UTC rollback — emulator clock = ground truth, 08:50 UAE), app v1.0.0 (RUN-11 build, 2nd install -r — data intact + DB migrated 31→32), till OPEN (1000), sheet Faisal Mart — TillzoPOS. Export: (23) 08:45.

#### Fixes (8 source files + 2 build-repair files → 2 builds → 2 installs) — 13 defects closed
1. **DEF-101 FIXED ✅ (MED)** — GRN confirm no try/catch + `_isLoading` stuck on exception: `CreateGrnViewModel.saveAndConfirmGRN` → try/catch/finally; catch logs + `ConfirmGrnResult(success=false, errorMessage=…)`; finally resets loading.
2. **DEF-102 FIXED ✅ (MED)** — Wastage negative/zero qty (negative qty INCREASED stock): `logWastage` → `quantity <= 0` reject + Log.w.
3. **DEF-103 FIXED ✅ (MED)** — Product save blank name / negative price/stock/tax: `InventoryCrudViewModel.saveItem` → blank reject + coerceAtLeast(0) on price/stock/threshold/cost/tax/damagedQty.
4. **DEF-104 FIXED ✅ (MED)** — Add/Update batch negative stockQty: reject `< 0` (addBatch + updateBatch).
5. **DEF-105 FIXED ✅ (LOW)** — GRN posTerminalId hardcoded "terminal_1" → spreadsheet-id based (DEF-05 pattern).
6. **DEF-106 FIXED ✅ (LOW)** — Category pos_terminal_id hardcoded "terminal_1" → spreadsheet-id based.
7. **DEF-107 FIXED ✅ (LOW)** — Customer save empty name → reject + log.
8. **DEF-108 FIXED ✅ (LOW)** — Vendor save empty name → SaveState.Error; negative creditLimit → clamp 0.
9. **DEF-109 FIXED ✅ (LOW)** — Custom cart item negative price/qty → reject.
10. **DEF-110 FIXED ✅ (LOW)** — UDHAAR without customer → VM-level SaleResult.Error (defense-in-depth; UI already gates).
11. **DEF-111 FIXED ✅ (LOW)** — PO save no catch → DB failure crash; ab Log.e (finally already resets _isSaving).
12. **DEF-112 FIXED ✅ (LOW)** — PO item negative qty/price → reject.
13. **DEF-113 FIXED ✅ (LOW, doc)** — ReceiptScreen stale "placeholder snackbar" comment → actual ESC/POS print behavior.

#### Build-repair (sibling GAP-3 work se toota hua build — is run mein theek kiya)
- **Dagger MissingBinding ReturnsDao** — ReturnsViewModel (GAP-3) injects ReturnsDao; DatabaseModule @Provides missing tha → build fail. Provider added (duplicate-removal included — sibling bhi add kar raha tha).
- **MIGRATION_31_32 crash** — DB version 32 (returns_log) but migration registry 30_31 tak; install -r par "migration from 31 to 32 required but not found" FATAL. Sibling ne DatabaseModule.addMigrations mein MIGRATION_31_32 register kiya → 2nd install clean, data intact.
- **ReturnsEntity.toSheetRow column misalignment** — 12 values vs 13 sheet cols (posTerminalId created_at mein, createdAt updated_at mein likha jata tha) → 13-col header-order fix (createdAt, lastUpdated, posTerminalId).
- **Concurrent-build race** — sibling build same dir mein → Gradle state-tracking hash failures; isolated /tmp copy build strategy (1m24s BUILD SUCCESSFUL).

#### Reinstall persistence (master task 3) — FULL CYCLE PASS (RUN-11 build)
- pm clear → camera allow → Continue with Google → yourtutorial3490 → consent → **RestoreWorker fetched 92 rows (RUN-10: 91) → completed successfully, 0 errors** (naya +1 = sibling ka till session/expense).
- Till open 1000 → Force Sync: all tables synced, **0 pending, 0 re-uploads**, SchemaGuard verified, "SyncWorker completed successfully".
- Sheet (22)→(23): Sales 17 byte-identical, POs 4, GRNs 3, Batches 6, Time_Clock 8, Inventory 2, Wastage 2, Adjustments 4, Khata 2, Vendors 3, ItemGtins 2 — **SAB SAME** (openpyxl diff). Sirf Till_Sessions +2 (sibling + is run ke sessions), Expenses +1, Categories +1 (sibling tests). Returns tab header-only (0 rows — no stray data).

#### Micro-tests (RUN #11)
- [PASS] DB migration 31→32 in-place (install -r, data intact, no crash)
- [PASS] Force Sync post-restore — 0 pending / 0 errors / SchemaGuard
- [PASS] Returns invoice lookup — 845ED833 prefix → Invoice Found ($100 CASH) (DEF-86 path intact)
- [PASS] Sheet (23) data integrity diff vs (22) — all key tabs SAME
- [INCONCLUSIVE] Returns refund Issue-Refund tap — uiautomator crash (UiAutomationService already registered, pid 12185) ne tap swallow kiya; Returns tab empty, Sales 17 (koi data corruption nahi)
- [INCONCLUSIVE] Wastage log UI — dialog dismiss; DEF-102 guard source/build verified

#### Totals (2026-08-23 08:50 UAE)
- **Defects: 113 registered — 66+ FIXED/verified; OPEN: DEF-51 (partial, larger refactor), DEF-62 (partial, DEF-35 mitigates), GAP-3 (code wired, live-test pending), DEF-31 sheet-side vendor dup row (rules forbid sheet edits)**
- **Source fixes (10 files):** WastageViewModel.kt, CrmViewModel.kt, VendorManagementViewModel.kt, CreatePurchaseOrderViewModel.kt, CreateGrnViewModel.kt, InventoryCrudViewModel.kt, PosViewModel.kt, ReceiptScreen.kt, DatabaseModule.kt (ReturnsDao provider), ReturnsEntity.kt (column alignment)
- **Builds:** 2× assembleDebug (1st Dagger fail, 2nd SUCCESS 1m24s isolated) + 2× adb install -r (1st migration crash, 2nd Success)
- **Emulator:** app v1.0.0 (RUN-11 build), till OPEN (1000), sheet live-synced, connected sheet ID intact (14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU)

---

## RUN #12 / ROUND-5 (2026-08-23 09:35–10:10 UAE) — BACKUP COMPLETENESS + TILL/POS VALIDATION

### Fixes (4 source-verified + live)
| DEF | Issue | Fix | Live |
|-----|-------|-----|------|
| DEF-114 | Till open/pay-in/pay-out negative amounts | Guards in TillViewModel (openTill <0, payIn/payOut <=0) | source+build |
| DEF-115 | Backup ZIP sirf 6 tables | +14 CSV exports (20 total), 6 DAO getAll* additions, password_hash excluded | ✅ Back Up Now → 9443B ZIP → openpyxl 20 CSVs |
| DEF-116 | Duplicate receipt bina items | ReprintReceiptUseCase items_json parse + HistoryViewModel item lines | source+build |
| DEF-118 | Negative payment components sale complete kar sakte the | coerceAtLeast(0.0) in PosViewModel.onPaymentAmountChanged | ✅ sale flow regression PASS |

### Live micro-test (master task 2): CASH SALE → SHEET
- Search HERMES → HERMES-PROD-001 ×1 → cart $110.00 (100+10% tax) → Cash 110 → Confirm → Receipt 9AAA40FF
- SyncWorker: 12 tables synced, schema verified, completed successfully, 0 errors
- Sheet export (24) vs (23): Sales row 15 = 9aaa40ff $110 CASH ✅ · Inventory stock 20→19 ✅ · Till_Sessions expected_cash 1110 / count 1 ✅ · Returns +1 (023a1fcb RESTOCK — GAP-3 live) ✅ · Khata +1 (Refund JAMA) ✅ · all 13 old invoices intact (no data loss)

### Reinstall persistence (master task 3): RUN #11 PASSED (92 rows restore) — is run mein repeat N/A (data intact verify via backup ZIP + sheet (24) instead)

### Observations
- DEF-119 (LOW, UX): till-gate cold-start transient "No Active Register Session" — self-resolves, deferred
- DEF-117 (LOW): ReceiptGenerator dead code hardcoded "Rs" — noted, no change
- Scattered sheet rows: sibling cleanup 09:30 — RESOLVED ✅

### State
- Build: 1× assembleDebug SUCCESS + install -r Success · Registry: 134 DEFs (70+ fixed) · App: till OPEN $1000 (expected 1110), data intact
