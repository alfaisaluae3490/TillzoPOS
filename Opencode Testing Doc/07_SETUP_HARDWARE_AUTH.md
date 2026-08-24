# 07 — SETUP / AUTH / HARDWARE: SIGN-IN, ONBOARDING, SHEET SELECTION, PRINTERS, SCANNER, DIAGNOSTICS

**Files under test:** `ui/signin/SignInScreen.kt` + `SignInViewModel.kt`, `ui/signin/SheetSelectionScreen.kt`, `ui/setup/onboarding/OnboardingScreen.kt` + `OnboardingViewModel.kt`, `ui/setup/SheetPickerScreen.kt` + `SheetPickerViewModel.kt`, `ui/hardware/printer/PrinterSettingsScreen.kt` + `PrinterSettingsViewModel.kt`, `ui/hardware/scanner/BarcodeScannerScreen.kt` + `ScannerViewModel.kt` + `InlineScannerViewModel.kt` + `InlineCameraBox.kt`, `ui/hardware/HardwareDiagnosticScreen.kt`, `ui/AppNavHost.kt`, `ui/MainActivity.kt`

---

## F7.1 SIGN-IN (Google OAuth)

**Field/Variable Level Test**
- [ ] T1. First launch (no `spreadsheet_id`): AppNavHost start = `"sheet_picker"` → SheetPickerScreen. If logged out → `SignInScreen`: title `"TillzoPOS"`, subtitle `"Smart POS for your shop"`, button `"Continue with Google"`.
- [ ] T2. Tap `"Continue with Google"` → DISCLOSURE dialog (not direct sign-in): title `"Backup & Sync Consent"`; body EXACT: `"Tillzo POS requires access to your personal Google Drive (via drive.file scope) to create and synchronize a secure database spreadsheet. This sheet stores your sales, inventory, and expense data so you can access it across your devices.\n\nSyncing happens in the background to ensure data consistency. Your data remains stored purely locally on your device and inside your own Google Drive. Tillzo POS developers do NOT collect, access, transfer, or sell your data."`; buttons `"Accept & Sync"` / `"Cancel"`.
- [ ] T3. `"Accept & Sync"` → Google account picker (`requestEmail`, `requestIdToken(ANDROID_CLIENT_ID)`, scope drive.file only).

**CREATE & SYNC**
- [ ] T4. Select account → signed in; default display name `"Shop Owner"` when none; prefs `user_email` + `user_display_name` saved; state `Done` → navigates to sheet selection.
- [ ] T5. Cancel account picker → `statusCode 12501` → back to `Idle` (no error shown). Record.

**READ & SYNC**
- [ ] T6. Error paths (record exact strings):
  - `10` → `"Developer Error (10): SHA-1 mismatch or Client ID misconfiguration. Check Logcat."`
  - `12500` → `"Sign-in failed (12500): Check Google Cloud Console and SHA-1 fingerprint."`
  - other → `"Sign-in failed (code=<code>)"` / `e.localizedMessage ?: "Sign-in failed"`

**UPDATE & SYNC**
- [ ] T7. Logout (via PIN screen `"Logout & Login via OAuth"`) → `GoogleSignIn.signOut()` + tokens invalidated → relaunch shows SignInScreen. Sign in again → same sheet restored (provisioned state).

**DELETE & SYNC**
- [ ] T8. Revoke app access from Google account settings → relaunch → record auth failure path (401 → `RE_AUTH_NEEDED` broadcast → sign-in flow).

**Edge cases**
- [ ] E1. Airplane mode during sign-in → record.
- [ ] E2. Multiple Google accounts on device → picker shows list. Record.
- [ ] E3. `SignInUiState.MultipleSheets` path — when 2+ existing tagged sheets: `SheetSelectionScreen` appears (see F7.3).

---

## F7.2 ONBOARDING (8-step wizard)

