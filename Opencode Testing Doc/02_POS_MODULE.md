# 02 — POS MODULE: HOME / CART / PAYMENT / RECEIPT / COMPLETE SALE

**Files under test:** `ui/home/HomeScreen.kt`, `PosViewModel.kt`, `PaymentDialog.kt`, `ReceiptScreen.kt`, `AdvancedMenuSheet.kt`, `HomeViewModel.kt`, `domain/usecase/CompleteSaleUseCase.kt`, `ReprintReceiptUseCase.kt`, `utils/ReceiptGenerator.kt`

**Prerequisites before starting this file:**
1. Fresh install, signed in, sheet created, onboarding complete.
2. Create products `HERMES-PROD-001` (Unit `PC`, Cost `50.00`, Price `100.00`, Tax `10`, Stock `10`, Low Alert `2`) and `HERMES-PROD-002` (Unit `KG`, Cost `150.00`, Price `250.00`, Tax `0`, Stock `5.5`, Low Alert `1`) using `03_INVENTORY F3.1`.
3. Open a till session with Opening Cash `1000.00` (steps in `06_SETTINGS_AND_SECURITY F6.7`).
4. Settings: confirm "Tax-Inclusive Prices" toggle is OFF and Currency Symbol is `$`.
5. Record the receipt invoice ID format: it is a 36-char UUID; the receipt shows only its first 8 chars uppercase.

---

## F2.1 TILL GATE (precondition to all selling)

**Field/Variable Level Test**
- [ ] T1. With no open till session: home screen must show lock icon (72dp AccentBlue), title `"No Active Register Session"` (or `"Register Session Closed"` if status `"RECONCILED"`), subtitle `"Please open a till before making sales."` / `"This till has been reconciled. Open a new till to continue selling."`, and button `"Open Till"` (min 220x48, AccentBlue).
- [ ] T2. With the till closed/reconciled: verify subtitle exactly `"This till has been reconciled. Open a new till to continue selling."`.

**CREATE & SYNC**
- [ ] T3. Tap `"Open Till"`. Expect navigation to TillOpenScreen. Enter Opening Cash `1000.00`, Notes `HERMES-TILL-001`, tap `"Open Register & Start Selling"`. Expect return to POS with cart enabled.

**READ & SYNC**
- [ ] T4. Wait for sync (Force Sync). Open Sheet tab `Till_Sessions`; find row where `session_id` (col A) matches the session. Verify `opening_cash` = `1000`, `cashier_name`, `pos_terminal_id` (first 20 chars of spreadsheet id or `TERM_1`), `status` = `"OPEN"`, `expected_cash` = `1000`, `sync_status` (col U) = `"synced"`, `created_at` (col V) and `updated_at` (col W) non-empty.
- [ ] T5. Close and reopen the app; verify the till gate does NOT appear while `status == "OPEN"` (still on POS).

**UPDATE & SYNC**
- [ ] T6. Perform a pay-in (F2.8) and a sale (F2.10); then open Sheet `Till_Sessions` row and verify `total_cash_sales`, `expected_cash` and `total_sales_count` columns updated in place (same row, not a new row).

**DELETE & SYNC**
- [ ] T7. No UI deletes a till session. Verify absence of any delete affordance on the till flow → record `[PASS]` if no delete button exists anywhere in TillOpenScreen. Till close is `"Close Till & End Shift"` (Z-Report, `05_STORE_MODULE F5.8`).

**Edge cases**
- [ ] E1. Open till with empty Opening Cash field → parsed as `0.0`; till must still open with `opening_cash = 0`. Record observed behavior.
- [ ] E2. Open till with Opening Cash `-50` → parsed `-50.0` (no validation). Record observed behavior.
- [ ] E3. Opening Cash `999999999999` → record whether app crashes or accepts.
- [ ] E4. Tap `"Open Register & Start Selling"` twice fast → record whether duplicate sessions are created in `Till_Sessions`.
- [ ] E5. Attempt to open till while one is already OPEN → record app behavior.

---

## F2.2 BARCODE SCAN → ADD TO CART (`InlineCameraBox`)

