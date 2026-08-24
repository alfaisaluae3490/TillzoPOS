# 05 — STORE MODULE: CRM / EXPENSES / HISTORY / RETURNS / STATEMENT / TIME CLOCK / VERIFY QR / Z-REPORT

**Files under test:** `ui/store/StoreModule.kt`, `options/crm/*`, `options/expense/*`, `options/history/*`, `options/returns/*`, `options/statement/*`, `options/timeclock/*`, `options/verifyqr/*`, `options/zreport/*`

**Sheet tabs:** `Customers`, `Khata_Events`, `Expenses`, `Sales_MMM_YYYY`, `Till_Sessions`, `Time_Clock`, `Wastage_Ledger` (column maps in `01` §1.2).

**Route map:** `StoreModule` start `"crm_screen"`; routes `crm_screen`, `statement_screen/{customerId}`, `statement/{customerId}`, `returns_screen`, `history_screen`, `zreport_screen`, `expense_screen`; AppNavHost adds `time_clock`, `verify_qr`.

---

## F5.1 CRM / CUSTOMERS & KHATA LEDGER (CrmScreen + CrmViewModel)

**Navigation:** POS → Menu → `"CRM / Accounts"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Customers & Ledger"`, action icon desc `"Add Customer"`, search placeholder `"Search Customers"`, empty state `"No customers yet"` / `"Tap + to add your first customer"`.
- [ ] T2. Add/Edit dialog fields EXACT: `"Name *"`, `"Phone *"`, `"WhatsApp"`, `"Email"`, `"Address"`; buttons `"Save"` (blue `0xFF1E88E5`, enabled only if name AND phone non-blank) / `"Cancel"`.
- [ ] T3. Selected customer view: header card (name, phone, email if present), icons desc `"Edit Customer"` / `"Delete Customer"`, button `"WhatsApp Statement"` (Message icon), StatBox 2×2 labels EXACT: `"Total Credit"` (red), `"Total Paid"` (green), `"Balance Due"` (red if >0 else green), `"Loyalty Pts"` (amber, no currency symbol).
- [ ] T4. Action buttons EXACT: `"Add Credit (-)"` (red) / `"Record Payment (+)"` (green). Section `"Transaction History"`, empty `"No transactions yet"`.

**CREATE & SYNC**
- [ ] T5. Create `HERMES-CUST-001`: Name, Phone `+92 300 1234001`, WhatsApp `+92 300 1234001`, Email `qa.cust@example.com`, Address `QA House 1`. `"Save"`.
- [ ] T6. Force Sync → Sheet `Customers`: A UUID, B name, C phone, D whatsapp, E email, F address, G `loyalty_points`=0, H `lifetime_spend`=0, I `is_deleted`=0, J blank, K `"synced"`, L pos_terminal_id, M/N timestamps.
- [ ] T7. Create `HERMES-CUST-002` (name+phone only).

**READ & SYNC**
- [ ] T8. Search `HERMES-CUST` → rows; search blank → full list. Select HERMES-CUST-001 → StatBox all zeros.

**UPDATE & SYNC**
- [ ] T9. `"Edit Customer"` → change name to `HERMES-CUST-001-EDIT`, add Email → Sheet same row B + E updated, K pending→synced.
- [ ] T10. Khata entries: on HERMES-CUST-001, `"Add Credit (-)"` → dialog `"Add Credit"` + fields `"Amount"` (numeric filter) + `"Note (Optional)"` + `"Save"`/`"Cancel"`. Enter `500.00`, note `HERMES-KHATA-001`. Then `"Record Payment (+)"` → `"Record Payment"` dialog, amount `200.00`, note `HERMES-KHATA-002`.
- [ ] T11. Force Sync → Sheet `Khata_Events` 2 rows: A UUID, B customer_id, C `event_type` = `"UDHAAR"` / `"JAMA"` (NOT credit/debit), D amount `500.0` / `200.0`, E note, F `reference_sale_id` null, G `is_deleted`=0, K `"synced"`, L pos_terminal_id, M/N timestamps.
- [ ] T12. StatBox now: `"Total Credit"` 500.00, `"Total Paid"` 200.00, `"Balance Due"` 300.00. History rows: label `"Credit"` (UDHAAR) / `"Payment"`, `"$500"` / `"$200"`, note, date `dd MMM yyyy, hh:mm a`.

