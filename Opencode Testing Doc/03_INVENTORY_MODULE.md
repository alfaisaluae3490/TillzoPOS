# 03 — INVENTORY MODULE: PRODUCT CRUD / CATEGORIES / UNITS / BATCHES / ALERTS / WASTAGE / OCR / QR / LABELS / ADJUSTMENT

**Files under test:** `ui/inventory/InventoryModule.kt`, `options/crud/InventoryCrudScreen.kt` + `InventoryCrudViewModel.kt`, `CategoryManagementScreen.kt`, `ProductUnitsScreen.kt`, `StockAdjustmentScreen.kt`, `module_a/BatchListBottomSheet.kt`, `options/alerts/StockAlertsScreen.kt` + `LowStockViewModel.kt`, `options/wastage/WastageLogScreen.kt` + `WastageViewModel.kt`, `options/ocr/OcrEntryScreen.kt` + `OcrEntryViewModel.kt`, `options/qr/QrGeneratorScreen.kt` + `QrGeneratorViewModel.kt`, `options/crud/BarcodePrintSettingsScreen.kt`

**Sheet tab under test:** `Inventory` (25 columns, see `01_SHEET_SCHEMA_AND_API` §1.2).
**Column map reminder:** A=system_row_id, B=barcode_id, C=name, D=sku, E=category, F=brand, G=description, H=cost_price, I=selling_price, J=tax_percent, K=unit, L=stock_qty, M=low_threshold, N=batch_number, O=expiry_date, P=manufacturing_date, Q=expiry_alert_days, R=is_damaged, S=damaged_qty, T=is_deleted, U=deleted_at, V=sync_status, W=pos_terminal_id, X=created_at, Y=updated_at.

---

## F3.1 PRODUCT CREATE (InventoryFormDialog — "Add Product")

**Navigation:** POS → Menu → `"Inventory"` (or Inventory icon) → `InventoryCrudScreen` (title `"Inventory Management"`).

**Field/Variable Level Test**
- [ ] T1. Top bar has: Back (desc `"Back"`), `"Categories"` button (Category icon), `"Units"` button, camera IconButton (desc `"Smart AI Entry (OCR)"`), FAB `+` (desc `"Add Item"`).
- [ ] T2. Tap FAB → dialog title `"Add Product"`. Fields present (Section 1 "Basic Info"): `"Product Name *"`, `"SKU *"` + `"Generate"` button (sets `SKU-<last6 of millis>`), `"Main Category *"` (readOnly dropdown, placeholder `"Select Main Category"`), `"Subcategory"` (readOnly, placeholder `"Select Subcategory"`, only when main has children), `"Description"`.
- [ ] T3. Section 2 "GTINs (Barcodes)": list of gtins each with Delete icon (desc `"Remove GTIN"`); field `"Add GTIN (Leave blank for Auto)"` + `"Add"` button.
- [ ] T4. Section 3 "Pricing & Tax": `"Cost Price"`, `"Selling Price"`, `"Tax %"` (all Number keyboard).
- [ ] T5. Section 4 "Stock": `"Current Stock"`, `"Low Alert"`, `"Unit (KG/ML/PC)"` readOnly dropdown (first item `"Manage Units..."`).
- [ ] T6. Section 5 "Batch & Expiry": `"Batch Number *"`, `"Mfg Date"` (readOnly, DatePicker), `"Exp Date *"` (readOnly, DatePicker), `"Expiry Alert (Days Before)"`.
- [ ] T7. Section 6 "Damaged Stock": shown only in EDIT mode: Switch `"Mark as having damaged items"` + `"Damaged Qty"`.