**Field/Variable Level Test**
- [ ] T1. On POS, the inline camera box (140dp, border `0xFF1E88E5` blue) shows `"Tap to activate scanner"` + `"Camera paused to save battery"` when idle. Verify exact text.
- [ ] T2. Tap it. Verify badge `"● LIVE"` (green) appears and border turns blue.

**CREATE & SYNC**
- [ ] T3. Point the camera at the printed barcode of `HERMES-PROD-001` (print via `03_INVENTORY F3.9` QR/GS1 label or generate on screen). Expected within 1.5s (debounce `DEBOUNCE_MS = 1500`): product found → vibration 100ms + beep (`TONE_PROP_BEEP` 150ms) + border green `0xFF4CAF50` 300ms → item added to cart with qty `1.0`. Verify cart row: name `HERMES-PROD-001`, `"$100 / PC"`, line total `$100`.
- [ ] T4. Scan an unknown barcode (e.g. EAN `8900000000001` not in inventory): border red `0xFFF44336`, no add. Record observed UI.
- [ ] T5. After 4 minutes without a scan (4-min sleep `SLEEP_TIMEOUT_MS`): verify camera auto-pauses back to `"Tap to activate scanner"`.

**READ & SYNC**
- [ ] T6. Scan the same product again → cart qty must increment to 2 (merged by `itemId`), line total `$200`.

**UPDATE & SYNC**
- [ ] T7. With `HERMES-PROD-002` (unit KG): scan, then tap the cart row → `CartDecimalQtyDialog` opens, title `"Enter Quantity"`, text `"Item: HERMES-PROD-002"`, field label `"Qty (KG)"` prefilled `1.000`. Enter `1.500`, tap `"Update"`. Verify cart shows `1.500` and line total `$375.00`.

**DELETE & SYNC**
- [ ] T8. Scan error case: scan 5 times rapidly. Verify only ONE add per scan (debounce must not double-add). Record each cart count.

**Edge cases**
- [ ] E1. Deny camera permission on first activation → record error/behavior (`"Camera permission required to scan"` is the VerifyQr string; on POS record actual).
- [ ] E2. Scan while cart contains the item with qty 0.1 (KG): verify min qty for KG step is `0.1` — press `-` twice, qty must not go below `0.1`.
- [ ] E3. Block-Negative-Stock OFF vs ON behavior for overstock scanning: set stock of PROD-001 to `1`, add qty `2` (via F2.5 decimal dialog). With `block_negative_stock` ON, expect snackbar `"Cannot oversell. Stock limit reached! (HERMES-PROD-001: requested 2, available 1)"` (SnackbarDuration.Short) and item NOT added.

---

## F2.3 SEARCH BAR → ADD TO CART

**Field/Variable Level Test**
- [ ] T1. Field placeholder exact: `"Search by name, SKU, or scan barcode…"`. Leading icon Search; trailing Clear icon (contentDescription `"Clear"`) appears only when query non-empty.
- [ ] T2. Type `HERMES-PROD-001` → dropdown result row shows `item_name`, `sku` (or barcode when SKU blank), and `"$100"`. Row tap → adds to cart qty 1.0.
- [ ] T3. Press hardware Enter / IME `Search` → first result added to cart + keyboard hides.

**CREATE & SYNC**
- [ ] T4. Type a partial name `HERMES-PROD` → results list; verify results update with 200ms debounce (`searchResults` uses `_searchQuery.debounce(200)`).

**READ & SYNC**
- [ ] T5. Clear query via Clear icon → dropdown closes, quick grid returns.

**UPDATE & SYNC**
- [ ] T6. Search by SKU `QA-SKU-001` (whatever SKU was set) → product found and added.

**DELETE & SYNC**
- [ ] T7. Search a non-existent name `ZZZ-NO-SUCH-ITEM` → empty dropdown, no crash.

**Edge cases**
- [ ] E1. Search with only spaces → record behavior.
- [ ] E2. Search with 200+ char string → record behavior (no crash expected).
- [ ] E3. Rapid typing of 50 chars → verify debounce prevents duplicate adds.

---

## F2.4 QUICK GRID (pinned items)

