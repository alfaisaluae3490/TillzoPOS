# 06 — SETTINGS & SECURITY: SETTINGS / PIN / BILLING / LOGS / DATA VIEWER / USERS / TILL / FORCE UPDATE / ROOT BLOCK

**Files under test:** `ui/settings/SettingsModule.kt`, `options/privacy/SettingsScreen.kt` + `SettingsViewModel.kt`, `options/billing/BillingScreen.kt` + `BillingViewModel.kt` + `BillingManager.kt`, `options/logviewer/SystemLogsScreen.kt` + `SystemLogsViewModel.kt`, `options/dataviewer/LocalDataViewerScreen.kt` + `LocalDataViewerViewModel.kt`, `ui/security/AdminAndUsersScreen.kt`, `ui/auth/options/session/PINUnlockScreen.kt` + `PINUnlockViewModel.kt`, `ui/till/TillOpenScreen.kt` + `TillViewModel.kt`, `ui/update/ForceUpdateScreen.kt` + `ForceUpdateViewModel.kt`, `ui/security/RootBlockedScreen.kt`

**Prefs keys:** see `01` §1.10 (`tillzo_setup_secure_prefs`, `auth_prefs`, `tillzo_update_prefs`).

---

## F6.1 SETTINGS SCREEN — NAVIGATION & TOGGLES