**CREATE & SYNC**
- [ ] T8. Create `HERMES-PROD-001`: Name `HERMES-PROD-001`, SKU `QA-SKU-001`, Main Category `HERMES-CAT-001` (create first in F3.2), GTIN blank (auto), Cost `50`, Selling `100`, Tax `10`, Stock `10`, Low Alert `2`, Unit `PC`, Batch `HERMES-BATCH-001`, Mfg Date = today, Exp Date = tomorrow (or +30d), Expiry Alert `30`. Tap `"Save"`.
- [ ] T9. After save the dialog closes and the product card appears. Record the auto-generated GTIN from the card (`"Barcode: <gtin>"`). Verify it equals `"0000000" + %07d(itemNumber)` (14 digits).
- [ ] T10. Force Sync → Sheet `Inventory` row for HERMES-PROD-001. Verify cells: A UUID, B = auto GTIN, C `HERMES-PROD-001`, D `QA-SKU-001`, E `HERMES-CAT-001`, F blank, G blank, H `50`, I `100`, J `10`, K `PC`, L `10`, M `2`, N `HERMES-BATCH-001`, O = exp date `yyyy-MM-dd`, P = mfg date, Q `30`, R `0`/`false`, S `0`, T `0`/`false`, U blank, V `"synced"` (or `"pending"` → re-check after Force Sync), W = first 20 chars of spreadsheet id, X/Y = epoch millis.
- [ ] T11. Create `HERMES-PROD-002` (KG): Name `HERMES-PROD-002`, SKU `QA-SKU-002`, Cost `150`, Selling `250`, Tax `0`, Stock `5.5`, Low Alert `1`, Unit `KG`, Batch `HERMES-BATCH-002`. Save. Verify Sheet row L = `5.5`, K = `KG`.

**READ & SYNC**
- [ ] T12. Navigate away and back to Inventory → cards render from Room: name bold, badge dot color logic (green = OK), `"QA-SKU-001 • HERMES-CAT-001"`, `"$100 / PC"`, `"Stock: 10"`, `"Barcode: <gtin>"`.
- [ ] T13. Second device/other-sheet test (delta sync): in the Google Sheet, edit row of HERMES-PROD-001 cell C to `HERMES-PROD-001-REMOTE` and `updated_at` (col Y) to a future epoch. Within 60s delta poll, app must show updated name (local pending rows win — this row is `synced` so remote applies). Verify `Inventory` tab `updated_at` bumped.

**UPDATE & SYNC**
- [ ] T14. Tap product card → `"Edit Product"` dialog. Change: Name `HERMES-PROD-001-EDIT`, Selling `110`, Stock `7`, Description `EDITED VIA QA`. `"Save"`.
- [ ] T15. Force Sync → verify ONLY these cells changed in the SAME row: C=`HERMES-PROD-001-EDIT`, G=`EDITED VIA QA`, I=`110`, L=`7`; verify A,B,D,E,H,J,K,M,N,O,P,Q,R,S,T,U unchanged; Y (updated_at) bumped.
- [ ] T16. Edit WITHOUT changing anything → tap `"Save"` → verify no new row in Sheet (in-place update only) and no duplicate.
- [ ] T17. Change unit PC→KG on save → record whether `unit` cell updates and whether decimal step logic on POS changes.

**DELETE & SYNC**
- [ ] T18. Tap Delete icon (desc `"Delete"`, red) on HERMES-PROD-001-EDIT card. **NO confirmation dialog exists** — record whether deletion is instant.
- [ ] T19. Force Sync → Sheet row: T (`is_deleted`) = `1`, U (`deleted_at`) = epoch, V (`sync_status`) = `"synced"`. Row must NOT be physically removed.
- [ ] T20. Verify card gone from app list; verify it is NOT re-inserted by delta sync (local pending delete wins).
- [ ] T21. Recreate product with SAME name/SKU → must create a NEW row with a NEW system_row_id (UUID). Record.