**Field/Variable Level Test**
- [ ] T1. Grid tile shows emoji `"🛒"`, `item_name` (11sp, max 2 lines), `"$100/PC"` price line.
- [ ] T2. Tap tile of `HERMES-PROD-001` (unit PC) → adds qty 1.0 directly. Tap tile of `HERMES-PROD-002` (unit KG) → must open `DecimalQtyDialog` (title `"Enter Quantity"`, field label `"Qty (KG)"` default `"1.0"`), confirm `"Add to Cart"`.

**CREATE & SYNC**
- [ ] T3. Pin an item: type product name in search, tap the pin chip `"📌"` (28dp circle) → `AdminPinDialog` opens. First time: registration mode, title `"Set Admin PIN"`, text `"No admin PIN is set. Please create a 4-digit PIN."`, fields `"New 4-digit PIN"` + `"Confirm PIN"`, button `"Next"`. Enter `1234` twice → then re-enter `1234` when `"Enter 4-digit PIN"` + `"Confirm"` appear → snackbar `"Pinned to quick grid"`. Verify tile appears in grid.
- [ ] T4. Wrong PIN attempt: pin a 2nd item with PIN `9999` → error string `"Incorrect PIN"`. Exact.

**READ & SYNC**
- [ ] T5. Verify PIN registration error strings: enter `1234`/`5678` mismatch → `"PINs do not match"`; enter `12` → `"PIN must be 4 digits"`.

**UPDATE & SYNC**
- [ ] T6. Unpin: long-press the pinned tile → dialog `"Remove from quick grid?"` + `"Remove HERMES-PROD-001 from quick grid?"`, confirm `"Remove"`. Verify tile disappears.
- [ ] T7. Re-pin and verify order: pin PROD-002 then PROD-001; verify grid order matches pin order (pinnedOrder = size+1).

**DELETE & SYNC**
- [ ] T8. Note: pin state is stored in Room `Inventory.isPinned`/`pinnedOrder` — verify it survives app restart (READ) but has NO dedicated sheet column. Record presence/absence of `is_pinned` in `Inventory` tab headers (expected: absent — local-only field).

**Edge cases**
- [ ] E1. Pin with PIN `0000` → accepted (no minimum-digit-strength check). Record.
- [ ] E2. Enter non-digit PIN `abcd` → record whether field rejects (NumberPassword keyboard).

---

## F2.5 CART OPERATIONS (qty steppers, remove, clear)

**Field/Variable Level Test**
- [ ] T1. Cart section header exact: `"Cart (2)"`. Rows show name, `"$100 / PC"`, `-` (desc `"Decrease"`), qty text, `+` (desc `"Increase"`), line total, `Close` icon (desc `"Remove"`, ErrorRed).
- [ ] T2. PC-unit item: `+` steps by `1.0`; `-` steps by `1.0`, min `1.0`. KG-item: step `0.1`, min `0.1`, qty displayed with 3 decimals (`%.3f`).
- [ ] T3. Totals row exact labels: `"Subtotal"`, `"Tax"` (only when >0), `"Discount"` (only when >0, green `- $x`), `"TOTAL"` bold AccentBlue.

**CREATE & SYNC**
- [ ] T4. Set cart: PROD-001 ×2, PROD-002 ×1. Verify Subtotal = `$450.00`, Tax = `$20.00` (PROD-001: 10% of 200), TOTAL = `$470.00`.

**READ & SYNC**
- [ ] T5. Verify `+` on PROD-002 (KG) → qty `1.100`, line total `$275.00`, TOTAL recalculates to `$495.00`.

**UPDATE & SYNC**
- [ ] T6. Tap cart row for PROD-002 → dialog prefilled `1.100`, update to `0.150` → line total `$37.50`, TOTAL `$257.50`.
- [ ] T7. Set qty to `0.000` via dialog → item must be REMOVED from cart (qty ≤ 0 → removeFromCart).

**DELETE & SYNC**
- [ ] T8. Tap `"Remove"` (Close icon) on PROD-001 → removed, Subtotal updates.
- [ ] T9. Tap `"Clear Cart"` → dialog `"Clear Cart?"` + `"All 1 items will be removed."`; confirm `"Clear"` → cart empty, totals `$0.00`, discount reset `0.0`, selected customer cleared. Verify the cart section disappears entirely (`"Cart (0)"` header gone).