**Field/Variable Level Test**
- [ ] T1. Header `"Set Up Your Business"` + subtitle `"Step <N> of 8 — <name>"`; steps EXACT: `["Owner Name", "Business Name", "Business Address", "Business Logo", "Business Phone", "Social Media", "Website / App Link", "Review"]`. Progress bar `(step+1)/8`.
- [ ] T2. Step 0: title `"What's your name?"`, subtitle `"Optional — used as the owner / manager name."`, placeholder `"e.g. John Smith"` → var `ownerName` (optional).
- [ ] T3. Step 1: title `"What's your business name? *"`, subtitle `"Required — shown on receipts, labels and your Google Sheet."`, placeholder `"e.g. Smith's Grocery"` → `businessName` REQUIRED.
- [ ] T4. Step 2: title `"Business address *"`, subtitle `"Required — printed on receipts and used for reports."`, placeholder `"e.g. 123 Main Street, New York, NY"` (multiline) → `businessAddress` REQUIRED.
- [ ] T5. Step 3: title `"Business logo"`, text `"Used on receipts and barcode labels. Optional — you can add one later."`, box `"Tap to choose a logo"` / `"Loading logo..."`, confirmation `"✓ Logo selected"`, picker `"image/*"` → `logoPath` (copied to `filesDir/business_logo.png`).
- [ ] T6. Step 4: `"Business phone number *"`, subtitle `"Required — customers can call this number from receipts."`, placeholder `"e.g. +1 555 123 4567"` → `businessPhone` REQUIRED.
- [ ] T7. Step 5: `"Social media username"`, subtitle `"Optional — just your @username, no full link needed."`, placeholder `"e.g. @smithsgrocery"` → `businessSocial`.
- [ ] T8. Step 6: `"Website / App link"`, subtitle `"Optional — one link for both your website and app."`, placeholder `"e.g. www.smithsgrocery.com"` → sets BOTH `businessWebsite` AND `businessAppLink`.
- [ ] T9. Step 7 Review: title `"Review your details"`, subtitle `"Everything looks good? Press Finish to sign in with Google and create your business workspace."`; rows `"Owner"`, `"Business"`, `"Address"`, `"Phone"`, `"Social"`, `"Website / App"`; card: `"After this: Google sign-in → your business Sheet + Drive folder are created automatically."`.
- [ ] T10. Buttons: `"Back"` (OutlinedButton, step>0), `"Next"` (disabled when `!canProceed || saving`), `"Finish & Sign In"` (last step, `"Saving..."` while saving).

**CREATE & SYNC**
- [ ] T11. Enter: owner `HERMES OWNER`, business `HERMES BUSINESS`, address `HERMES ADDRESS 1`, phone `+923001234567`, social `@hermesqa`, website `https://hermesqa.example`. Logo: pick a small PNG.
- [ ] T12. `"Finish & Sign In"` → saved to prefs: `owner_name`, `business_name`, `business_address`, `business_phone`, `business_social`, `business_website`, `business_app_link`, `business_logo_path`, `onboarding_complete`=true.

**READ & SYNC**
- [ ] T13. Reinstall → `prefillFromExisting()` — wait, prefs cleared on reinstall; instead: kill app mid-wizard (step 4) → relaunch → record whether wizard resumes at step 4 or restarts (state held in memory only — record actual).

**UPDATE & SYNC**
- [ ] T14. Edit a step after completing (back nav) → updated prefs.

**DELETE & SYNC**
- [ ] T15. `"Back"` from step 0 → record (button hidden when step 0).

**Edge cases**
- [ ] E1. `"Next"` with blank businessName (step 1) → disabled. Record.
- [ ] E2. Logo with huge 50MB image → record.
- [ ] E3. Phone `abc` → accepted (no validation). Record.
- [ ] E4. Website without scheme `hermesqa.example` → record stored raw.

---

## F7.3 SHEET SELECTION & SHEET PICKER

**Field/Variable Level Test**
- [ ] T1. `SheetSelectionScreen` (2+ tagged sheets found): top bar `"Select Your Data Sheet"`; banner `"We found existing POS data sheets in your Google Drive. Select one to continue, or create a fresh sheet."`; header `"Your Existing Sheets:"`; row: TableChart icon, name, `"Last modified: <ts>"`, `"Created: <ts>"`, ChevronRight; divider `"  or  "`; button `"Start Fresh (Create New Sheet)"` (OutlinedButton, AddCircleOutline).
- [ ] T2. `SheetPickerScreen` (0 or 1 sheet): header `"Select Data Sheet"`, subtitle `"Where should your POS data be stored?"`; loading `"Initializing..."` / `"Creating your data sheet..."` / `"Searching your Google Drive..."`; sections `"📂  Found in your Google Drive"`, `"✨  Start Fresh"`, divider `"  OR  "`; empty `"No existing sheets found"` / `"Create a new one below"`; button `"Retry"` (error state); card `"Create New Sheet"` / `"Start with a blank data sheet in your Drive"`; sheet cards show `"Modified: <date>"`, `"Created: <date>"`, tag `"Tillzo POS backup"` (when tagged).