**Edge cases (create path)**
- [ ] E1. Tap `"Save"` with only Name filled (SKU blank) → silent: nothing happens, no error string (validation is silent: `name.isNotBlank() && sku.isNotBlank() && category.isNotBlank() && batch.isNotBlank() && expDate.isNotBlank() && price>=0 && cost>=0 && stock>=0 && threshold>=0`). Record.
- [ ] E2. Name blank, everything else valid → silent no-op. Record.
- [ ] E3. Expiry date in the PAST → accepted (DatePicker has NO range validation). Record observed.
- [ ] E4. Mfg date AFTER expiry date → accepted. Record.
- [ ] E5. Selling Price `-10` → `price.toDoubleOrNull() ?: 0.0` → `-10 >= 0` fails → silent no-op. Record.
- [ ] E6. Selling Price `abc` → coerced `0.0`, allowed. Record.
- [ ] E7. Tax `1000` → allowed; record tax math on POS (`cartTax` = total×1000% → 10x total).
- [ ] E8. Expiry Alert `-5` → `expAlert.toIntOrNull() ?: 30` → `-5` stored. Record.
- [ ] E9. GTIN manually entered `123` (too short) → accepted as-is; record whether barcode validation exists (BarcodeHelper has NO checksum validation — expect accepted).
- [ ] E10. GTIN with letters `ABC123` → accepted. Record.
- [ ] E11. Two identical GTINs via `"Add"` twice → record duplicate entries in list.
- [ ] E12. Stock `999999999.99` → record.
- [ ] E13. Offline create: Airplane mode ON → create product → saved locally (pending badge +1) → OFF + Force Sync → row appears. Record.
- [ ] E14. Long name (200 chars) → record.
- [ ] E15. Unicode/emoji name `हर्ड-आटा-🧪` → record create + Sheet cell.
- [ ] E16. Tap `"Cancel"` mid-form → no record created.

---

## F3.2 CATEGORY MANAGEMENT (hierarchy + two-step selector)

**Navigation:** Inventory → `"Categories"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Categories"`, FAB desc `"Add Category"`, empty state `"No categories yet"` / `"Tap + to add your first category"`.
- [ ] T2. Form dialog titles: `"Add Main Category"` / `"Add Subcategory"` / `"Edit Category"`. Fields: `"Category Name *"`, `"Parent Category"` dropdown with options `"None (Main Category)"` + existing main categories (excluding self).

**CREATE & SYNC**
- [ ] T3. Add `HERMES-CAT-001` (main). Then add subcategory `HERMES-SUBCAT-001` under it (via `"Add Subcategory"` on the parent row). Cards show hierarchy (subcategory indented).
- [ ] T4. Force Sync → Sheet `Categories` tab: row1 A=UUID, B=`HERMES-CAT-001`, C blank (parent null), V=`synced`; row2 B=`HERMES-SUBCAT-001`, C = parent UUID.

**READ & SYNC**
- [ ] T5. In `"Add Product"` form → `"Main Category *"` dropdown lists HERMES-CAT-001; selecting it reveals `"Subcategory"` dropdown with `"Select Subcategory"` + HERMES-SUBCAT-001. Select sub → verify product form `category` var = subcategory name (selectedSubCategory name).

**UPDATE & SYNC**
- [ ] T6. `"Edit"` on HERMES-CAT-001 → rename to `HERMES-CAT-001-RENAMED` → verify Sheet cell B updated in place; verify product cards referencing old name keep old string (category stored by name, not id — record behavior).
- [ ] T7. `"Add Subcategory"` under a subcategory → record whether 3rd level is allowed.

**DELETE & SYNC**
- [ ] T8. `"Delete"` on HERMES-CAT-001-RENAMED → dialog `"Delete Category?"` + body `""HERMES-CAT-001-RENAMED" and all its subcategories will be removed."` (or `"..." will be removed."` when no children). Confirm `"Delete"` → children cascade soft-deleted FIRST. Verify Sheet `Categories`: both rows `is_deleted`=1 (col D), `deleted_at` set; main row deleted before children in log.

**Edge cases**
- [ ] E1. `"Delete"` dialog `"Cancel"` → nothing deleted.
- [ ] E2. Empty category name → silent no-op (`addCategory` returns if blank). Record.
- [ ] E3. Duplicate category name → allowed (no uniqueness check). Record.
- [ ] E4. 200-char category name → record.
- [ ] E5. In-form inline `"Manage Categories"` dialog: `"New Category"` field + `"Add Category"` icon + `"Delete Category"` icons + `"Close"` — verify add/delete work inside the Add Product dialog without leaving the form.

---