**Edge cases**
- [ ] E1. Rapid `+` taps (10 taps) on PC item → qty must be exactly 10 (no lost updates).
- [ ] E2. Enter qty `9999999` via dialog → record total formatting overflow (e.g. `$999,999,900.00`).
- [ ] E3. Cancel `"Clear Cart"` via `"Cancel"` → cart unchanged.

---

## F2.6 CUSTOM ITEM ("Open Item / Custom Amount")

**Field/Variable Level Test**
- [ ] T1. Tap `"Open Item"` → dialog title `"Open Item / Custom Amount"`, text `"Enter a custom item to add to the cart."`, fields `"Item Name / Note"` (placeholder `"e.g. Custom Shirt"`) and `"Selling Price (Rs)"` (placeholder `"e.g. 15.50"`, Decimal keyboard), buttons `"Add to Cart"` / `"Cancel"`.

**CREATE & SYNC**
- [ ] T2. Enter name `HERMES-CUSTOM-001`, price `99.99` → `"Add to Cart"` → cart row `HERMES-CUSTOM-001`, `"$100 / PC"` (formatted `%.0f`), qty 1. Note: unit forced `"PC"`, tax `0.0`, itemId `CUSTOM_ITEM_<timestamp>`.

**READ & SYNC**
- [ ] T3. Complete this sale (F2.10 flow) → in Sheet `Sales_MMM_YYYY` `items_json` (col D) must contain `"name":"HERMES-CUSTOM-001"`, `"pricePerUnit":99.99`, `"taxPercent":0.0`.

**UPDATE & SYNC**
- [ ] T4. Add 2nd custom item with price `0.01` → verify allowed (price > 0 check only), total formats `$0`.

**DELETE & SYNC**
- [ ] T5. Blank name → defaults to `"Custom Item"`; price must be > 0 for the button to enable.

**Edge cases**
- [ ] E1. Price `abc` → `toDoubleOrNull` fails; record whether button enables (expected: disabled, no error text).
- [ ] E2. Price `-5` → record whether `-5 > 0` gate blocks.
- [ ] E3. Name 500 chars → record UI behavior.

---

## F2.7 DISCOUNT

**Field/Variable Level Test**
- [ ] T1. With cart total > 0 and no discount: button `"Discount"` (OutlinedButton, `LocalOffer` icon) visible. Tap → dialog title `"Apply Discount"`, text `"Enter discount amount"`, field `"Amount ($)"` prefilled `"0.00"`, buttons `"Apply"` / `"Cancel"`.

**CREATE & SYNC**
- [ ] T2. Cart = PROD-001 ×2 (subtotal 200, tax 20, total 220). Apply discount `25.00` → TOTAL `$195.00`, row shows `"Discount"` + `"- $25.00"` in SuccessGreen.

**READ & SYNC**
- [ ] T3. Complete the sale (F2.10) → Sheet `Sales_MMM_YYYY` col G (`discount`) = `25`, col H (`total`) = `195`, col F (`tax`) = `20`.
- [ ] T4. Re-open discount dialog → title becomes `"Edit Discount"`, field prefilled `"25.00"`.

**UPDATE & SYNC**
- [ ] T5. Change to `30.00` → TOTAL `$190.00`. Apply → sale total 190 verified in Sheet after sync.

**DELETE & SYNC**
- [ ] T6. Tap `"Remove"` (ErrorRed TextButton next to Discount row) → discount `0.0`, TOTAL back to `$220.00`.

**Edge cases**
- [ ] E1. Discount `500` on total 220 → TOTAL `-280.00`. Record negative total behavior in cart and in Sheet (refund detection: `sale.total < 0` renders `"REFUNDED"` in History). Record whether checkout allows negative total.
- [ ] E2. Discount `0` → dialog must behave as `"Apply Discount"` (0 → no change).
- [ ] E3. Discount with `block_negative_stock` irrelevant — record totals math anyway.

---

## F2.8 PAY IN / PAY OUT

**Field/Variable Level Test**
- [ ] T1. Top bar till icon (contentDescription `"Till"`) → dropdown menu with items `"Pay In (Add Cash)"` (AddCircle, green) and `"Pay Out (Remove Cash)"` (RemoveCircle, red).

