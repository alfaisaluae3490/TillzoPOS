# 11 — SUPPLEMENTAL TEST STEPS (net-new items extracted from external audits)

**Origin:** items 1–18 below extracted from `Other Reports/TillzoPOS_Hermes_Testing_Protocol.md` (Phase 2 modules 7,8,12,15,16,19 + Phase 3 §3.B/3.C/3.D); items S-19+ from `Other Reports/Deconstructing…Blueprint.md` (Phase IV methodology). Execute as an add-on pass after files 02–09. Use the `HERMES-` tagging and PASS/FAIL logging rules from `00_INDEX.md`.

**Ground-truth rule (CRITICAL):** never resolve a sheet tab or column from the external reports — resolve from `01_SHEET_SCHEMA_AND_API.md` §1.1/§1.2 (tab `Inventory` not `inventory`, `Wastage_Ledger` not `wastage_log`, `Stock_Adjustments` not `StockAdjustments`, `Till_Sessions` not `till_sessions`, `Purchase_Orders`/`PO_Items` not `purchase_orders`/`purchase_order_items`, `Product_Units` not `product_units`, `GRN_Headers`/`GRN_Items` not `grn_headers`/`grn_items`, `Khata_Events` not `KhataEvents`, `Sales_MMM_YYYY` not `Sales`).

---

## S-1 CROSS-SHEET REFERENTIAL INTEGRITY (Report 1 methodology; Report 2 §3.D)

- [ ] S-1.1. Pick any product `HERMES-PROD-001` in `Inventory` tab; take its `category` cell (col E). Search the `Categories` tab for the same string in col B (`category_name`). PASS = found in ≥1 row with `is_deleted`=0; FAIL = orphan value; record.
- [ ] S-1.2. Take any PO's `vendor_id` (col C of `Purchase_Orders`). Search `Vendors` tab col A (`vendor_id`). PASS = exact match found. Record.
- [ ] S-1.3. Take any GRN's `po_id` (col C of `GRN_Headers`). Search `Purchase_Orders` col A. PASS = match found.
- [ ] S-1.4. Take any `Khata_Events.customer_id` (col B). Search `Customers` col A. PASS = match found (even if customer soft-deleted).

## S-2 DATA TRANSFORMATION CHECKS (Report 1 methodology)

- [ ] S-2.1. Vendor `is_active`: create vendor with Active ON → check exact cell value in `Vendors` col I (`is_active`). Record literal (expect `1` or `true`). Toggle OFF → record literal again. Compare against Customer/Product boolean cells (`is_deleted`, `is_damaged` in `Inventory` cols T/R) — record the app's consistent boolean representation.
- [ ] S-2.2. Raw-value preservation: open Edit dialog for a product with Selling Price `1234.56` → the price field must contain `1234.56` (raw), NOT `$1,234.56` (formatted). PASS if raw. Same check for `Credit Limit` on vendor edit.

## S-3 RESTORE PATH ROBUSTNESS (Report 2 §Module 19 — flagged "previously broken")

- [ ] S-3.1. Fresh install → select existing large sheet (≥500 rows) → verify Home is NOT reachable until restore completes (progress dialog `"Restoring cloud database..."` stays up; strings from `01` §1.8).
- [ ] S-3.2. Kill app mid-restore (adb `am force-stop`) → relaunch → restore must resume/restart via `initial_restore` worker; local DB must NOT be left half-populated and then treated as synced. Record end state.
- [ ] S-3.3. Restore failure (network off) → `"Restore Failed"` + `"Retry Restore"`; verify the error is ALSO in System Logs (tag/level recorded).
- [ ] S-3.4. After successful restore: verify `delta_cursor` row exists in `Sync_Log` tab col B with status `synced` (mirrors Room `sync_log`).

## S-4 GRN VOID / REVERSAL + REMAINING-BALANCE (Report 2 §Module 8)