## F3.3 PRODUCT UNITS

**Navigation:** Inventory → `"Units"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Units of Measure"`, FAB desc `"Add Unit"`. Defaults present: `Piece→PC`, `Kilogram→KG`, `Gram→G`, `Liter→L`, `Milliliter→ML`, `Box→BOX`, `Dozen→DZ`.
- [ ] T2. Dialog `"Add Custom Unit"`: fields `"Unit Name (e.g. Packet)"`, `"Abbreviation (e.g. PKT)"` (auto-uppercases), buttons `"Add"`/`"Save"`, `"Cancel"`.

**CREATE & SYNC**
- [ ] T3. Add `HERMES-UNIT` / `HU`. Force Sync → Sheet `Product_Units` tab (camelCase headers!): A `unitId` UUID, B `unitName`=`HERMES-UNIT`, C `abbreviation`=`HU`, D `isDeleted`=false, E `syncStatus`=`"synced"`, F/G timestamps.

**READ & SYNC**
- [ ] T4. In `"Add Product"` form unit dropdown → `HERMES-UNIT (HU)` appears; select → product saved with unit `HU`.

**UPDATE & SYNC**
- [ ] T5. `"Edit Unit"` (edit icon shown only for non-default units) → rename abbreviation to `HX` → Sheet cell C updated in place.

**DELETE & SYNC**
- [ ] T6. Delete icon exists only for non-defaults. Delete HERMES-UNIT → Sheet row `isDeleted`=true (no physical removal). Default units have NO delete/edit → record.
- [ ] T7. Verify a unit in use by a product cannot be deleted without impact — record app behavior.

**Edge cases**
- [ ] E1. Blank unit name → record.
- [ ] E2. Abbreviation lowercase `pkt` → verify auto-uppercase to `PKT`.
- [ ] E3. Abbreviation > 10 chars → record.

---

## F3.4 PRODUCT BATCHES (BatchListBottomSheet)

**Navigation:** product card with batches → `"View Batches (N)"` TextButton.

**Field/Variable Level Test**
- [ ] T1. Sheet header `"Product Batches"`. Row shows `"Batch: <batchNumber>"`, `"Stock: <stockQty>"`, `"Cost: <costPrice> | Selling: <sellingPrice>"`, `"Expiry: <expiryDate>"`, Edit icon (desc `"Edit Batch"`), `"+ Batch"` button. Empty state `"No batches found for this product."`.
- [ ] T2. `"Add New Batch"` dialog: `"Batch Number (optional)"`, `"Mfg Date (YYYY-MM-DD)"` (plain text, NO picker), `"Expiry Date (YYYY-MM-DD)"`, `"Stock Qty *"` (Decimal), `"Cost Price"` (Decimal), buttons `"Save"`/`"Cancel"`.

**CREATE & SYNC**
- [ ] T3. On HERMES-PROD-001 (currently stock 7 after F3.1 edits): `"+ Batch"` → Batch `HERMES-BATCH-ADD`, Mfg today, Exp +60d, Stock `3`, Cost `45` → Save. Verify product `totalStock` display updates (7→10) and `"View Batches (10)"`.
- [ ] T4. Force Sync → Sheet `Product_Batches`: A `batch_id` UUID, B product_id, C barcode, D `HERMES-BATCH-ADD`, E mfg, F exp, G `3`, H `45`, I `0` (selling not entered → 0), J `is_active`=1, K `is_deleted`=0, L blank, M `"synced"`, N pos_terminal_id, O/P timestamps.
- [ ] T5. Verify `Inventory` row stock_qty (col L) = `10` (recalculated sum of active batches).

**READ & SYNC**
- [ ] T6. Reopen batch sheet → `HERMES-BATCH-ADD` row present with stock 3.

**UPDATE & SYNC**
- [ ] T7. `"Edit Batch"` → dialog `"Edit Batch"` seeded with values; change Stock Qty to `5`, Selling Price `120`. Save → `recalculateTotalStock` → product stock `12`. Verify Sheet `Product_Batches` cell G=`5` in place, `Inventory` L=`12`.