**CREATE & SYNC**
- [ ] T2. `"Pay In (Add Cash)"` → dialog `"Pay In — Add Cash to Drawer"`, text `"Enter the amount being added to the till (e.g., change from bank)."`, field `"Amount (Rs)"` placeholder `"e.g. 500"`, confirm `"Confirm"` (enabled only when amount > 0). Enter `500` → Confirm.
- [ ] T3. `"Pay Out (Remove Cash)"` → dialog `"Pay Out — Remove Cash from Drawer"` + text `"Enter the amount being taken out (e.g., for expenses, lunch, etc.)."`. Enter `200` → Confirm.

**READ & SYNC**
- [ ] T4. Force Sync → Sheet `Till_Sessions`: `expected_cash` must reflect +500−200 → `1300` (1000 opening + 500 − 200) on the SAME row (in-place update, `updateRowByUuid`).

**UPDATE & SYNC**
- [ ] T5. Pay In `0` → `"Confirm"` must be disabled. Pay Out `0` → disabled. Record.

**DELETE & SYNC**
- [ ] T6. Pay In `-100` → record behavior (amount > 0 gate expected to block; `-100 > 0` false → button disabled).
- [ ] T7. Pay In `1e10` → record formatting.

**Edge cases**
- [ ] E1. Pay In while till is RECONCILED (after Z-Report) → record crash/error (expected: no open session → DAO behavior).

---

## F2.9 PAYMENT DIALOG (PaymentDialog.kt)

**Field/Variable Level Test**
- [ ] T1. Cart total `220.00` (PROD-001 ×2 + discount 0). Tap `"PAY NOW"` (filled AccentBlue, 16sp Bold). Dialog header: `"Payment"` + `"Total: $220.00"`; section `"Select Payment Method(s)"`.
- [ ] T2. Fields present: `"Cash"` (Money icon), `"Card"` (CreditCard), `"Digital Wallet"` (AccountBalanceWallet), `"Credit Amount"` (AccountBalance, Decimal), quick-add buttons `"+$10"` `"+$20"` `"+$50"` `"+$100"` `"+$500"` `"+$1000"`.
- [ ] T3. Cash quick-add: tap `"+$100"` twice → Cash field `200`. Verify `"Remaining:"` row shows `$20.00` (red if > 0.01).

**CREATE & SYNC**
- [ ] T4. Tap `"+$50"` → Cash `250` > total 220 → `"Change to return: $30.00"` (SuccessGreen). Verify `"Remaining:"` = `$0.00` (green).
- [ ] T5. `"Confirm Payment"` (CheckCircle, SuccessGreen, 54dp) enabled; tap → sale completes (see F2.10 verification).

**READ & SYNC**
- [ ] T6. Split payment: new cart, total `220.00`; Cash `100`, Card `100`, Wallet `20` → all 3 fields filled; method becomes `"SPLIT"`. Verify `payment_method` col I in Sheet = `"SPLIT"`, `payment_split_json` (col O) contains the `PaymentDetails` JSON (NOT `"{}"`), cash_amount=100, card_amount=100, wallet_amount=20.

**UPDATE & SYNC**
- [ ] T7. Clear all amounts → `methodString` = `"CASH"` (none-selected default). Record.
- [ ] T8. Partial cash + khata: cart `220`, Cash `120`, toggle `addBalanceToKhata` (label `"Add $100.00 balance to customer account"`) → Credit section auto-sets `100`; `remainingAmount` → 0. Requires selected customer (search `"Search customer name or phone"` → select). Verify error string when no customer selected: `"⚠ Please select a customer to proceed with Credit"` and Confirm disabled.

**DELETE & SYNC**
- [ ] T9. Toggle `showUdhaarSection` (`"Credit"` / `"Customer must be selected"`): turn OFF → udhaarText cleared and UDHAAR method + customer deselected. Record.
- [ ] T10. `"Add New Customer"` (PersonAdd) → form `"Name *"`, `"Phone *"`, `"WhatsApp (optional)"`; `"Save & Select Customer"` enabled only when name AND phone non-blank.