**Navigation:** POS → Menu → `"Settings"` (route `"settings_module"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Settings & Privacy"`, back desc `"Back"`. Section headers EXACT: `"Account & Billing"`, `"Security & Device Lock"`, `"Store Settings"`, `"Data & Privacy (Google Play Compliance)"`, `"App Info"`.
- [ ] T2. Menu items (title / subtitle pairs):
  1. `"Subscription & Licenses"` / `"Manage your Tillzo POS Plus subscription"` → billing
  2. `"App Security PIN"` / `"PIN Lock is Active"` or `"Quick PIN lock is Disabled"` + Switch (`isPinEnabled`)
  3. `"Manage PIN"` (TextButton, only when PIN exists) → PIN settings dialog
  4. `"Block Negative Stock"` / `"Prevent selling more than available stock"` + Switch
  5. `"Currency Symbol"` / `"Applies to POS, receipts, reports (e.g. $, USD, EUR, AED, SAR, INR)"` + OutlinedButton (current symbol, default `"Rs"` display) → DropdownMenu options EXACT order: `"$", "USD", "EUR", "AED", "SAR", "INR", "GBP", "QAR", "OMR", "PKR"`
  6. `"Tax-Inclusive Prices"` / `"On: prices already include tax (total = subtotal). Off: tax added on top."` + Switch
  7. `"Loyalty Program"` / `"Customers earn points on every sale (shown in CRM / Accounts)"` + Switch
  8. `"Data Safe Promise"` (bold header) + body: `"This app reads and writes exclusively to your personal Google Sheet using the \`drive.file\` scope. Tillzo POS DOES NOT transmit, log, or store your inventory or customer data on any external developer servers. Your financial data stays entirely on your Google Drive."`
  9. `"Data Sheet"` card: label `"Connected Sheet ID:"` + ID (truncated `take(20) + "..."` if >20) + button `"Connect Different Sheet"` (SwapHoriz)
  10. `"Goods Receipt Drive Folder"` card: subtitle `"Folder: <name>"` or `"No folder selected"`; button `"Select Receipt Folder"` / `"Change Receipt Folder"`
  11. `"Privacy Policy"` / `"Read our full privacy commitments online"` → opens `https://tillzopos.com/privacy`
  12. `"Version"` / `"Tillzo POS <VERSION_NAME> (Build <VERSION_CODE>) - Protected by RootBeer"`
  13. `"System Logs"` / `"View and export app logs (rolling 48-hour buffer)"` → logs
  14. `"Local Backup"` / `backupProgress` or `"Export all data to a ZIP file"` → CreateDocument `application/zip`, filename `"TillzoPOS_Backup_<yyyyMMdd_HHmmss>.zip"`
  15. `"Stored Data (This Phone)"` / `"View all data saved on this device"` → data viewer
  16. `"Back Up Now"` / `autoBackupStatus` or `"Save a backup copy to Documents (safe even if you uninstall)"`

**CREATE & SYNC (settings persist to prefs — verify via behavior)**
- [ ] T3. Toggle `"Block Negative Stock"` ON → verify POS blocks oversell (F2.2 E3). OFF → allows. Prefs key `block_negative_stock`.
- [ ] T4. Currency Symbol → `"PKR"` → POS prices, receipts, reports show `PKR` (currencySymbol from prefs; default `"Rs"` display when blank). Record all surfaces: cart, payment, receipt, history.
- [ ] T5. `"Tax-Inclusive Prices"` ON → cart total = subtotal − discount (tax NOT added); OFF → subtotal + tax − discount. Verify against F2.14 XF1.
- [ ] T6. `"Loyalty Program"` OFF → CRM Loyalty Pts stops accruing on sales. ON → accrues. Prefs `KEY_LOYALTY_ENABLED`, rate `KEY_LOYALTY_RATE`=1f.

**READ & SYNC**
- [ ] T7. `"Connect Different Sheet"` → dialog `"Select Data Sheet"` + body `"Choose a Google Sheet to store your POS data."`; loading `"Searching your Google Drive..."`; empty `"No existing worksheets found"` + `"Create a new one below"`; divider `"  OR  "`; create card `"Create New Sheet"` / `"Start with a blank data sheet in your Drive"`; manual entry toggle `"Paste Sheet ID or URL manually"` ⇄ `"Hide manual entry"`, field `"Sheet ID or URL"`, button `"Connect"`; dismiss `"Cancel"`.
- [ ] T8. `"Select Receipt Folder"` dialog: body `"Choose a Google Drive folder for GRN attachments."`; loading `"Searching folders..."`; empty `"No folders found"`; toggle `"Create new folder"` ⇄ `"Hide create form"`, field `"Folder name"`, button `"Create & Select"`.
- [ ] T9. After connecting a different sheet: verify `spreadsheet_id` pref changed and new sheet receives sync (create a product → sync → row in NEW sheet). Record old-sheet residual data behavior.

**UPDATE & SYNC**
- [ ] T10. Folder selection persists: `grn_folder_id` + `grn_folder_name`; Settings card shows `"Folder: <name>"`; GRN attachments upload to it.

**DELETE & SYNC**
- [ ] T11. No "Clear Local Data" / logout / dark mode / sound / receipt auto-print / change-password buttons exist — confirm ALL absences → `[PASS]`.

**Edge cases**
- [ ] E1. Connect sheet with invalid ID `"abc"` → `extractSheetId` strips → empty → nothing saved. Record.
- [ ] E2. Connect a random public sheet (no POS tabs) → record sync behavior (ensureCoreTables / header write).
- [ ] E3. `"Create New Sheet"` twice quickly → record duplicate sheets in Drive (appProperties tag `isTillzoPosSheet: true`).
- [ ] E4. Folder name blank in create → record.
- [ ] E5. Open browser privacy link with no browser → record crash.

---

## F6.2 SECURITY PIN (setup / change / remove / lock screen)

**Field/Variable Level Test**
- [ ] T1. No PIN set: toggle `"App Security PIN"` ON → dialog `"Set Security PIN"` + `"Create a 4-digit PIN for quick access to Tillzo POS."` + field `"New 4-Digit PIN"` (digits only, max 4, PasswordVisualTransformation) + `"Save PIN"`/`"Cancel"`.
- [ ] T2. PIN set: toggle OFF → calls `togglePinLock(false)` (prefs `is_pin_enabled`). `"Manage PIN"` → dialog `"Security PIN Settings"` with `"Remove PIN"` (red), section `"Change PIN"`, fields `"Current PIN"`, `"New PIN"`, `"Confirm New PIN"`, button `"Change PIN"`.

**CREATE & SYNC**
- [ ] T3. Save PIN `1234` → toggle stays ON; `auth_prefs` key `app_pin` = `1234`; PINUnlockScreen (app relaunch) shows `"Tillzo Quick Access"` + field `"Enter 4-Digit PIN"` + `"Unlock"` + `"Logout & Login via OAuth"`.
- [ ] T4. Lock test: lock app (home/background or relaunch) → enter `1234` → `"Unlock"` → app opens (Success). Enter `9999` → error `"Invalid PIN"` (exact string), unlimited attempts (no lockout — record).

**READ & SYNC**
- [ ] T5. Change PIN: Current `1234`, New `5678`, Confirm `5678` → saved. Validation chain EXACT error strings:
  - wrong current → `"Incorrect current PIN. Please try again."`
  - new not 4 digits → `"New PIN must be exactly 4 digits."`
  - mismatch → `"New PIN and confirm PIN do not match."`

**UPDATE & SYNC**
- [ ] T6. `"Remove PIN"` → `app_pin` removed; Settings shows `"Quick PIN lock is Disabled"`.

**DELETE & SYNC**
- [ ] T7. PIN setup with `12` (not 4) → error `"PIN must be exactly 4 digits."` (PINUnlockScreen) / `"PIN must be exactly 4 digits."` (Settings dialog). Verify both exact strings.

**Edge cases**
- [ ] E1. PIN `0000` → accepted. Record.
- [ ] E2. PIN with letters `12ab` → record (digits-only filter expected).
- [ ] E3. `"Cancel"` on Set Security PIN dialog while toggle moved ON → toggle must reset to OFF (dialog cancel resets). Record.
- [ ] E4. Unlock screen: tap `"Logout & Login via OAuth"` → returns to sign-in flow, PIN cleared. Record full round-trip.
- [ ] E5. PINUnlockScreen setup mode title `"Create Your PIN"` + label `"Enter New 4-Digit PIN"` + button `"Save PIN"` — verify after logout+login with no PIN set.
- [ ] E6. No retry limit / no lockout — record that 100 wrong attempts still unlock on correct PIN.

---

## F6.3 BILLING (BillingScreen + BillingManager)

**Field/Variable Level Test**
- [ ] T1. Title `"Tillzo POS Subscription"`. State machine: `LOADING` → spinner + `"Connecting to Google Play..."`; `ACTIVE` → CheckCircle green 100dp + `"Subscription Active"` + `"You are currently on the Tillzo POS Pro plan. All data sync features are enabled."` (NO buttons); `EXPIRED`/`ERROR` → icon + headline `"Trial Expired"`/`"Connection Error"` + `"To continue syncing data to Google Sheets, please renew your subscription via Google Play."` + button `"Subscribe with Google Play"` (56dp, fillMaxWidth).
- [ ] T2. Error banner (errorContainer): raw `billingError` string. Footer: `"Billing is securely managed by Google Play. We do not store any payment information locally."`

**CREATE & SYNC**
- [ ] T3. With no subscription: status shows EXPIRED path; tap `"Subscribe with Google Play"` → Play billing sheet for `com.tillzo.pos.sub.monthly`.

**READ & SYNC**
- [ ] T4. Cancel purchase → `_billingError = "Purchase was cancelled."`. Record banner.
- [ ] T5. Active subscription (pre-existing Google Play purchase) → `"Subscription Active"`.

**UPDATE & SYNC**
- [ ] T6. NO Manage/Restore/Upgrade buttons (verified absent) — confirm → `[PASS]`.

**DELETE & SYNC**
- [ ] T7. Cancel subscription in Play console → status must flip to EXPIRED on refresh. Record latency.

**Edge cases**
- [ ] E1. No Play Services → `Billing Setup Failed: <debugMessage>`. Record.
- [ ] E2. `queryPurchases` fails → `"Failed to query purchases: <msg>"`.
- [ ] E3. Acknowledge fails → `"Acknowledge Failed: <msg>"` (subscriptions acknowledged, never consumed).
- [ ] E4. Purchase flow with `subscriptionOfferDetails` empty → `"Product details not found."`.
- [ ] E5. Airplane mode on Billing screen → record error state handling.

---

## F6.4 SYSTEM LOGS (SystemLogsScreen + SystemLogsViewModel)

**Field/Variable Level Test**
- [ ] T1. Title `"System Logs"`; actions: Filter (desc `"Filter"`), Export (desc `"Export"`, spinner while exporting, disabled while exporting). Search placeholder `"Filter by message..."` (singleLine, 12dp radius, Search icon).
- [ ] T2. Filter sheet `"Filter Logs"`: section `"Log Level"` chips EXACT `"ALL"`(null), `"INFO"`, `"WARN"`, `"ERROR"`, `"FATAL"` (colors: ERROR `0xFFF44336`, FATAL `0xFFD32F2F`, WARN `0xFFFF9800`); section `"Tag"` chips `"ALL"` + one per `allTags`; button `"Apply"`.
- [ ] T3. Log row: timestamp `"MM-dd HH:mm:ss"`, level, tag, message (collapsed maxLines 5 + ellipsis); WARN orange, INFO green, ERROR/FATAL errorContainer. Per-row `FilledTonalButton` `"Copy Log"` (ContentCopy icon).
- [ ] T4. Empty state `"No logs found"`. NO clear button exists anywhere — confirm absence → `[PASS]`.

**CREATE & SYNC**
- [ ] T5. Trigger actions that log (sales, sync, errors) → new log rows appear (Room `app_logs`, `FileLoggingTree` writes `app.log`, 512KB × 5 files).
- [ ] T6. Export → file `TillzoPOS_Logs_<yyyyMMdd_HHmmss>.txt` in Downloads; message `"Exported to Downloads/<file.name>"`; header `"TillzoPOS System Logs"` + `"Total entries: N"`; lines `"[$ts] [$level] [$tag] $message"`.

**READ & SYNC**
- [ ] T7. `"Copy Log"` → clipboard contains full message.

**UPDATE & SYNC**
- [ ] T8. Apply filters: level `"ERROR"` → only ERROR/FATAL? (DAO `getFilteredLogs(tag, level)`) — record which levels appear per filter.

**DELETE & SYNC**
- [ ] T9. 48h retention: `deleteLogsOlderThan(now - 48h)` on app start + SyncWorker — record that logs >48h are purged (simulate by clock manipulation if possible).

**Edge cases**
- [ ] E1. Search text not applied to DAO (query filters in-memory only) — record mismatch: search for text present only on later pages.
- [ ] E2. Export with no logs → `"Export failed"`/`"Export failed: <msg>"`. Record.
- [ ] E3. 5000+ logs → record list performance.
- [ ] E4. FATAL crash (kill app via `adb shell am crash`) → AppLog entry tag `"APP_CRASH"` with stacktrace. Verify appears in logs.

---

## F6.5 LOCAL DATA VIEWER (LocalDataViewerScreen + ViewModel)

**Field/Variable Level Test**
- [ ] T1. Title `"Stored Data (This Phone)"`. Info card: `"All data below is stored ON THIS PHONE"` / `"It is safe even offline. Cloud sync adds a second copy."`.
- [ ] T2. Summary grid labels EXACT: `"Inventory Items"`, `"Sales"`, `"Customers"`, `"Expenses"`, `"Khata Events"`, `"Till Sessions"`.
- [ ] T3. Expandable sections (NOT tabs) with headers + counts:
  - `"Inventory Items (N)"` rows: item_name / `"Stock: <n> <unit> • Price: <p> • SKU: <s>"`
  - `"Sales (N)"` rows: `"Invoice <8chars>"` / `"<date> • <payment_method> • <total> • <sync_status>"`
  - `"Customers (N)"` rows: name / `"<phone> • Loyalty: <pts> pts"`
  - `"Expenses (N)"` rows: category / `"<amount> • <description> • <date>"`
  - `"Khata Events (N)"` rows: `"<event_type> <amount>"` / `"Customer: <8chars> • <date>"`
  - `"Till Sessions (N)"` rows: `"Session <8chars>"` / `"<status> • Net: <netCash> • <date>"`
  - Date format `dd MMM yyyy, hh:mm a` (Locale.US); glyphs `"▾"`/`"▸"`.

**CREATE & SYNC**
- [ ] T5. Verify counts match Room after the records created in files 02–05.

**READ & SYNC**
- [ ] T6. Expand each section → rows render; khataEvents is one-shot (not reactive) — record whether new khata events appear without reopening screen.

**UPDATE & SYNC**
- [ ] T7. Make a change in another module (edit product) → counts/sync_status strings update live (StateFlow reactive except khataEvents).

**DELETE & SYNC**
- [ ] T8. NO export/backup/restore buttons on this screen (verified absent) — confirm → `[PASS]`.

**Edge cases**
- [ ] E1. 10,000 inventory rows → expand perf. Record.
- [ ] E2. Long names → truncation. Record.

---

## F6.6 ADMIN & USERS (AdminAndUsersScreen — 2 screens + dialog)

**Navigation:** POS → Menu → `"Admin Dashboard"` → `"Users"`.

**Field/Variable Level Test**
- [ ] T1. Dashboard header: `"← Back"`, `"Admin Dashboard"`, `"Users"` button. Stat cards EXACT: `"Today's Sales"`, `"Total Sales (All Time)"` (+`"<n> invoices"`), `"Today's Expenses"`, `"Total Expenses"`, `"Net (Sales − Expenses)"`, `"Inventory Value (cost)"` (Σ stock×cost), `"Registered Users"`.
- [ ] T2. UserManagementScreen: `"← Back"`, `"User Management"`, `"+ Add User"`; empty state `"No users yet.\nAdd the first user (role = Admin) to enable role-based access."`.
- [ ] T3. AddUserDialog `"Add User"`: field `"Email *"`, field `"Display Name"`, dropdown button `"Role: <role>"` options EXACT `listOf("Admin", "Manager", "Cashier")` (default `Cashier`), buttons `"Save"`/`"Cancel"`.
- [ ] T4. User row: name, email, role OutlinedButton → dropdown (Admin/Manager/Cashier), delete `"🗑"` icon.

**CREATE & SYNC**
- [ ] T5. Add `hermes.qa@example.com` / `HERMES QA` / role `Manager`. VM: name fallback = email prefix, `password_hash`=null, `pos_terminal_id`=`"ADMIN"`, UUID row id.
- [ ] T6. Force Sync → Sheet `Users_Permissions`: A UUID, B email, C name, D `"Manager"`, E blank (password_hash), F blank, G `is_deleted`=0, H blank, I `"synced"`, J `"ADMIN"`, K/L timestamps.

**READ & SYNC**
- [ ] T7. Role change: `Manager` → `Admin` → Sheet D updated, sync pending→synced (`setRole` marks pending + updated_at).

**UPDATE & SYNC**
- [ ] T8. Duplicate email add → error string `"User with this email already exists"` (errorMsg flow). Blank email → `"Email required"`. Record display mechanism.

**DELETE & SYNC**
- [ ] T9. `"🗑"` delete → Sheet G=1 soft delete. Verify row retained.

**Edge cases**
- [ ] E1. No users → empty-state text exact (T2).
- [ ] E2. 3 users with different roles → role dropdown on each independent.
- [ ] E3. Delete last user → record.
- [ ] E4. Today's Sales uses `dayStart = now − (now % 86400000L)` — record boundary behavior for a sale at 00:00:00.
- [ ] E5. Inventory Value sums `current_stock × cost_price` — verify against Sheet for a multi-product inventory.

---

## F6.7 TILL OPEN / CLOSE (TillOpenScreen + TillViewModel)

**Field/Variable Level Test**
- [ ] T1. Title `"Open Register / Start Shift"`, heading `"Enter Opening Cash"`, subtext `"Count your drawer and enter the total"`. Field `"Opening Cash (Amount)"` (Decimal, NO validation), field `"Notes (optional)"`. Button `"Open Register & Start Selling"` (PlayArrow icon).

**CREATE & SYNC**
- [ ] T2. Opening Cash `1000.00`, Notes `HERMES-TILL-OPEN` → button → POS. Sheet `Till_Sessions`: `session_id` UUID, cashier_id (email or `"cashier"`), cashier_name (display or `"Cashier"`), pos_terminal_id (20 chars or `"TERM_1"`), `opening_cash`=1000, `expected_cash`=1000, `shift_date`=`yyyy-MM-dd`, status=`"OPEN"`, `sync_status`=`"synced"`.

**READ & SYNC**
- [ ] T3. Reopen app → till gate not shown (session OPEN).

**UPDATE & SYNC**
- [ ] T4. Pay In/Out + sales update session columns (F2.8, F2.10).

**DELETE & SYNC**
- [ ] T5. Close via Z-Report (F5.8) → status `"RECONCILED"`, `closing_cash`, `net_cash`, `closed_at` set.

**Edge cases**
- [ ] E1. Opening Cash empty → 0.0. Record.
- [ ] E2. Opening Cash `abc` → 0.0. Record.
- [ ] E3. Two tills: open till on device A, sale on device A, open till on device B with same account → record two OPEN sessions behavior (`getOpenSessionFlow` per terminal).

---

## F6.8 FORCE UPDATE GATE (ForceUpdateScreen + CheckForceUpdateUseCase)

**Field/Variable Level Test**
- [ ] T1. Hard block (daysRemaining null): title `"Update Required"`, body `"This version of TillzoPOS is no longer supported.\nPlease update to continue using the app."`, button `"Update on Play Store"` (`BuildConfig.PLAY_STORE_URL`), non-dismissible.
- [ ] T2. Countdown: title `"Update Available"`, body `"Please update TillzoPOS within <N> day<s>.\nAfter that, the app will be blocked."`, buttons `"Later"` / `"Update"`.

**CREATE & SYNC**
- [ ] T3. Edit Sheet `Settings` tab: row `min_app_version` (value cell) = current VERSION_CODE + 100, `last_updated_timestamp` = now. Restart app → countdown appears with `3 - daysElapsed` days.

**READ & SYNC**
- [ ] T4. First-detection timestamp stored in `tillzo_update_prefs` key `outdated_first_detected_at`. Record persistence across restarts.

**UPDATE & SYNC**
- [ ] T5. Set `min_app_version` 3 days ago (via `outdated_first_detected_at` manipulation or waiting) → `daysElapsed >= 3` → HardBlock. Verify `"Update Required"` non-dismissible and app unusable behind it.

**DELETE & SYNC**
- [ ] T6. Restore `min_app_version` = 1, restart → gate gone. Record `outdated_first_detected_at` clearing.

**Edge cases**
- [ ] E1. `min_app_version` cell missing → default 1 → no gate. Record.
- [ ] E2. Settings fetch fails (offline) → `FetchError` → proceed (no gate). Record.

---

## F6.9 ROOT BLOCK (RootBlockedScreen)

**Field/Variable Level Test**
- [ ] T1. On a rooted device: full-screen red `0xFFB71C1C`, icon desc `"Security Alert"`, `"SECURITY ALERT"` (28sp Black, letterSpacing 2sp), `"Tillzo POS cannot run on this device."`, `"Your device appears to be ROOTED or modified. To protect your financial data and comply with Google Play policies, access has been permanently blocked."` (color `0xFFFFCDD2`), button `"EXIT APP"` (white, red content `0xFFB71C1C`, 56dp) → `finishAffinity()`.

**READ & SYNC**
- [ ] T2. Non-rooted device: gate absent. Verify RootBeer check passes → `[PASS]`.

**DELETE & SYNC**
- [ ] T3. Tap `"EXIT APP"` on rooted device → app process fully exits (no restart loop). Record.

**Edge cases**
- [ ] E1. Magisk-hide / app not detected as rooted → record whether gate triggers (RootBeer heuristics).

---

## F6.10 SETTINGS KNOWN-BUG CHECKPOINTS

- [ ] XF1. `SettingsModule` has NO route for "Admin & Users", "Hardware", or "Store" — those screens live in AppNavHost; verify Settings shows only 3 sub-routes (`billing_screen`, `system_logs`, `data_viewer`).
- [ ] XF2. SettingsViewModel `setTaxInclusive` writes prefs key literally `"KEY_TAX_INCLUSIVE"` (same for `KEY_LOYALTY_ENABLED`) — inconsistent naming with snake_case keys. Record that toggles still work.
- [ ] XF3. `BaseViewModel` (errorChannel + updateState) is NOT used by Settings/Billing/Logs/DataViewer VMs. Verify no functional impact.
- [ ] XF4. Billing `onPurchasesUpdated` with `USER_CANCELED` shows `"Purchase was cancelled."` as an ERROR banner — record severity mismatch (cancellation shown as error).
- [ ] XF5. SystemLogs export filename generated inside `appLogger` (screen only shows `"Exported to Downloads/<file.name>"`) — record actual file name.