**DELETE & SYNC**
- [ ] T8. Batch rows have NO delete UI and NO active toggle (verified absent). Confirm absence → `[PASS]`. Deactivation happens automatically: run a sale that empties a batch → `deactivateBatch` sets `is_active`=0 when stock ≤ 0. Test: sell the full `HERMES-BATCH-ADD` (5 units) via POS → verify Sheet `Product_Batches` row J (`is_active`)=0 and product stock recalculated.

**Edge cases**
- [ ] E1. Blank batch number → auto `"BATCH-<millis%100000>"`. Record generated value.
- [ ] E2. Stock Qty `abc` → coerced to existing batch stock (`?: batch.stockQty`). Record.
- [ ] E3. Stock Qty `-5` → record.
- [ ] E4. Expiry date free text `not-a-date` → accepted. Record.
- [ ] E5. Add batch to a product with `hasBatches=false` → record whether flag flips to true and delta sync re-fetch.

---

## F3.5 STOCK ALERTS

**Navigation:** Inventory badge chips or POS banner → `Stock Alerts`.

**Field/Variable Level Test**
- [ ] T1. Title `"Stock Alerts"`. Tabs: `"Low Stock (N)"` / `"Out of Stock (N)"` / `"Expiring (N)"`. Inventory screen chips: `"⚠️ N Low"`, `"🚫 N Out"`, `"⏰ N Expiring"`.
- [ ] T2. Card lines: `item_name` / `"<category>  •  <sku>"` / `"Stock: <n> <unit>  |  Threshold: <t>"`. Badges: `"LOW"` (`#FFC107`), `"OUT"` (`#E53935`), `"EXPIRED"`, `"<n> days"` (`#FF6F00`).

**CREATE & SYNC**
- [ ] T3. Create product `HERMES-PROD-003` (stock `1`, threshold `5`, expiry tomorrow) → tabs must count it: Low Stock (1 ≤ 5), Expiring (within 30 days).
- [ ] T4. Create product `HERMES-PROD-004` (stock `0`) → Out of Stock tab count +1.

**READ & SYNC**
- [ ] T5. Expiry logic: product with `expiry_date` = today → `"EXPIRED"` badge; +29 days → `"29 days"`; +31 days → NOT in list.

**UPDATE & SYNC**
- [ ] T6. Edit HERMES-PROD-003 stock to `10` → Low Stock count drops.

**DELETE & SYNC**
- [ ] T7. Soft-delete HERMES-PROD-004 → Out tab count drops.

**Edge cases**
- [ ] E1. Threshold `0` and stock `0` → appears in BOTH Low and Out tabs. Record.
- [ ] E2. No restock button exists (verified absent) — tapping a card navigates to Inventory. Confirm absence → `[PASS]`.
- [ ] E3. Empty states exact: `"No low stock items 🎉"` / `"No out-of-stock items 🎉"` / `"No expiry alerts 🎉"`.

---

## F3.6 WASTAGE LOG

**Navigation:** POS → Menu → `"Wastage Entry"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Wastage Log"`, FAB desc `"Log Wastage"`. Summary cards: `"Today's Loss"`, `"Month Loss"`, `"Today's Items"` (value `"<n> wasted"`).
- [ ] T2. Filter chips: `"All"` + `EXPIRED`, `DAMAGED`, `THEFT`, `OTHER`. Empty state `"No wastage records"`.
- [ ] T3. Dialog `"Log Wastage"`: `"Search Product"` (result rows `"<name> (<unit>) — Stock: <n>"`), `"Reason"` dropdown (options EXACT: `EXPIRED`, `DAMAGED`, `THEFT`, `OTHER`; default `DAMAGED`), `"Quantity (<unit>)"` (Number), `"Notes (optional)"`, buttons `"Log Wastage"`/`"Cancel"`.