**Edge cases**
- [ ] E1. Cash `220.001` (3 decimals) → record parse (Double).
- [ ] E2. Cash `abc` → parses `0.0` silently; Confirm must be disabled (remaining > 0.01). Record.
- [ ] E3. All four methods filled summing beyond total → `"Change to return"` only for cash; record.
- [ ] E4. Customer phone with non-digit chars in khata flow → record.
- [ ] E5. `"Confirm Payment"` double-tap rapidly → `_isProcessing` guard must prevent duplicate sales (verify only ONE row appears in Sheet `Sales_MMM_YYYY`).
- [ ] E6. Wait 30s on dialog, rotate device → record state loss (local `remember` state vs ViewModel state).

---

## F2.10 COMPLETE SALE — CREATE & SYNC (core)

**Setup:** cart = PROD-001 ×2 (cost 50.00, price 100.00, tax 10%), discount 0, payment CASH 220.

**Field/Variable Level Test**
- [ ] T1. Before payment: verify `_isProcessing` guard — while spinner on `"Confirm Payment"`, UI must not allow another sale.
- [ ] T2. Verify stock before sale in Sheet `Inventory` tab for HERMES-PROD-001: `stock_qty` (col L) = `10`.

**CREATE & SYNC**
- [ ] T3. Confirm Payment. Receipt screen appears (F2.11). Record receipt `"Invoice #:"` = first 8 chars of the 36-char UUID.
- [ ] T4. Force Sync. In Sheet `Sales_MMM_YYYY` (current month tab) find the row whose `invoice_id` (col A) STARTS WITH the receipt's 8-char invoice ID. Verify EVERY cell:
  - B `pos_id` = first 20 chars of spreadsheet id
  - C `timestamp` = epoch millis of sale time
  - D `items_json` = Gson array with `name` `"HERMES-PROD-001"`, qty 2, unitCost `100.0`, taxPercent `10.0`, total `200.0`
  - E `subtotal` = `200`, F `tax` = `20`, G `discount` = `0`, H `total` = `220`
  - I `payment_method` = `"CASH"`, J `cash_amount` = `220`, K/L/M = `0`
  - N `customer_id` = blank, O `payment_split_json` = `"{}"`, P `reference_id` = blank
  - Q `cashier_id` = user email, R `sync_uuid` = full UUID
  - S `is_deleted` = `0`, T `deleted_at` = blank, U `sync_status` = `"synced"` (record actual if `"pending"`, then re-check after Force Sync)
  - V `pos_terminal_id`, W `system_row_id` = UUID, X `created_at`, Y `updated_at` non-empty
- [ ] T5. Verify `Inventory` tab row for HERMES-PROD-001: `stock_qty` (col L) = `8` (10−2), `updated_at` bumped, `sync_status` = `"synced"`.
- [ ] T6. Verify `Till_Sessions` row: `total_cash_sales` = `220`, `total_sales_count` = `1`, `expected_cash` updated.
- [ ] T7. Verify Room (via Data Viewer in Settings): `Sales` count +1, `Inventory` stock 8.

**READ & SYNC**
- [ ] T8. History (Home → Advanced Options → `"Transaction History"`) shows the sale: `"Invoice: 8CHARS"`, `"Payment: CASH"`, `"$220.00"`, date `dd MMM yyyy, hh:mm a`.
- [ ] T9. Verify Receipt QR: scan receipt QR with Verify QR (`05_STORE_MODULE F5.7`) → `"✓ RECEIPT VERIFIED"`.

**UPDATE & SYNC**
- [ ] T10. Completed sales have NO edit UI. Verify: no edit affordance on receipt or history row → `[PASS]` if absent. (Refunds are the only "modify" path — F2.10 DELETE below.)

**DELETE & SYNC**
- [ ] T11. Full-invoice refund (Returns flow in `05_STORE_MODULE F5.4`): refund total `220` reason `Restock`. After refund + Force Sync verify in Sheet:
  - New row in `Sales_MMM_YYYY` with `total` (col H) = `-220`, `reference_id` (col P) = `"REFUND_OF_<originalInvoiceId>_Restock"`, `payment_method` copied.
  - `Inventory` row: `stock_qty` back to `10`.
  - `Till_Sessions`: `total_refunds` = 220.
- [ ] T12. Refund does NOT physically delete the original row (append-only) → verify original row still present.