**CREATE & SYNC**
- [ ] T3. Create new: `sheetSetupUseCase.execute(shopName, forceCreate = true)` → new spreadsheet titled `"<businessName> — TillzoPOS"` (fallback `"Shop Owner"`); 24 tabs created (list in `01` §1.1); headers written per `SheetColumns`; `Settings` seeded `last_updated_timestamp`=0, `min_app_version`=1, `shop_name`; Drive folder `"TillzoPOS Business"` created; sheet tagged `appProperties {isTillzoPosSheet: "true", shopName, createdByApp: "TillzoPOS", version: "1"}`.
- [ ] T4. Verify in Drive: folder + tagged spreadsheet exist; record spreadsheet ID.
- [ ] T5. Select existing: `"Select Data Sheet"` → `saveProvisioningResult` + `ensureBusinessFolder()` + `scheduleRestoreWorker()` (`initial_restore`) → navigate home.

**READ & SYNC**
- [ ] T6. Restore dialog: `"Restoring cloud database..."` + status + `"This may take up to a minute. Please do not close the app."` with progress bar; on failure `"Restore Failed"` + error + `"Retry Restore"`.

**UPDATE & SYNC**
- [ ] T7. Exactly 1 tagged sheet found → auto-selects without asking (`sheets.size == 1`). Record.

**DELETE & SYNC**
- [ ] T8. Delete the Drive sheet, clear app data, reinstall → no sheets found → create path. Record.

**Edge cases**
- [ ] E1. `loadExistingSheets` exception → silently `Ready(emptyList())`. Record.
- [ ] E2. Create failure → `"Failed: <result.error>"` / `"Failed: <e.message>"`. Record.
- [ ] E3. Restore worker retries: `runAttemptCount < 3` → backoff EXPONENTIAL 10s; then failure. Record timing on network-off restore.
- [ ] E4. Sheet with only `SYS_DB_DO_NOT_TOUCH` tab (foreign) → record header-write attempt behavior.

---

## F7.4 PRINTER SETTINGS (PrinterSettingsScreen + ViewModel)

**Field/Variable Level Test**
- [ ] T1. Header `"Back to POS"` button; title `"Hardware Settings"`; button `"Test ML Scanner"` (→ scanner testing).
- [ ] T2. Card 1 `"Bluetooth Printer (SPP)"`: field `macAddress` label `"MAC Address (e.g. 00:11:22:33:44:55)"`; button `"Test Bluetooth Connection"`.
- [ ] T3. Card 2 `"Wi-Fi / Network Printer (Port 9100)"`: field `ipAddress` label `"IP Address (e.g. 192.168.1.100)"`; button `"Test Network Connection"`.
- [ ] T4. Button `"Hardware Diagnostics"` (OutlinedButton) → diagnostics. Status line `"Status: <status>"` — container errorContainer if contains `"Failed"`, primaryContainer if contains `"Success"`, else surfaceVariant.
- [ ] T5. NO dropdowns, NO save button (auto-save on change), NO validation errors (verified absent).

**CREATE & SYNC**
- [ ] T6. Enter MAC `00:11:22:33:44:55` → auto-persisted to pref `printer_mac`. Enter IP `192.168.1.100` → `printer_ip`.
- [ ] T7. `"Test Bluetooth Connection"` → `"Testing Bluetooth Print..."` → `"Bluetooth Print Success"` (payload `"Tillzo Bluetooth Print Test Successful!"` printed) OR `"Bluetooth Print Failed - Pair Device First"`. Record exact.
- [ ] T8. `"Test Network Connection"` → `"Testing Network Print..."` → `"Network Print Success"` OR `"Network Print Failed - Check IP/Port"` (port 9100; payload `"Tillzo Wi-Fi Print Test Successful!"`). Record.

**READ & SYNC**
- [ ] T9. Relaunch → MAC/IP retained.

**UPDATE & SYNC**
- [ ] T10. Change MAC to invalid `"zz"` → record (no validation; printer connect attempts fail).

**DELETE & SYNC**
- [ ] T11. Clear MAC field → pref empty; POS print shows `"No printer configured. Set MAC in Printer Settings."`.