**CREATE & SYNC**
- [ ] T4. On HERMES-PROD-001 (stock 12): log `HERMES-WAST-001`: quantity `2`, reason `EXPIRED`, notes `QA wastage`. Save → product stock drops to `10`.
- [ ] T5. Force Sync → Sheet `Wastage_Ledger`: A `wastage_id` UUID, B product_id, C product name, D `batch_id` = null (no batch selector exists), E batch_number null, F `2`, G unit, H cost price, I `total_loss` = 2 × cost, J `EXPIRED`, K notes, L logged_by, M `wastage_date` today, N `"synced"`, O pos_terminal_id, P/Q timestamps.

**READ & SYNC**
- [ ] T6. List row: productName / `"2 PC  •  <date>"` / notes / reason chip / `"Loss: $<loss>"`.

**UPDATE & SYNC**
- [ ] T7. No edit/undo on entries (verified absent). Confirm absence → `[PASS]`.

**DELETE & SYNC**
- [ ] T8. No delete on entries (verified absent). Confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. Quantity `0` → `"Log Wastage"` fires only if qty > 0; record disabled/blocked state.
- [ ] E2. Quantity `-3` → record.
- [ ] E3. Quantity greater than current stock (`99` on stock 10) → record behavior (no stock guard found in VM).
- [ ] E4. Search product with no results → record.
- [ ] E5. Reason filter `THEFT` shows only THEFT rows.

---

## F3.7 OCR ENTRY ("Smart AI Entry")