**Edge cases**
- [ ] E1. Complete sale with EMPTY cart → `"Confirm Payment"` flow must be unreachable (PAY NOW hidden); record.
- [ ] E2. Loyalty: with `KEY_LOYALTY_ENABLED` true, attach customer (from F2.9 T8) to a sale; verify Sheet `Customers` `loyalty_points` (col G) increased by `total × KEY_LOYALTY_RATE (1f)`, `lifetime_spend` (col H) += total.
- [ ] E3. UDHAAR sale: cart 220, Credit 220, customer selected → Sheet `Khata_Events` row: `event_type` = `"UDHAAR"`, `amount` = `220`, `note` = `"Sale: <8char>"`, `reference_sale_id` = sale system_row_id.
- [ ] E4. Offline sale: Airplane mode ON → complete sale → verify app does NOT crash, sale saved locally, pending badge increments; then Airplane mode OFF → Force Sync → row appears in Sheet.
- [ ] E5. `POST_SALE_INSTANT_SYNC` worker: after a sale with network ON, verify Sheet row appears WITHOUT manual Force Sync within ~60s. Record observed latency.
- [ ] E6. Double submit: rapid double-tap `"Confirm Payment"` → exactly one Sheet row (see F2.9 E5).
- [ ] E7. Sale while till is OPEN but `expected_cash` mismatch: record `expected_cash` vs `opening_cash + sales − payouts`.

---

## F2.11 RECEIPT SCREEN (ReceiptScreen.kt)

**Field/Variable Level Test**
- [ ] T1. After sale: top bar `"Receipt"`; body shows `"Invoice #:"` + 8-char ID, `"Date:"` (`dd MMM yyyy, hh:mm a`), `"Cashier:"` (first 20 chars), column headers `"ITEM"`/`"QTY"`/`"TOTAL"`, lines `"Subtotal:"` `"Tax:"` `"Discount:"` (`"- $…"`), `"TOTAL:"`, payment lines `"Cash Paid:"` `"Card:"` `"Wallet:"` `"Credit:"` `"Change:"`, `"Scan to verify invoice"`, `"Thank you! Come again 🙏"`, QR (contentDescription `"Invoice QR Code"`, 120dp).

**CREATE & SYNC**
- [ ] T2. `"Share on WhatsApp"` (filled `0xFF25D366`): with customer whatsapp set → opens `https://api.whatsapp.com/send?phone=<number>&text=<receipt text>`; with no number → inline field `"WhatsApp Number (with country code)"` appears + button `"Send to {whatsappNumber}"`. Enter `+923001234567` → verify intent.

**READ & SYNC**
- [ ] T3. `"Print Receipt"` (Outlined, Print icon):
  - No printer MAC configured → snackbar `"No printer configured. Set MAC in Printer Settings."`
  - MAC configured + printer on → `"Printing..."` then `"Receipt sent to printer"`; verify paper output: header `"TILLZO POS RECEIPT"`, invoice, totals, cut.
  - Printer off/wrong MAC → `"Print failed. Check printer connection."`

**UPDATE & SYNC**
- [ ] T4. `"New Sale"` (AccentBlue, 56dp, AddShoppingCart) → returns to POS with empty cart, `"New sale started"` log.

**DELETE & SYNC**
- [ ] T5. Verify NO Email / PDF / Copy buttons exist on receipt → `[PASS]` if absent.

**Edge cases**
- [ ] E1. WhatsApp number containing letters `abc` → record sanitization (phone cleaned `[0-9+]` in `sendWhatsAppReceipt`).
- [ ] E2. Printer MAC set to invalid `00:00:00:00:00:00` → record failure string.
- [ ] E3. Rotation mid-receipt → record state.
- [ ] E4. Receipt for a refunded sale (negative): History row shows `"REFUNDED"`; record receipt text for duplicate print (`"Status: REFUNDED"`).

---

## F2.12 DUPLICATE RECEIPT (History → Print Duplicate)

**Field/Variable Level Test**
- [ ] T1. History sale row shows Print icon only (contentDescription `"Print Duplicate"`) — NO "View Receipt"/"Reprint" text button. Verify absence.