**UPDATE & SYNC**
- [ ] T13. Add a second credit 100 → Balance Due 400. Verify counts and ledger order (append-only, newest last).

**DELETE & SYNC**
- [ ] T14. `"Delete Customer"` → soft delete + reload. Force Sync → Sheet `Customers` I=1, J set. Verify khata events REMAIN (append-only ledger, no cascade).
- [ ] T15. Verify a deleted customer's Balance Due path — record whether statement still opens.

**Edge cases**
- [ ] E1. Save with name only (phone blank) → `"Save"` disabled. Record.
- [ ] E2. Phone `abc` → accepted. Record.
- [ ] E3. Amount `abc` → parse fails → silent. Record.
- [ ] E4. Amount `0` → ViewModel rejects `amount <= 0` silently. Record.
- [ ] E5. Amount `-50` → rejected silently. Record.
- [ ] E6. Add Credit with NO customer selected → VM returns silently. Record.
- [ ] E7. Loyalty: make a sale with HERMES-CUST-002 attached → Sheet `Customers` G `loyalty_points` += total×rate(1f), H `lifetime_spend` += total.
- [ ] E8. Unicode customer name `ग्राहक-हर्म्स` → record create + Sheet cell.
- [ ] E9. Customer with 500+ transactions → record ledger scroll perf.

---

## F5.2 EXPENSES (ExpenseScreen + ExpenseViewModel)

**Navigation:** POS → Menu → `"Expenses"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Daily Expenses"`, action desc `"Add Expense"`, FAB desc `"Add"`, empty state `"No expenses recorded recently."`.
- [ ] T2. Dialog `"Log New Expense"` / `"Edit Expense"`: label `"Category:"` with FilterChips EXACT options `"Rent"`, `"Electricity"`, `"Wages"`, `"Maintenance"`, `"Misc"` (default `"Rent"`); fields `"Amount"` (numeric filter), `"Description"`; buttons `"Save Expense"` / `"Cancel"`.
- [ ] T3. Row: category bold, description, `"dd MMM, hh:mm a"` date, `"- $<amount>"` red, icons desc `"Edit Expense"` / `"Delete Expense"`.

**CREATE & SYNC**
- [ ] T4. Log `HERMES-EXP-001`: Category `Electricity`, Amount `1234.56`, Description `QA bill`. Save → row appears.
- [ ] T5. Force Sync → Sheet `Expenses`: A UUID, B `Electricity`, C `1234.56`, D `QA bill`, E epoch ts, F `logged_by_user_id` (user email or `"cashier"`), G `is_deleted`=0, H blank, I `"synced"`, J pos_terminal_id, K/L timestamps.

**READ & SYNC**
- [ ] T6. List window = last 7 days only (`loadExpenses`: now − 7d → now). Create an expense, then record whether an older expense (8 days) appears — expected: hidden.

**UPDATE & SYNC**
- [ ] T7. `"Edit Expense"` → change amount `2345.67`, description `QA bill edited` → Sheet same row C/D updated in place.

**DELETE & SYNC**
- [ ] T8. `"Delete Expense"` — NO confirm dialog (direct soft delete). Sheet G=1, H set, row retained.

**Edge cases**
- [ ] E1. Amount `abc` → parse fails → Save blocked silently. Record.
- [ ] E2. Amount `0` → VM rejects (`amount <= 0`). Record.
- [ ] E3. Amount `-10` → rejected. Record.
- [ ] E4. Blank description → VM rejects. Record.
- [ ] E5. No till open: expense save → `deductExpenseFromSession` exception swallowed (`"Non-fatal: expense already saved, no open session to deduct from"`). Record till `expected_cash` unchanged.
- [ ] E6. Till OPEN: save expense → till `expected_cash` decreased by amount. Verify in `Till_Sessions`.
- [ ] E7. Rapid double-save → record duplicate rows.