**Edge cases**
- [ ] E1. MAC with lowercase hex → record connect behavior.
- [ ] E2. Network test to unreachable IP → 3 attempts, `delay(1000*attempt)`, socket `soTimeout=3000`; total ~6s. Record observed failure time.
- [ ] E3. Printer paired but OFF → `"Bluetooth Print Failed - Pair Device First"`. Record.

---

## F7.5 BARCODE SCANNER (full-screen, BarcodeScannerScreen + ScannerViewModel)

**Field/Variable Level Test**
- [ ] T1. Back (desc `"Close"`) → `stopScanning()` + dismiss; title `"Scan Barcode"`; torch icon (desc `"Torch"`, FlashOn yellow `0xFFFFEB3B` / FlashOff).
- [ ] T2. Formats (9): `QR_CODE, CODE_128, CODE_39, EAN_13, EAN_8, UPC_A, UPC_E, DATA_MATRIX, PDF417`. Camera: back, 1280×720 max, `STRATEGY_KEEP_ONLY_LATEST`.
- [ ] T3. Status texts: Scanning `"Position barcode inside the frame"` + `"Auto-detect ON"`; Processing `"Looking up product..."`; Success `"Product found! Adding to cart..."`; NotFound `"Product not found in inventory"` + `"Try scanning again"`.
- [ ] T4. Feedback: vibrate 100ms, beep `TONE_PROP_BEEP` 150ms, reticle green 300ms → blue; 200ms delay → `onProductScanned`; 30s idle (`delay(30_000L)`) → auto `Idle` → dismiss.

**CREATE & SYNC**
- [ ] T5. Scan HERMES-PROD-001 barcode → Success state → product added to cart qty 1.0.
- [ ] T6. Scan unknown EAN `8900000000001` → NotFound → 2s pause → back to Scanning (retry loop). Record.

**READ & SYNC**
- [ ] T7. Rapid 5 scans of same barcode within 1.5s (debounce `DEBOUNCE_MS=1500`) → only ONE add. Record.

**UPDATE & SYNC**
- [ ] T8. Torch toggle ON/OFF → camera flash. Record permission impact.

**DELETE & SYNC**
- [ ] T9. `"Close"` mid-scan → returns without add.

**Edge cases**
- [ ] E1. Camera permission denied → record error handling.
- [ ] E2. Scan a QR (not barcode) → QR_CODE format supported → record lookup result.
- [ ] E3. Scan DataMatrix (GS1 label from F3.9) → record lookup (barcode stored as plain GTIN string vs GS1 content mismatch).
- [ ] E4. Low light / blur → record scanner robustness (multiple attempts).

---

## F7.6 INLINE SCANNER (HomeScreen camera box) — cross-ref F2.2; extra checks

- [ ] T1. Analysis resolution 640×480 (480p), 8 formats (no PDF417), back camera.
- [ ] T2. Sleep timer 4 min (`SLEEP_TIMEOUT_MS`), reset on each scan; tap-to-wake.
- [ ] T3. `isCameraActive` false → box shows tap-to-activate; true → `"● LIVE"` badge + scan guide + corner markers (16dp/2.5dp) + 0.7f×1.5dp guide line.
- [ ] T4. Border colors: blue `0xFF1E88E5` idle, green `0xFF4CAF50` ProductFound, red `0xFFF44336` ProductNotFound.

---

## F7.7 HARDWARE DIAGNOSTICS (HardwareDiagnosticScreen)

**Field/Variable Level Test**
- [ ] T1. Title `"Hardware Diagnostics"`, back desc `"Back"`. Section `"Printer Diagnostics"`: `"Configured Printer MAC: <mac>"` (`"Not configured"` red if blank). Card `"Bluetooth Printer Test"`: desc `"Sends a connection test command to the configured thermal printer."`; button `"Test Printer Connection"` (spinner + `"Testing..."`, enabled iff `!isTestingPrinter && printerMac.isNotBlank()`).
- [ ] T2. Section `"Scanner Diagnostics"`: card `"Camera / Barcode Scanner"`: `"Scanner testing is available from the Scanner screen."` + `"To test: Go to Settings > Hardware > Scanner Testing"` (no button).
- [ ] T3. Section `"System Info"`: `"Printer MAC"` (`"Not set"` if blank), `"Bluetooth Available"` (`isEnabled?.toString() ?: "N/A"`), `"Camera Available"` = `"Via ML Kit barcode scanner"`.

