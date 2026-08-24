# 10 — EXTERNAL AI AUDIT REPORTS: FULL ANALYSIS, FINDINGS & CONCLUSIONS

**Source folder:** `Other Reports\`
**Analyzed files (read end-to-end, no line skipped):**
1. `Deconstructing TillzoPOS_ A Zero-Abstraction, Line-by-Line Blueprint for Full Application Testing.md` (125 lines)
2. `TillzoPOS_Hermes_Testing_Protocol.md` (458 lines)

**How to use this file:** this is a meta-analysis for the QA lead. Hermes executes files 02–09 + 11. This file tells you which parts of the two external reports are trustworthy, which are wrong, and what net-new test value they add.

---

## A. REPORT 1 ANALYSIS — "Deconstructing TillzoPOS… Blueprint" (125 lines)

### A.1 What it actually contains
- A methodology proposal only. NO field inventory, NO column mapping, NO endpoints, NO defects, NO test data. Every concrete example is hypothetical (`<button id="saveProductBtn">`, "Wireless Bluetooth Headphones", `productPriceInput`, `/api/export/products`).
- Five phases: I Micro-Mapping → II CRUD Protocol → III Edge-Case Audit → IV Backend Sync/Integrity → V Final Checklist. Structure mirrors the user's original brief.

### A.2 Findings (stack mismatch — NOT executable)
| ID | Finding | Severity |
|---|---|---|
| R1-1 | **Wrong technology stack assumed.** Report assumes a WEB app: HTML/TSX/Vue/Angular DOM (`<button>`, `<input id=...>`), Redux/Zustand state, Express/NestJS + TypeORM backend, Zod validation, `credentials.json`/`.env`. Actual app is native Android (Kotlin, Jetpack Compose, Room, Retrofit). None of its "scanner" instructions apply. | CRITICAL |
| R1-2 | **Wrong source repository.** Cites `github.com/alfaisaluae3490/TillzoPOS`; local tree is `C:\Users\Faisal Khan\Desktop\Tillzo`. Report never opened the local source. | HIGH |
| R1-3 | **Zero concrete findings.** All "example target" values are invented (IDs like `productNameInput`, `Sheet1!B:B`). Cannot be used for literal execution. | HIGH |
| R1-4 | **Assumes browser execution environment** (Chrome DevTools, network throttling, F5 refresh) — irrelevant to a native app (use Airplane mode + proxy, as in my file 08). | MEDIUM |

### A.3 Salvageable methodology (adopted into file 11)
1. **Header-schema validation** — verify each sheet tab's row 1 headers exactly match the official column list.
2. **Cross-sheet referential integrity** — FK-style checks (product's category exists in `Categories` tab; PO's `vendor_id` exists in `Vendors` tab).
3. **Data-transformation checks** — how booleans (e.g. `is_active`) serialize into sheet cells; exact string value.
4. **Raw-value preservation** — edit form must show the raw number (e.g. `1299.99`), not formatted display.
5. **Bulk-op row-count integrity** — before/after row counts.
6. **Conditional-UI testing principle** — test every conditional state of every element.
7. **PASS/FAIL/BLOCKED reporting discipline** — already adopted in file 00.

### A.4 Conclusion (Report 1)
- **Do not execute.** Non-applicable stack, hypothetical targets, wrong repo.
- Keep only its 7 methodology ideas above (now in `11_SUPPLEMENTAL_TEST_STEPS.md`).
- No defect findings to merge into `09_KNOWN_DEFECTS_REGISTRY.md`.

---

## B. REPORT 2 ANALYSIS — "TillzoPOS_Hermes_Testing_Protocol" (458 lines)

### B.1 What it actually contains
- Stack-accurate protocol (Kotlin/Compose/Room/Sheets v4). Claims fields/labels/columns "copied verbatim" from `toSyncMap()` + Compose labels.
- Phase 1: 19 table→column maps, 9 REST endpoints, 46 UI screens inventory.
- Phase 2: 19 CRUD modules with exact sheet verification.
- Phase 3: empty-field (15), wrong-type (~10), timeout/failure (7), cross-module integrity (5) test lists.
- 2 explicit bug expectations: PO `currency` edge case; restore path "previously broken (reverse sync)".

### B.2 Findings — CRITICAL discrepancies vs the actual code (my ground-truth map in file 01)
The report derives column lists from entity `toSyncMap()` and assumes **sheet tab name = Room table name**. The real writer is `SheetColumns` (in `utils/Constants.kt`, the declared "Single Source of Truth for Google Sheet Column Ordering") and `SheetsRepository.createWorkspace` tab list. Consequences:

| ID | Finding | Evidence / impact | Severity |
|---|---|---|---|
| R2-1 | **WRONG TAB NAMES in report.** Report tells Hermes to verify tabs `inventory`, `KhataEvents`, `grn_headers`, `grn_items`, `product_batches`, `product_units`, `purchase_orders`, `purchase_order_items`, `vendors`, `till_sessions`, `wastage_log`, `StockAdjustments`, `Sales`. Actual tabs created: `Inventory`, `Khata_Events`, `GRN_Headers`, `GRN_Items`, `Product_Batches`, `Product_Units`, `Purchase_Orders`, `PO_Items`, `Vendors`, `Till_Sessions`, `Wastage_Ledger`, `Stock_Adjustments`, `Sales_MMM_YYYY`. | A Hermes run using report 2 alone would fail every sheet verification (tab-not-found). | CRITICAL |
| R2-2 | **Inventory column list mismatched.** Report: `…item_number, barcode_id, name, category, unit, selling_price, price, stock_qty…total_stock, has_batches…last_updated…`. `SheetColumns.INVENTORY` (actual write order): `system_row_id, barcode_id, name, sku, category, brand, description, cost_price, selling_price, tax_percent, unit, stock_qty, low_threshold, batch_number, expiry_date, manufacturing_date, expiry_alert_days, is_damaged, damaged_qty, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`. Extra/missing fields: no `item_number`, no duplicate `price`, no `total_stock`, no `has_batches`, no `last_updated` in the SHEET. | Report's sheet column assertions (e.g. verifying `selling_price` AND `price`) would mislead. | HIGH |
| R2-3 | **KhataEvents column list mismatched.** Report adds `event_id, pos_id, type, sync_uuid`; actual sheet = `system_row_id, customer_id, event_type, amount, note, reference_sale_id, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`. | Same risk. | HIGH |
| R2-4 | **GRN header/item lists mismatched.** Report: `po_number, vendor_phone, received_by_name, total_items, total_received_qty, is_deleted` (headers) + `batch_id, category_id, brand, ordered_qty, selling_price, low_stock_threshold` (items). Actual: `GRN_HEADERS` = `grn_id, grn_number, po_id, vendor_id, vendor_name, status, notes, received_by, total_amount, sync_status, pos_terminal_id, attached_file_id, attached_file_url, created_at, updated_at`; `GRN_ITEMS` = `grn_item_id, grn_id, po_item_id, product_id, product_name, barcode_id, sku, received_qty, unit_cost_price, total_cost, unit, batch_number, manufacturing_date, expiry_date, inventory_action, is_new_item, sync_status, created_at, updated_at`. | Report's GRN verification would reference non-existent columns. | HIGH |
| R2-5 | **Till_Sessions mismatch.** Report lists camelCase raw entity fields incl. `totalSplitSales, cashVariance, totalPayIn, totalPayOut, posId`. Actual sheet columns: `session_id, cashier_id, cashier_name, pos_terminal_id, opening_cash, closing_cash, expected_cash, total_cash_sales, total_card_sales, total_wallet_sales, total_udhaar_sales, total_sales_count, total_refunds, net_cash, status, notes, shift_date, opened_at, closed_at, sync_status, created_at, updated_at`. | No `cash_variance`, `total_pay_in`, `total_pay_out`, `pos_id` columns on the sheet. Report's Z-Report assertion (`cashVariance` col) is unverifiable. | HIGH |
| R2-6 | **StockAdjustments mismatch.** Report: camelCase raw (`adjustmentId, productId…`). Actual: `adjustment_id, product_id, adjustment_type, quantity_changed, reason, adjusted_by, sync_status, pos_terminal_id, created_at, updated_at`. | Same risk. | MEDIUM |
| R2-7 | **Unverified UI items.** `Try Again` button on SignInScreen (not found in code); `PaymentDialog` label `UDHAAR` (actual field label is `Credit Amount`); `ProductUnits` tab in report = `product_units` (actual `Product_Units`). | Minor; use my files 02–07 as label ground truth. | LOW |
| R2-8 | **Tab-list omission.** Report's 19 tables do not include `Returns`, `Dashboard`, `BarcodeGeneralConfigs`, `BarcodeFieldConfigs`, `Settings`, `Sync_Log`, `SYS_DB_DO_NOT_TOUCH`, `Product_Units` variants — actual workspace has 24 tabs (file 01 §1.1). | Partial tab map. | LOW |
| R2-9 | **Restore flagged "previously broken (reverse sync)"** — the report asserts the restore/delta-sync path was historically broken. My map found no such marker; treat as a claim to re-verify (see file 11 step S-3.x). | Verification target, not a confirmed defect. | INFO |

### B.3 Genuinely net-new test value (not in files 02–09 → now in file 11)
1. Cross-sheet referential integrity checks (category → Categories, PO vendor → Vendors).
2. Transformation check: exact cell representation of booleans (`is_active`, `is_damaged`).
3. Raw-value preservation in edit forms.
4. GRN void/reversal: double-decrement or silent no-op probe; second-GRN remaining-balance edge; further-receipt blocked.
5. StockAdjustments: confirm write-once ledger (no edit/delete UI).
6. Batch delete → `is_deleted`/`is_active` flip + parent `total_stock` decrease (my F3.4 T8 covers no-delete-UI; report adds expected flip check for the "delete" path via POS auto-deactivate).
7. Vendor: inactive vs deleted treated distinctly in PO picker.
8. Sales: verify each payment method individually (Cash / Card / Digital Wallet / UDHAAR), UDHAAR → KhataEvents cross-module.
9. Restore robustness: no navigation before restore completes; kill mid-restore; error surfaced AND logged.
10. Workers: NightlyBackup file produced; ExpiryCheckWorker matches `⏰ Expiring` chip count; shard row-count before/after.
11. Idempotency: retried append must not double-append (verify via `system_row_id`).
12. 429 rate-limit backoff behavior.
13. Kill app mid-sync → `sync_status` stays `pending` (never falsely `synced`).
14. Soft-delete "resurrection" check: stale read must not reset `is_deleted` to 0.
15. Cross-module integrity: delete category with assigned items; delete vendor with open POs; delete customer with khata history; delete product with batches/POs/Sales history; GRN against cancelled PO blocked.
16. Self-lockout: app must prevent deleting/downgrading the last admin.
17. Edge inputs not in my F8.2: `Expiry Alert` decimal "2.5"; phone `12ab34`; email without `@`; PIN >4 digits; malformed MAC (no colons); IP octet >255; invalid Sheet ID string; `Print Quantity` 0/negative; `Credit Limit` negative.
18. Empty-field additions: Save user with no Role selected; Khata Save with no customer selected.

### B.4 Conclusion (Report 2)
- **Executable-quality structure, but with tab/column ground-truth errors** (R2-1…R2-6). If Hermes runs report 2 without file 01 corrections, most sheet verifications fail falsely.
- Correct the discrepancies by always resolving tab names + column order from file 01, not from report 2's tables.
- Its unique edge cases are merged into `11_SUPPLEMENTAL_TEST_STEPS.md` (18 items above) — run those as part of the main protocol.
- No new confirmed defects beyond my `09` registry; its PO-currency edge case matches DEF-02; its restore claim (R2-9) becomes a re-verification step, not a defect.

---

## C. FINAL VERDICT (both reports)

| Criterion | Report 1 (Blueprint) | Report 2 (Hermes Protocol) |
|---|---|---|
| Stack accurate | NO (web app assumed) | YES |
| Literal field/column inventory | NO (hypothetical) | PARTIAL (real but stale/wrong in 6 areas) |
| Executable as-is | NO | NO (needs tab/column corrections) |
| Defect findings | 0 | 0 confirmed (2 suspicions → matched to my DEF-02; restore claim → re-verify) |
| Net-new test value | 7 methodology ideas | 18 concrete test steps |
| Final disposition | Archive only; methodology adopted | Corrected + merged via file 11; reference for cross-checks |