---

## F5.3 TRANSACTION HISTORY (HistoryScreen + HistoryViewModel)

**Navigation:** POS → Menu → `"Transaction History"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Transaction History"`, search placeholder `"Search by Invoice ID or QR UUID"`.
- [ ] T2. Filter chips EXACT: `"All"`, `"Today"`, `"Yesterday"`, `"Last 7 Days"`. NO payment/cashier/date-picker filters (verified absent).
- [ ] T3. Sale row: `"Invoice: <8chars>"` (red container `0xFFFFEBEE` if `total < 0`), `"Payment: <method>"`, `"REFUNDED"` label (red 12sp bold, when total < 0), amount green/red, date `dd MMM yyyy, hh:mm a`, Print icon desc `"Print Duplicate"`.
- [ ] T4. Footer: `"Load More"` TextButton (or spinner) — pageSize `30`.

**CREATE & SYNC**
- [ ] T5. With sales from F2.10 (CASH 220), F2.9 T6 (SPLIT 220), F2.10 E3 (UDHAAR): `"Today"` chip lists all 3; `"Yesterday"` lists none (if today); `"Last 7 Days"` lists all.

**READ & SYNC**
- [ ] T6. Search by full invoice UUID → found (matches `invoiceId.contains(query)` OR `systemRowId.contains(query)`); search by 4-char prefix → found; search `xyz` → empty (in-memory filter of loaded pages only).

**UPDATE & SYNC**
- [ ] T7. `"Load More"`: with >30 sales, verify pagination appends (offset = page×30) and `"Load More"` disappears when no more.

**DELETE & SYNC**
- [ ] T8. No delete of history rows — confirm absence → `[PASS]`. (Refunds create negative rows, F5.4.)

**Edge cases**
- [ ] E1. Search query typed while on `"Today"` → filter + search combined (both apply). Record.
- [ ] E2. Refunded sale row: `"Invoice: <8chars>"` + `"REFUNDED"` + amount `-$220.00`. Record exact.
- [ ] E3. Duplicate print for a sale only present in Sheet (ReprintReceiptUseCase sheet lookup, tabs matching `startsWith("Sales_")` sorted desc, `readRange("$tab!A:Y")`, row[0]==invoiceId) → record local vs remote fetch.
- [ ] E4. printStatus card shows `printStatus` + `"Dismiss"` button.

---

## F5.4 RETURNS & REFUNDS (ReturnsScreen + ReturnsViewModel)

**Navigation:** POS → Menu → `"Returns & Refunds"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Returns & Refunds"`, back desc `"Back"`, search field label `"Scan QR or Enter Invoice UUID"`, status card + `"Dismiss"`.
- [ ] T2. Found-invoice card: `"Invoice Found"`, `"Date: <dd MMM yyyy, hh:mm a>"`, `"Total: $<total>"` bold, `"Payment Method: <method>"`, section `"Select Return Reason:"`, buttons `"Return to Inventory"` (primary, reason `"Restock"`) and `"Mark as Wastage"` (pink `0xFFE57373`, reason `"Damaged/Wastage"`).
- [ ] T3. Confirm dialog: title `"Confirm Refund (<reason>)"`, body `"This will issue a reverse transaction for $<total>. The items will <be returned to stock|NOT be returned to stock>."`, buttons `"Issue Refund"` (red) / `"Cancel"`.
- [ ] T4. Empty states: idle `"Scan a customer receipt QR code to begin process."`; searched-not-found `"No invoice found with that ID."`.

**CREATE & SYNC**
- [ ] T5. Refund the CASH `220` sale (HERMES-PROD-001 ×2): scan receipt QR (or paste invoice UUID). `"Return to Inventory"` → `"Issue Refund"` → status `"Refund Processed Successfully."`.
- [ ] T6. Force Sync → Sheet `Sales_MMM_YYYY` NEW row: H `total`=`-220`, E `subtotal`=`-200`, F `tax`=`-20`, I method=`"CASH"`, P `reference_id`=`"REFUND_OF_<originalInvoiceId>_Restock"`, items_json has negative quantities, U `"synced"`.
- [ ] T7. Sheet `Inventory` PROD-001 stock restored +2 (original 10 − 2 + 2 = 10).
- [ ] T8. Sheet `Till_Sessions`: `total_refunds` = 220.