**CREATE & SYNC**
- [ ] T4. MAC configured → `"Test Printer Connection"` → sends TSPL label: `SIZE 40 mm, 30 mm` + text `"DIAGNOSTIC"` + barcode `"TSPL DIAGNOSTIC TEST\nConnection OK"`. Results EXACT: `"Printer test successful. Check device for output."` / `"Printer test failed. Check MAC address and Bluetooth."` / `"Error: <e.message>"`.

**READ & SYNC**
- [ ] T5. MAC blank → button disabled + `"Not configured"` red. Record.

**UPDATE & SYNC**
- [ ] T6. MAC valid + printer OFF → failure strings. Record.

**DELETE & SYNC**
- [ ] T7. No persistence on this screen (read-only) — confirm → `[PASS]`.

**Edge cases**
- [ ] E1. Bluetooth OFF system-wide → `"Bluetooth Available: false"`. Record test result.
- [ ] E2. Double-tap test button → `isTestingPrinter` guard. Record.

---

## F7.8 NAVIGATION GATES (AppNavHost + MainActivity)

**Field/Variable Level Test**
- [ ] T1. Start route: `spreadsheetId.isEmpty()` → `"sheet_picker"`, else `"home"`.
- [ ] T2. Root gate: `RootBeer(context).isRooted` OR any su-path → `RootBlockedScreen` (NO NavHost at all).
- [ ] T3. Force update gate: `CheckForceUpdateUseCase` on start → `UpToDate` / `CountdownActive` / `HardBlock` / `FetchError` (F6.8).
- [ ] T4. PIN gate: `PINUnlockScreen` when app locked (F6.2).

**READ & SYNC**
- [ ] T5. Route inventory (record each navigation logs `"Navigated to: <route> | Args: <args>"`):
  `sheet_picker`, `home`, `receipt/{invoiceId}` (popUpTo home), `inventory_module` (inner: `inventory_crud`, `ocr_entry`, `qr_generator/{barcode_id}`, `barcode_print_settings/{item_id}`, `category_management`, `product_units`, `stock_alerts`), `store_module/{startDest}` (inner: `crm_screen`, `statement_screen/{customerId}`, `statement/{customerId}`, `returns_screen`, `history_screen`, `zreport_screen`, `expense_screen`), `time_clock`, `verify_qr`, `settings_module` (inner: `settings_main`, `billing_screen`, `system_logs`, `data_viewer`), `po_list`, `create_po`, `po_detail/{poId}`, `grn_list`, `create_grn/{poId}` (→ success `popBackStack("po_list")` + `grn_success/{grnId}/{newProductsCreated}/{batchesAdded}/{batchesUpdated}`), `grn_detail/{grnId}`, `qr/{barcodeId}`, `till_open`, `wastage_log`, `stock_adjustment`, `hardware_diagnostics`, `admin_dashboard`, `user_management`.

**UPDATE & SYNC**
- [ ] T6. Receipt flow: `onNewSale` → `popBackStack("home")`. Record deep-link edge: relaunching into `receipt/{invoiceId}` with stale id.

**DELETE & SYNC**
- [ ] T7. Invalid route string → record (NavHost failure handling).

**Edge cases**
- [ ] E1. Process death mid-sale → state restore. Record.
- [ ] E2. System back button during payment dialog → record.
- [ ] E3. IME/hardware enter on search bar → adds first result (F2.3 T3).

---

## F7.9 SETUP/HARDWARE KNOWN-BUG CHECKPOINTS

- [ ] XF1. `MICRO_BATCH_WINDOW_MS` (20s) defined but never used — sales sync instantly via `POST_SALE_INSTANT_SYNC`. Record observed latency.
- [ ] XF2. `SignInViewModel` skips auto sheet-check after sign-in; navigation to `sheet_picker` is handled by AppNavHost — verify no double navigation.
- [ ] XF3. `SheetPickerScreen` folder fallback name `"TillzoPOS Business"` used when businessName blank — record which folder is created.
- [ ] XF4. `EscPosPrinter` sends NO barcode commands (text only) — verify a POS receipt has no barcode printed (only QR via other paths).
- [ ] XF5. `TsplPrinter` label is hardcoded 40×30mm — verify physical label size matches on a 40mm printer.
- [ ] XF6. `ScannerViewModel` idle 30s auto-dismiss — record whether an idle scanner dismisses the screen unexpectedly mid-demo.