**CREATE & SYNC**
- [ ] T2. Tap → status card `"Printing Duplicate Receipt..."` → on printer success `"Duplicate Printed Successfully."`; output text must contain `"      ** DUPLICATE RECEIPT **      "`, `"Invoice ID: <8chars>"`, `"TOTAL: $220.00"`, `"PAID VIA: CASH"`.

**READ & SYNC**
- [ ] T3. No printer → `"Print Failed. Check Printer connection."`; catch → `"Error: <message>"`. Record exact.

**UPDATE & SYNC**
- [ ] T4. Duplicate print for a sale that exists ONLY in Sheet (not local) — `ReprintReceiptUseCase` fetches from Sheet tab matching `invoice_id` col A. Record whether offline print works (expected: local lookup only).

**DELETE & SYNC**
- [ ] T5. Tap `"Dismiss"` on status card → clears status.

**Edge cases**
- [ ] E1. Invoice ID with lowercase/uppercase mismatch → record lookup case-sensitivity.

---

## F2.13 ADVANCED OPTIONS SHEET + FORCE SYNC + PENDING BADGE

**Field/Variable Level Test**
- [ ] T1. Menu icon (contentDescription `"Menu"`) → sheet `"Advanced Options"` with EXACT rows in order:
  1. `"Wastage Entry"` 2. `"Returns & Refunds"` 3. `"CRM / Accounts"` 4. `"Inventory"` 5. `"Vendors"` 6. `"Stock Adjustment"` 7. `"Stock Alerts"` 8. `"Purchase Orders"` 9. `"Goods Receipts"` 10. `"Transaction History"` 11. `"Force Sync"` 12. `"Z-Report / Day Close"` 13. `"Expenses"` 14. `"Open / Close Register"` 15. `"Time Clock"` 16. `"Verify Receipt QR"` 17. (divider) 18. `"Admin Dashboard"` 19. `"Hardware Diagnostics"` 20. `"Settings"`.

**CREATE & SYNC**
- [ ] T2. With 0 pending: sync dot green. Create a pending record (e.g. an expense from `05_STORE_MODULE F5.2`) → within UI refresh, badge shows count and dot amber (`WarningAmber`).

**READ & SYNC**
- [ ] T3. Tap `"Force Sync"` → status `"Sync in progress..."` → `"Sync Completed Successfully!"` (3s then clears). Verify badge count drops to 0 and dot green.

**UPDATE & SYNC**
- [ ] T4. Airplane mode ON → Force Sync → expect `"Sync Failed. Please check connection."` (3s). Record exact string and pending badge retention.

**DELETE & SYNC**
- [ ] T5. Badge cap: create >99 pending rows (e.g. 101 expenses via direct DAO insert is not possible via UI — instead record behavior with 99 max displayed; verify display caps at `99` if achievable).

**Edge cases**
- [ ] E1. Force Sync during an in-flight periodic sync → record `ExistingWorkPolicy.REPLACE` behavior.
- [ ] E2. Force Sync with sheet deleted from Drive → record error path (expect retry + eventual failure; no crash).

---

## F2.14 KNOWN-BUG CHECKPOINTS (POS)

- [ ] XF1. `remainingAmount` in PosViewModel does NOT apply the `taxInclusive` branch while `cartTotal` does (see 09 registry). Test: set "Tax-Inclusive Prices" ON, cart 1× PROD-001 (price 100 incl. tax) → cart total shows `$100` but Payment dialog `"Remaining:"` may compute from `sub + tax − paid` = `$110` → record the mismatch value exactly.
- [ ] XF2. CompleteSaleUseCase sets `payment_split_json = "{}"` when method != `"SPLIT"` — verify col O is literally `"{}"` (not blank) for CASH sales.
- [ ] XF3. `ReceiptGenerator.kt` is orphaned (no callers) and prints `"Rs"` hardcoded — verify no receipt in the app uses `"Rs"` when currency is `$` (should be `$`).
- [ ] XF4. `HomeViewModel.kt` is a legacy placeholder (numPad state) — verify no UI reads `HomeViewModel` (record null usage).
- [ ] XF5. Micro-batch window `MICRO_BATCH_WINDOW_MS = 20s` is defined but never used — verify sales upload via `POST_SALE_INSTANT_SYNC` immediately, not batched 20s.