**READ & SYNC**
- [ ] T9. History shows refund row as REFUNDED (F5.3 E2). CRM statement shows nothing (no customer attached).

**UPDATE & SYNC**
- [ ] T10. Partial refund NOT possible (full-invoice only). Verify absence of item/qty selectors → `[PASS]`.

**DELETE & SYNC**
- [ ] T11. No undo/void of a refund — confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. **EXPECTED FAILURE (XF — dead code):** `"Mark as Wastage"` passes reason `"Damaged/Wastage"` but VM tests `"Damaged"` → wastage branch NEVER executes. Test: tap `"Mark as Wastage"` → `"Issue Refund"` → verify NO `Wastage_Ledger` row is created, and items are NOT restocked (negative sale only). Record actual. Also verify the `"Damaged"` expected: `WastageEntity(reason="DAMAGED", notes="Sales return — damaged (Refund of <invoice>)")`.
- [ ] E2. Refund with network OFF → negative sale saved locally, stock restored locally; syncs later. Record.
- [ ] E3. Refund a REFUNDED invoice → record.
- [ ] E4. Invoice UUID not found → `"No invoice found with that ID."`.
- [ ] E5. QR content: if QR encodes the UUID (`sync_uuid`), fallback `getSaleByInvoiceId` path handles it. Record both entry modes.
- [ ] E6. Batch product refund: if product has batches → oldest? NO — code picks `maxByOrNull { it.createdAt }` (newest active batch) and increments it; then recalc totalStock. Verify which batch got the +2.

---

## F5.5 ACCOUNT STATEMENT (StatementScreen + StatementViewModel)

**Navigation:** CRM customer → `"WhatsApp Statement"` → `statement_screen/{customerId}` (or `statement/{customerId}`).

**Field/Variable Level Test**
- [ ] T1. Title `"Account Statement"`. Header: name, phone, `"Net Balance: $<baqaya>"` (red if <0 else green `0xFF4CAF50`). Section `"Transaction History"`; rows `"Credit"` (UDHAAR) / `"Payment"`, date `dd MMM yyyy, hh:mm a`, `"Note: <note>"`, `"$<abs amount>"`. FAB `"Send via WhatsApp"` (green `0xFF25D366`).

**CREATE & SYNC**
- [ ] T2. For HERMES-CUST-001 (baqaya 300): statement lists 2 transactions; Net Balance `$300.00` (red).

**READ & SYNC**
- [ ] T3. WhatsApp FAB → `https://api.whatsapp.com/send?phone=<phone>&text=<encoded>` with message literal: `"*Account Statement - Tillzo POS*\n"`, `"Customer: <name>\n"`, `"Current Balance: *$300.00*\n\n"`, `"Recent Transactions:\n"`, lines `"- <dd MMM, hh:mm a>: Credit (-) $500"` / `"Payment (+)"` + `" (<note>)"`, last `"\n_System Generated by Tillzo POS_"`. Only last 10 events. Record.

**UPDATE & SYNC**
- [ ] T4. No date fields / no customer selector (customerId from route) — confirm absence → `[PASS]`.

**DELETE & SYNC**
- [ ] T5. No Share/Print/PDF buttons beyond WhatsApp FAB — confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. Customer with 0 transactions → empty history, balance 0 (green).
- [ ] E2. Customer phone blank → FAB opens WhatsApp without phone param. Record.
- [ ] E3. Route `statement/{customerId}` (duplicate route) vs `statement_screen/{customerId}` — record navigation from CRM uses which.
- [ ] E4. >10 events → only 10 in message.

---

## F5.6 TIME CLOCK (PunchClockScreen + PunchClockViewModel)