- [ ] S-4.1. Confirm absence of GRN void/delete UI (already F4.5 T14). If a future build adds it: verify voiding reverses `Inventory.stock_qty` AND `Product_Batches.stock_qty` exactly once — probe for double-decrement and silent-no-op. Record current state (no void path exists).
- [ ] S-4.2. Partial receipt (18 of 20) → second GRN on the SAME PO for remaining 2 → PO must flip to `RECEIVED`. Then attempt a THIRD GRN on the same PO → record whether the app blocks it (expected: no explicit block found in code — record actual; this is a potential DEF-25 candidate).
- [ ] S-4.3. PO item `received_qty` accumulation: verify `PO_Items.received_qty` (col H) sums across multiple GRNs to the ordered value.

## S-5 WRITE-ONCE LEDGER VERIFICATIONS (Report 2 §Modules 4, 10, 13, 14)

- [ ] S-5.1. StockAdjustments: confirm NO edit and NO delete affordance (no icon, no swipe, no long-press). Verify via input simulation on each row. PASS = no path found.
- [ ] S-5.2. Expenses: confirm edit DOES exist (unlike report's assumption) → verify edited cell updates in `Expenses` in place (already F5.2 T7). Record.
- [ ] S-5.3. Wastage: confirm no edit/delete (already F3.6 T7/T8).
- [ ] S-5.4. Time_Clock punches: verify append-only — clock OUT creates a NEW row (not update of IN row) in `Time_Clock` (already F5.6 T6).

## S-6 VENDOR STATE SEMANTICS (Report 2 §Module 6)

- [ ] S-6.1. Create vendor Active=OFF, `is_deleted`=0 → must NOT appear in `CreatePurchaseOrderScreen` vendor suggestions (`getActiveVendors`), but MUST still appear in VendorManagement list with `"Inactive"` label. Record.
- [ ] S-6.2. Soft-deleted vendor (is_deleted=1) → absent from both. Record.

## S-7 SALES — EVERY PAYMENT METHOD INDIVIDUALLY (Report 2 §Module 11)

- [ ] S-7.1. CASH-only sale: `payment_method` col I = `"CASH"`, `cash_amount`=total, others 0, `payment_split_json` (col O) = `"{}"`.
- [ ] S-7.2. CARD-only sale: `"CARD"`, `card_amount`=total, col O `"{}"`.
- [ ] S-7.3. WALLET-only sale: `"WALLET"`, `wallet_amount`=total.
- [ ] S-7.4. UDHAAR-only sale with selected customer: `"UDHAAR"`, `udhaar_amount`=total, AND a `Khata_Events` row appears with `event_type`=`"UDHAAR"`, `amount`=total, `reference_sale_id`=sale `system_row_id`. Verify the two rows cross-match (cross-module dependency). Record.

## S-8 BACKUP / WORKER SCHEDULES (Report 2 §Module 19)

- [ ] S-8.1. Trigger `NightlyBackupWorker` manually (if schedulable) → verify backup file exists in `Documents/TillzoPOS` (naming per `01` §1.x / F8.5). Record.
- [ ] S-8.2. ExpiryCheckWorker: set an item with `expiry_date` = today+29 days, expiry_alert_days=30 → notification `"⏰ Expiring Soon: <name>"` / `"Expires in 29 days (<date>)"` AND `⏰ Expiring` chip count matches (cross-check F3.5). Record both.
- [ ] S-8.3. MonthlyShardWorker: count total rows in `Sales_MMM_YYYY` before/after a shard cycle (or simulate by setting ROW_LIMIT context if possible) → verify `ARCH_Sales_<prevMonth>` rename + no data loss (before total == after total across renamed+current tabs). Record.

## S-9 SYNC IDEMPOTENCY & RESILIENCE (Report 2 §3.C)

- [ ] S-9.1. Double-append probe: create a product, let it sync, then edit it → Force Sync → verify EXACTLY ONE row per `system_row_id` in `Inventory` tab (dedupe via col A). Record count.
- [ ] S-9.2. 429 rate-limit simulation (proxy returns 429): Force Sync with 5 pending rows → verify retry/backoff (WorkManager exponential 5s) and NO dropped rows after recovery. Record attempts.
- [ ] S-9.3. Kill app mid-sync: make 3 changes, start Force Sync, `am force-stop` within 2s → relaunch → verify each record's `sync_status` is `pending` (never falsely `synced` in Room/Data Viewer) and all 3 upload on next sync without duplicates.
- [ ] S-9.4. Delete-resurrection probe: soft-delete a product (is_deleted=1 pending) → BEFORE sync, edit the same `system_row_id` directly in the sheet (remote update, future `updated_at`) → Force Sync → verify local pending delete still wins and sheet row keeps `is_deleted`=1 (not reset to 0 by stale read). Record.

## S-10 CROSS-MODULE REFERENTIAL EDGE CASES (Report 2 §3.D)

- [ ] S-10.1. Delete a category that has assigned products → reopen those products → record crash vs sensible display (expected: products keep the category NAME string, no crash — category stored by name).
- [ ] S-10.2. Delete a vendor that has open POs → open `PODetailScreen` for those POs → must render vendor last-known name (no crash). Record.
- [ ] S-10.3. Delete a customer with khata history → `Khata_Events` rows must remain; `CrmScreen` must not crash when balance is computed. Record.
- [ ] S-10.4. Delete a product with active batches, PO items, and sales history → open PO detail + History + Wastage → no crash on resolved names/prices. Record.
- [ ] S-10.5. Attempt GRN against a PO that is NOT in SENT/PARTIALLY_RECEIVED state (e.g. after full receipt) → record whether the app blocks (no explicit block found in `CreateGrnScreen` guard — potential DEF-25 candidate).
- [ ] S-10.6. Self-lockout: in Admin & Users, attempt to delete or downgrade the LAST remaining Admin → record behavior (no guard found in `UserManagementViewModel` — potential DEF-25 candidate).

## S-11 INPUT EDGE ADDITIONS (Report 2 §3.A/3.B)

- [ ] S-11.1. `Expiry Alert (Days Before)`: input `2.5` → record coercion (`toIntOrNull` → fails → fallback 30). Input `0` → stored 0.
- [ ] S-11.2. Phone fields: `12ab34` on vendor/customer/user forms → record acceptance.
- [ ] S-11.3. Email without `@` (`notanemail`) on Add User + vendor/customer email → record validation (expected: none — silent).
- [ ] S-11.4. PIN >4 digits (`12345`) in Set Security PIN dialog → record (`"PIN must be exactly 4 digits."` expected).
- [ ] S-11.5. Printer MAC without colons (`001122334455`) → record Test Bluetooth behavior (no format validation expected).
- [ ] S-11.6. Printer IP octet >255 (`256.1.1.1`) → record Test Network behavior.
- [ ] S-11.7. Settings `Sheet ID or URL` = `not-a-sheet` → tap `"Connect"` → record (expected: silently no-op via `extractSheetId`).
- [ ] S-11.8. `Print Quantity` = `0` and `-1` on BarcodePrintSettings → record.
- [ ] S-11.9. Vendor `Credit Limit` = `-100` → record.
- [ ] S-11.10. Add User with no Role change (default `Cashier` kept) → record role cell in `Users_Permissions` col D.
- [ ] S-11.11. Khata `"Save"` with no customer selected → record silent no-op.
- [ ] S-11.12. Cart qty > `current_stock` with `block_negative_stock` OFF → record oversell allowed (contrast with F2.2 E3).
- [ ] S-11.13. Date fields: type free text `"soon"` into GRN `Mfg Date`/`Exp Date` (free-text fields) → record accepted; exp < mfg chronological violation → record accepted (no validation expected).

## S-12 REPORT-2 CLAIM RE-VERIFICATION (due-diligence on its assertions)

- [ ] S-12.1. SignInScreen has NO `"Try Again"` button — verify absence (report 2 listed it). PASS = absent.
- [ ] S-12.2. `PaymentDialog` does NOT show a field labeled `UDHAAR` — the credit field is labeled `"Credit Amount"` and the toggle `"Credit"`. Verify. PASS = report wrong.
- [ ] S-12.3. `Product_Units` tab headers are camelCase (`unitId, unitName, abbreviation, isDeleted, syncStatus, createdAt, updatedAt`) — verify row 1 exactly. PASS = matches file 01.
- [ ] S-12.4. Restore claim "previously broken (reverse sync)": execute S-3.x and record whether restore is fully functional in THIS build. Result feeds DEF-25 discussion, not a confirmed defect.