**Navigation:** Inventory top bar camera icon (desc `"Smart AI Entry (OCR)"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Smart AI Entry (OCR)"`, overlay `"Point camera at the product label/weight"` + spinner. No fields/buttons (back arrow only).

**CREATE & SYNC**
- [ ] T2. Show the app a weight label (e.g. `"500g"`) → regex match on `mg|g|kg|ml|l|oz` (case-insensitive) → auto-returns with `savedStateHandle["ocr_scanned_weight"]` → `"Add Product"` dialog opens with name prefilled `"500g Product"`. Record.

**READ & SYNC**
- [ ] T3. Show a label with NO weight (`"Chicken"`) → record (expected: no match, stays on scanner).

**UPDATE & SYNC**
- [ ] T4. Label `"1.5 kg"` → name `"1.5 kg Product"`? Record exact prefill (weightUnit is the raw matched text).

**DELETE & SYNC**
- [ ] T5. Back arrow mid-scan → returns to Inventory, no prefill.

**Edge cases**
- [ ] E1. Label with weight AND product name (`"Atta 2kg"`) → record prefill (expect `"2kg Product"`).
- [ ] E2. No camera permission → record error.

---

## F3.8 QR GENERATOR (label print for a barcode ID)

**Navigation:** product card → QR icon (desc `"Print QR Code"`) → route `barcode_print_settings/{item_id}`.

**Field/Variable Level Test**
- [ ] T1. Route `"qr/{barcodeId}"` also exists (`QrGeneratorScreen`). Title `"Print QR Label"`, line `"Label for ID: <barcodeId>"`. No fields (fixed 512×512 QR), single button `"Print to TSPL Printer"`.

**CREATE & SYNC**
- [ ] T2. Verify QR bitmap renders (QR encoding of the barcode ID).
- [ ] T3. Tap `"Print to TSPL Printer"` → status `"Printing..."` → `"Print Success!"`; printer must output 40×30mm label: `SIZE 40 mm, 30 mm` + `BARCODE 10,60,"128",...` (`TsplPrinter`).

**READ & SYNC**
- [ ] T4. No printer → `"Print Failed. Check Printer connection."` / `"Error: <msg>"`. Record.

**UPDATE & SYNC**
- [ ] T5. No edit path — record absent.

**DELETE & SYNC**
- [ ] T6. No delete — record absent.

**Edge cases**
- [ ] E1. Empty barcodeId on route → record.

---

## F3.9 BARCODE PRINT SETTINGS + GS1 LABEL PDF (BarcodePrintSettingsScreen)

**Navigation:** Inventory → QR icon (route `"barcode_print_settings/{item_id}"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Barcode Print Settings"`, FAB `"Generate PDF (<qty> labels)"`, share chooser title `"Print GS1 Label"`.
- [ ] T2. Fields: `"Print Quantity"`, `"Currency"`, `"Width (e.g., 144)"`, `"Height (e.g., 72)"`, `"Company Name"`.
- [ ] T3. Buttons: `"Upload Logo"`/`"Change Logo"`, `"Clear Logo"`, `"Add Custom Application Identifier (AI)"`, `"Restore Default Settings"`.
- [ ] T4. Sliders (each labeled `<name>: <value>`): `"Item Title Size"` (4–16), `"Barcode Size"` (24–72), `"Logo Size"` (4–30), `"Company Name Text Size"` (3–15), position sliders `"Logo X/Y"`, `"Name X/Y"`, `"Title X/Y"`, `"Price X/Y"`, `"SKU X/Y"`, `"Serial Number (SN) X/Y"`, `"GTIN X/Y"`, `"LOT X/Y"`, `"EXP X/Y"`, `"Barcode X/Y"`.
- [ ] T5. Toggles: `" Bold Title"`, `"Show Logo"`, `"Show Name"`, `"Enable Prefix"`, `"Enable Suffix"`, `"Use FNC1 Separator (~1)"`.
- [ ] T6. Prefix/Suffix: `"Prefix Value"` (default `]d2`) + `"Position"`; `"Suffix Value"` + `"Position"`.
- [ ] T7. Field cards: `"(<aiCode>) <fieldName>"`, `"Value: <customValue|Static>"`, toggles `"Enabled"`, `"FNC1 Separator (~1)"`, buttons `"Move Up"`, `"Move Down"`, `"Delete"`. Built-in fields (undeletable): `GTIN`(AI `01`), `EXPIRY`(AI `17`), `BATCH`(AI `10`, FNC1 ON), `SN`(AI `21`), `SKU`(AI `240`, disabled).
- [ ] T8. Custom AI dialog `"Add Custom AI Field"`: `"AI Name (e.g. Additional SKU)"`, `"AI Code (e.g. 240)"`, `"Value (Static text)"`, toggle `"Append FNC1 Separator (~1)"`, `"Add"`/`"Cancel"`.

**CREATE & SYNC**
- [ ] T9. Set Qty `2`, Width `144`, Height `72`, add custom AI `HERMES-AI` code `240` value `QA-STATIC` → Generate PDF → file `GS1_Label_<sku>.pdf` in cacheDir (verify via share sheet). PDF pages: company name, `SN: <15-char serial>`, DataMatrix (GS1 string starts `]d2` + `01<14-digit gtin>17<yyMMdd>10<batch>~1 21<serial>`), price, SKU, LOT, EXP lines per defaults.
- [ ] T10. Verify GS1 string: serial format `[HHmmss][SKU6][index3]` length 15; GTIN zero-padded to 14.

**READ & SYNC**
- [ ] T11. Reopen screen → all settings persisted (auto-save to `barcode_prefs`).
- [ ] T12. `"Restore Default Settings"` → all values back to defaults.

**UPDATE & SYNC**
- [ ] T13. Disable `EXPIRY` field, move `BATCH` to first → Generate → GS1 order changes to `10 BATCH ~1 01 GTIN 21 SN`.
- [ ] T14. Enable Prefix with value `PRE` position `2` → GS1 contains `PRE` after char 2. Suffix `SUF` position `100` → appended at end.

**DELETE & SYNC**
- [ ] T15. `"Delete"` on custom AI → removed; on built-in → record (expected: blocked, `fieldId in [GTIN,EXPIRY,BATCH,SN,SKU]` guard).
- [ ] T16. `"Clear Logo"` → logo removed from PDF.

**Edge cases**
- [ ] E1. Qty `0` → record.
- [ ] E2. Qty `-1` → record.
- [ ] E3. Qty `1000` → record PDF generation time/perf (onProgress callback).
- [ ] E4. Width `0`/Height `0` → record.
- [ ] E5. Upload non-image file as logo → record.
- [ ] E6. Blank company name → record.

---

## F3.10 STOCK ADJUSTMENT

**Navigation:** POS → Menu → `"Stock Adjustment"` (route `"stock_adjustment"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Stock Adjustment"`. Sections `"Select Product"` / `"Adjustment Details"`.
- [ ] T2. Product picker: `"Search by name, SKU, or barcode"` + `"Select"`/`"Change"` buttons; result `"✓ <name>"`, `"Stock: <n> <unit>"`.
- [ ] T3. Fields: `"Adjustment Type"` dropdown with EXACT options: `"RECEIVED"`→`"Received stock"`, `"CORRECTION"`→`"Stock correction"`, `"DAMAGED"`→`"Damaged / wastage"`, `"RETURNED"`→`"Customer return"`, `"SALE_RETURN"`→`"Sale return"` (default `RECEIVED`). `"Quantity Change (use − for negative)"`, `"Reason *"`. Supporting text `"Current: <n> <unit>"` + `"New stock: <clamped> <unit>"` (`coerceAtLeast(0.0)`).

**CREATE & SYNC**
- [ ] T4. On HERMES-PROD-001 (stock 10): type `RECEIVED`, qty `4`, reason `QA-STOCKADJ-001` → `"Save Adjustment"`. App list stock = 14.
- [ ] T5. Force Sync → Sheet `Stock_Adjustments`: A `adjustment_id` UUID, B product_id, C `RECEIVED`, D `4`, E reason, F adjusted_by, G `"synced"`, H pos_terminal_id, I/J timestamps. And `Inventory` L=14.

**READ & SYNC**
- [ ] T6. Negative: type `CORRECTION`, qty `-3`, reason `QA-STOCKADJ-002` → app stock 11, Sheet D=`-3`, `Inventory` L=11.

**UPDATE & SYNC**
- [ ] T7. No edit path — record absent.

**DELETE & SYNC**
- [ ] T8. No delete path — record absent.

**Edge cases**
- [ ] E1. Qty `abc` → record (expect parse fail → silent).
- [ ] E2. Qty blank → record.
- [ ] E3. Qty `-999` on stock 11 → new stock clamps to 0; verify Sheet D=`-999` and app shows 0.
- [ ] E4. Reason blank → `"Reason *"` required? Record whether save blocked.
- [ ] E5. Save with no product selected → record.
- [ ] E6. `SALE_RETURN` type → verify same mechanics.

---

## F3.11 INVENTORY LIST: SEARCH + FILTERS + DELETE + BADGES

**Field/Variable Level Test**
- [ ] T1. Search placeholder `"Search items..."` (leading Search icon). Filter chips from `ProductFilter` enum, labels: `"ALL"`, `"LOW STOCK"`, `"OUT OF STOCK"`, `"NEAR EXPIRY"`, `"EXPIRED"`, `"DAMAGED"`.

**CREATE & SYNC**
- [ ] T2. With products above, verify counts per chip match the `getProductsUseCase` filters: LOW STOCK = stock ≤ threshold; OUT OF STOCK = stock ≤ 0; NEAR EXPIRY = expiry within `expiry_alert_days` (30 default); EXPIRED = expiry < today; DAMAGED = is_damaged_stock.

**READ & SYNC**
- [ ] T3. `"DAMAGED"` chip: mark HERMES-PROD-001 damaged (edit → toggle `"Mark as having damaged items"` + Damaged Qty `2`) → appears under DAMAGED; Sheet R (`is_damaged`)=1, S (`damaged_qty`)=2.

**UPDATE & SYNC**
- [ ] T4. Card badge dot: red if `current_stock <= 0` OR expired; yellow if `<= low_stock_threshold`; orange near-expiry; green OK. Create a product for each state and verify dot colors.

**DELETE & SYNC**
- [ ] T5. Immediate delete (no confirm) verified in F3.1 T18. Also verify sync: after delete, pending badge +1 until Force Sync; `deleteItem` triggers `triggerManualSync()` automatically — record whether row syncs without manual Force Sync.

**Edge cases**
- [ ] E1. Search + filter combined → record.
- [ ] E2. `"View Batches (N)"` on a product with no batches → record.
- [ ] E3. 200 products → scroll performance record.