**Navigation:** POS → Menu → `"Time Clock"` (route `"time_clock"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Employee Time Clock"`. Badge `"IN"`/`"OUT"`; text `"You are clocked IN"` / `"You are clocked OUT"`; subtitle `"Punch in at the start of your shift, punch out at the end"`.
- [ ] T2. Button `"Punch IN"` (green, Login icon) / `"Punch OUT"` (red, Logout icon). Section `"Recent Activity"`, empty `"No punches recorded yet"`. Row: employee_name (or email), date `dd MMM yyyy, hh:mm a` (Locale.US).
- [ ] T3. NO fields (email/name/note inputs verified absent), NO timer display (verified absent).

**CREATE & SYNC**
- [ ] T4. Tap `"Punch IN"` → status `"Clocked IN ✓ (synced on next sync)"`, badge `"IN"`.
- [ ] T5. Force Sync → Sheet `Time_Clock`: A UUID, B employee_email, C employee_name (email prefix before `@`), D `"IN"`, E epoch, F note null, G pos_terminal_id, H/I timestamps, J `"synced"`.

**READ & SYNC**
- [ ] T6. Tap `"Punch OUT"` → `"Clocked OUT ✓ (synced on next sync)"`, badge `"OUT"`. Sheet row D=`"OUT"` (new row, append-only).

**UPDATE & SYNC**
- [ ] T7. Double Punch IN while clocked IN → record behavior (no guard found in VM).

**DELETE & SYNC**
- [ ] T8. No delete — confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. Punch with network OFF → local insert, syncs later. Record.
- [ ] E2. Rapid IN→OUT within 1 second → record timestamps.
- [ ] E3. Fresh install (no user email) → `employee_email = "user_1"`. Record when testing fresh state.

---

## F5.7 VERIFY RECEIPT QR (VerifyQrScreen + VerifyQrViewModel)

**Navigation:** POS → Menu → `"Verify Receipt QR"` (route `"verify_qr"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Verify Receipt QR"`. States: Idle `"Scan a receipt QR code to verify it"`; no permission `"Camera permission required to scan"`; Scanning `"Verifying receipt..."` + spinner; refresh icon `"Scan Again"` (when not Idle).
- [ ] T2. Verified card: `"✓ RECEIPT VERIFIED"` + rows `"Invoice"` (sync_uuid.take(12)), `"Total"` (raw), `"Payment"`, `"Time"`, `"Terminal"`. Invalid: `"❌"`, `"Receipt NOT FOUND in local records"`, `"QR: <take(24)>"`, `"Note: this device may not have synced the sale yet."`.

**CREATE & SYNC**
- [ ] T3. Scan a receipt QR from F2.10 → `"✓ RECEIPT VERIFIED"` with matching invoice/total/payment/time/terminal.

**READ & SYNC**
- [ ] T4. Scan the QR of a sale made on ANOTHER device (in Sheet but not local) → expected `"Receipt NOT FOUND in local records"` (local-only lookup: `getSaleByInvoiceId ?: getSaleById`). Record.

**UPDATE & SYNC**
- [ ] T5. `"Scan Again"` → returns to Scanning.

**DELETE & SYNC**
- [ ] T6. No expired state exists (verified absent) — confirm → `[PASS]`.

**Edge cases**
- [ ] E1. Blank QR → VM returns silently. Record.
- [ ] E2. QR of a refunded (negative) sale → record verified state + total display.
- [ ] E3. Camera permission denied → `"Camera permission required to scan"`.
- [ ] E4. Scan a non-QR barcode (CODE128) → camera restricted to `FORMAT_QR_CODE`; record behavior.

---

## F5.8 Z-REPORT & DAY CLOSE (ZReportScreen + ZReportViewModel)

**Navigation:** POS → Menu → `"Z-Report / Day Close"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Z-Report & System Health"`. Sync warning card (errorContainer): `"Unsynced Data Pending"` + `"You have <n> items waiting. Cannot close till until uploaded."`.
- [ ] T2. `"Today's Summary"`: `"Gross Sales:"`, `"Expenses:"` (`"- $<x>"` red), `"Expected Cash in Drawer:"`.
- [ ] T3. `"Till / Cash Drawer"` card rows EXACT: `"Opened At"`, `"Opening Cash"`, `"Cash Sales"`, `"Expected in Drawer"`, `"Pay In (Added)"`, `"Pay Out (Removed)"`, `"Total Transactions"`, `"Physical Cash Count ($)"`, `"Variance:"`.
- [ ] T4. Buttons: `"Close Till & End Shift"` (enabled when `pendingSync == 0 && count non-blank`), `"CLOSE DAY (Z-REPORT)"` (enabled when `pendingSync == 0`).
- [ ] T5. Dialog `"Confirm Day Close"`: text `"Count the cash in your drawer and enter the total below."`, `"Physical Cash Count ($)"`, `"Expected:"`, `"Variance:"`, `"This action cannot be undone."`, buttons `"Confirm & Close"` / `"Cancel"`.

**CREATE & SYNC**
- [ ] T6. With a till session (opening 1000, cash sales 220, expense 1234.56 on same session): verify Gross Sales, Expenses, Expected values. Physical count `1100.00` → variance computed.
- [ ] T7. `"Close Till & End Shift"` → Z-Report prints: `"        == Z-REPORT ==        "`, `"Date:"`, `"POS ID:"`, `"Cash Sales:"`, `"Card Sales:"`, `"Wallet Sales:"`, `"Credit Sales:"`, `"Gross Sales:"`, `"Expenses:"`, `"Expected Cash:"`, `"Physical Cash:"`, `"Cash Variance (OVERAGE|SHORTAGE):"`, `"Status: RECONCILED"`, `"NET IN DRAWER:"`, `"    DAY CLOSED COMPLETELY    "`. Strings: `"Day Closed Successfully! Printing Z-Report..."`.
- [ ] T8. Sheet `Till_Sessions`: status=`"RECONCILED"`, `closing_cash`, `net_cash`, `closed_at` set, in-place update.
- [ ] T9. CSV auto-export: header `"invoice_id,timestamp,items_count,subtotal,tax,discount,total,payment_method"`, file `TillzoPOS_Sales_<yyyyMMdd>.csv` in Downloads. String `"Day Closed + CSV exported: <fileName>"`.

**READ & SYNC**
- [ ] T10. After close, POS till gate shows `"Register Session Closed"` + `"This till has been reconciled. Open a new till to continue selling."`.
- [ ] T11. `"CLOSE DAY (Z-REPORT)"` without opening count → enabled when pendingSync==0; record behavior.

**UPDATE & SYNC**
- [ ] T12. Pending sync > 0 → both buttons disabled + warning card visible; string `"Error: Cannot close day. <n> items pending sync."`.

**DELETE & SYNC**
- [ ] T13. No date-range fields, no refunds section, no per-method table, no Share/PDF buttons (CSV auto + auto-print only) — confirm absences → `[PASS]`.

**Edge cases**
- [ ] E1. Physical count `abc` → record.
- [ ] E2. Physical count negative → record.
- [ ] E3. Variance OVERAGE vs SHORTAGE literal — verify exact wording in printout for both signs.
- [ ] E4. Close with printer off → `"Closed, but Print Failed: <e.message>"` (day still closes).
- [ ] E5. Close twice → record (second close blocked by no open session).
- [ ] E6. Report window = local midnight → now. A sale at 23:59:59 yesterday vs 00:00:00 today — verify boundary inclusion.
- [ ] E7. `errorMessage` card shown when reportStatus starts with `"Error"` (errorContainer).

---

## F5.9 STORE MODULE KNOWN-BUG CHECKPOINTS

- [ ] XF1. Returns `"Mark as Wastage"` dead branch (see F5.4 E1) — confirm and record Sheet outcome.
- [ ] XF2. History search filters only already-loaded pages (`pageSize 30`) — a match on page 3 requires scrolling. Record.
- [ ] XF3. `expenseDao` deduction from till happens on SAVE only when a session is open; the exception is swallowed. Record behavior parity.
- [ ] XF4. Statement WhatsApp message: `Credit (-)` vs `Payment (+)` — verify sign convention vs `Balance Due` sign (baqaya red when <0). Record consistency.
- [ ] XF5. VerifyQR has no "expired" state and no sheet fallback — record.
