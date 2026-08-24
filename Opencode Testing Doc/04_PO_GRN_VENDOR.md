# 04 — PURCHASE ORDERS / GOODS RECEIPTS / VENDORS

**Files under test:** `ui/inventory/module_b/*` (CreatePurchaseOrderScreen, PODetailScreen, PurchaseOrderListScreen, VendorManagementScreen + 4 viewmodels), `ui/inventory/module_c/*` (CreateGrnScreen, GrnDetailScreen, GrnListScreen, GrnSuccessScreen + 2 viewmodels), `domain/usecase/po/*` (4 use cases)

**Sheet tabs under test:** `Purchase_Orders`, `PO_Items`, `GRN_Headers`, `GRN_Items`, `Vendors`, `Product_Batches`, `Inventory` (column maps in `01` §1.2).

**Status vocabulary:** `DRAFT`, `SENT`, `PARTIALLY_RECEIVED`, `RECEIVED`, `CANCELLED`; GRN: `DRAFT`, `CONFIRMED`.

---

## F4.1 VENDOR CRUD (VendorManagementScreen)

**Navigation:** POS → Menu → `"Vendors"`.

**Field/Variable Level Test**
- [ ] T1. Title `"Vendors"`, FAB desc `"Add Vendor"`, search placeholder `"Search vendors..."`, empty state `"No vendors yet"` / `"Tap + to add your first vendor"`.
- [ ] T2. Form dialog `"Add Vendor"` / `"Edit Vendor"` (full-screen, accordion `"Basic Info"` with Person icon). Fields EXACT: `"Name *"`, `"Phone *"` (Phone kb), `"WhatsApp"`, `"Email"` (Email kb), `"Address"` (minLines 2), `"City"`, `"Credit Limit"` (Decimal, default `"0.0"`), `"Active Status"` Switch (default ON for new).
- [ ] T3. Bottom row: `"Cancel"` (TextButton) + `"Save"` (Button, enabled only when `name.isNotBlank() && phone.isNotBlank()`); saving shows `"Saving vendor..."` + LinearProgressIndicator.

**CREATE & SYNC**
- [ ] T4. Create `HERMES-VEND-001`: Name, Phone `+1 555 0100001`, WhatsApp `+1 555 0100001`, Email `qa.vendor@example.com`, Address `QA Street 1`, City `Karachi`, Credit Limit `5000.00`, Active ON. `"Save"` → dialog closes, card shows avatar letter, `"Active"` label green, phone, address.
- [ ] T5. Force Sync → Sheet `Vendors`: A `vendor_id` UUID, B name, C phone, D whatsapp, E email, F address, G city, H `5000.0`, I `is_active`=1, J `is_deleted`=0, K `"synced"`, L/M timestamps.
- [ ] T6. Create `HERMES-VEND-002` (Active OFF) → I=0, card shows `"Inactive"` grey.

**READ & SYNC**
- [ ] T7. Search `HERMES-VEND` → both rows; search `zzz` → empty.
- [ ] T8. Vendor appears in CreatePO vendor search (`"Search or type vendor name"`): only ACTIVE vendors in suggestions (`getActiveVendors`).

**UPDATE & SYNC**
- [ ] T9. Edit HERMES-VEND-001: name `HERMES-VEND-001-EDIT`, Credit Limit `9999.99`, Active OFF → `"Save"`. Sheet: B renamed, H=`9999.99`, I=`0`, same row (no new row).

**DELETE & SYNC**
- [ ] T10. `"Delete"` (trash icon) → dialog `"Delete Vendor"` + body `"Delete HERMES-VEND-001-EDIT? This marks the vendor as deleted locally and removes them from your spreadsheet on next sync."`, buttons `"Delete"` (red `0xFFE53935`) / `"Cancel"`. Confirm → card gone.
- [ ] T11. Force Sync → Sheet `Vendors`: J (`is_deleted`)=1, K `"synced"`. Row NOT physically removed.
- [ ] T12. `"Cancel"` on dialog → vendor stays.

**Edge cases**
- [ ] E1. Name filled, phone blank → `"Save"` disabled (silent). Record.
- [ ] E2. Phone `abc` → accepted (Phone kb only, no validation). Record.
- [ ] E3. Credit Limit `abc` → `creditLimit.toDoubleOrNull() ?: 0.0` → 0.0 saved. Record.
- [ ] E4. Credit Limit `-100` → record.
- [ ] E5. Duplicate phone → allowed. Record.
- [ ] E6. Save with network OFF → saved locally (sync pending), then syncs. Record.
- [ ] E7. Delete failure path: offline delete → record pending behavior.

---

## F4.2 CREATE PURCHASE ORDER (CreatePurchaseOrderScreen)

**Navigation:** POS → Menu → `"Purchase Orders"` → FAB (desc `"Create PO"`).

**Field/Variable Level Test**
- [ ] T1. Title `"Create Purchase Order"` + back arrow. Bottom bar: `"Save Draft"` (OutlinedButton) + `"Save & Share"` (Button, Share icon). Both enabled ONLY when `selectedVendor != null && items.isNotEmpty()`.
- [ ] T2. Section `"Vendor"`: search field `"Search or type vendor name"` (Person icon), max 5 suggestions (name + phone), `"Add New Vendor"` TextButton → dialog `"New Vendor"` with `"Vendor Name *"`, `"Phone *"`, `"WhatsApp (optional)"`, buttons `"Add"`/`"Cancel"` (Add fires only if name AND phone non-blank). Selected vendor chip: CheckCircle + name + phone on blue 10%.
- [ ] T3. Section `"Order Details"`: `"Expected Delivery Date (YYYY-MM-DD)"` (readOnly, DatePickerDialog OK/Cancel, format `SimpleDateFormat("yyyy-MM-dd")`), `"Notes (optional)"` (minLines 2).
- [ ] T4. Section `"Items (N)"`: search field `"Search inventory to add item..."` (max 5 suggestions: name + sku + `"$<cost>"`), item cards with `"<i>. <productName>"`, Close X (red, removes), inline `"Qty"` (Decimal) + `"Cost"` (Decimal) + `"= $<totalCost>` computed. Summary (when items non-empty): `"Total Items:"` + `"Total Amount:"` `"$<total>"` (formatted `%,.0f`).

**CREATE & SYNC**
- [ ] T5. Search vendor `HERMES-VEND-002` (active) → select. Add items: HERMES-PROD-001 × `2` @ cost `50`; HERMES-PROD-002 × `1.5` @ cost `150`. Verify Total `$325` (`2×50 + 1.5×150`).
- [ ] T6. Tap `"Save Draft"` → navigates back to PO list. PO number auto-format `PO-<yyyyMM>-<NNNN>` (e.g. `PO-202608-0001`; count = `getTotalPOCount()+1`). Record the exact PO number.
- [ ] T7. Force Sync → Sheet `Purchase_Orders`: A `po_id` UUID, B `po_number` (recorded), C vendor_id, D vendor_name, E status=`"DRAFT"`, F notes, G `325.0`, H currency=`"$"` (**hardcoded — see XF in F4.8**), I expected delivery `yyyy-MM-dd`, J created_by=`"admin"`, K `"synced"`, L pos_terminal_id, M/N timestamps.
- [ ] T8. Sheet `PO_Items` rows (2): A `po_item_id` UUID, B `po_id` (matches header), C product_id, D product name, E sku, F barcode, G `ordered_qty` 2 / 1.5, H `received_qty` 0, I `unit_cost_price` 50 / 150, J `total_cost` 100 / 225, K unit, L `"synced"`, M/N timestamps.

**READ & SYNC**
- [ ] T9. PO list card: `poNumber` bold, status chip `"Draft"` grey `0xFF9E9E9E`, vendorName, `"$325"`, date `dd MMM yyyy`, `"Expected: <date>"`.

**UPDATE & SYNC**
- [ ] T10. Create a SECOND PO with the same vendor: PROD-001 ×1 @ `60` (change Cost to 60) → `"Save & Share"` → WhatsApp opens with share text containing `"PURCHASE ORDER"`, `"Vendor : HERMES-VEND-002"`, `"Phone  : ..."`, `"Delivery: <date>"`, item line `"  2? ..."`, `"TOTAL: $60"`, `"Notes: ..."`, `"Please confirm receipt of this order."`. After WhatsApp, PO saved with status=`"SENT"`. Record PO-2 number.
- [ ] T11. Verify Sheet PO-2: E status=`"SENT"`. Verify numbering sequential (PO-2 = PO-1 + 1).
- [ ] T12. Update qty inline `1` → `3` → total recalc; update cost → recalc. Record live recompute.

**DELETE & SYNC**
- [ ] T13. Item removal: Close X removes item + total recalc. Verify `PO_Items` in Sheet only gets items that exist at save time (removed item absent).
- [ ] T14. NO delete-PO UI exists (no cancel/delete button in Create or List screen). Confirm absence → `[PASS]`. `CANCELLED` status is unreachable from UI (see 09).

**Edge cases**
- [ ] E1. `"Save Draft"` with no vendor → buttons disabled (silent). Record.
- [ ] E2. `"Save Draft"` with vendor but 0 items → disabled. Record.
- [ ] E3. Qty field `abc` → `toDoubleOrNull` ignored silently (text kept, no add). Record.
- [ ] E4. Qty `0` → item added with orderedQty 0 → record Sheet value.
- [ ] E5. Qty `-2` → record.
- [ ] E6. Cost `0` → allowed. Record.
- [ ] E7. `"Add New Vendor"` with name only (phone blank) → `"Add"` must not fire. Record.
- [ ] E8. Vendor WhatsApp empty → `"Save & Share"` uses `vendor.phone`; BOTH empty → falls back to `https://api.whatsapp.com/send?text=<encoded>`. Record.
- [ ] E9. Expected delivery date in the past → accepted. Record.
- [ ] E10. Duplicate item added twice → two PO_Items rows (no merge). Record.
- [ ] E11. 50 items in PO → record performance.
- [ ] E12. Search suggestion for product shows cost formatted `%,.0f` → record for cost 1234.56.

---

## F4.3 PURCHASE ORDER LIST (PurchaseOrderListScreen)

**Field/Variable Level Test**
- [ ] T1. Title `"Purchase Orders"`, FAB desc `"Create PO"`. NO search bar (verified absent). Filter chips: `"All"`, `"Draft"`, `"Sent"`, `"Received"`, `"Cancelled"` mapping to statuses `[null, DRAFT, SENT, RECEIVED, CANCELLED]` — exact equality match.
- [ ] T2. Status chip colors/labels: `"Draft"`/grey, `"Sent"`/blue `0xFF1E88E5`, `"Partial Receipt"`/orange `0xFFFF9800` (status `PARTIALLY_RECEIVED`), `"Received"`/green `0xFF4CAF50`, `"Cancelled"`/red `0xFFF44336`, unknown → raw string/grey.

**CREATE & SYNC**
- [ ] T3. With PO-1 (DRAFT) and PO-2 (SENT): `"Draft"` chip shows PO-1 only; `"Sent"` shows PO-2 only; `"All"` shows both.

**READ & SYNC**
- [ ] T4. Tap PO-1 card → `po_detail/{poId}` → header shows poNumber as title, `"Vendor"` label + name, `POStatusBadge` (`"DRAFT"`), `"Created"`/`"Expected"`/`"Currency"`/`"Items"`/notes/`"Total Amount"` `"$325"`.
- [ ] T5. Detail sections: `"Order Items (2)"` with `POItemCard`: name + `"$100"`, `"Ordered: 2 PC"`, `"Recv'd: 0 PC"` (progress grey), `"SKU: QA-SKU-001"`. `"Linked Receipts (0)"` empty.

**UPDATE & SYNC**
- [ ] T6. PO-1 detail (DRAFT): bottom bar shows `"Mark as SENT"` (Send icon, blue) → tap → status badge `"SENT"`. Sheet `Purchase_Orders` E=`"SENT"`, K sync pending→synced.

**DELETE & SYNC**
- [ ] T7. PO-2 detail (SENT): bottom bar shows `"Receive Goods"` (Inventory icon, green `0xFF4CAF50`) → navigates to Create GRN. No cancel/delete buttons anywhere (see F4.2 T14).

**Edge cases**
- [ ] E1. `PARTIALLY_RECEIVED` PO appears ONLY under `"All"` (not under Sent/Received chips). Record after F4.5 creates one.
- [ ] E2. Empty state `"No Purchase Orders"` + `"Tap + to create your first PO"` on a fresh sheet.
- [ ] E3. Status text rendering for unexpected status `"ARCHIVED"` → record raw display.

---

## F4.4 PO DETAIL — SHARE + STATUS FLOW (PODetailScreen/ViewModel)

**Field/Variable Level Test**
- [ ] T1. Share IconButton (top bar, only if vendorName non-empty) → WhatsApp text literal: `"*Purchase Order: <poNumber>*"`, `"Date: <dd MMM yyyy>"`, `"Dear <vendorName>,"`, `"Please process the following order:"`, items `"<i>. <productName> — Qty: <qty> <unit> @ $<unitCostPrice>"`, `"*Total: $<totalAmount>*"`, `"Notes: <notes>"` → `https://wa.me/?text=`.
- [ ] T2. Status logic: `canSend = status == "DRAFT"`; `canReceive = status in ["SENT","PARTIALLY_RECEIVED"]`; else static box `"PO <status>"` green.

**CREATE & SYNC**
- [ ] T3. Mark PO-2... wait — PO-2 already SENT. Use a 3rd PO: `HERMES-PO-003` DRAFT with PROD-002 ×1 @150. Then `"Mark as SENT"` → Sheet E=`"SENT"`.
- [ ] T4. `updateStatus` accepts ANY string (no whitelist in `UpdatePOStatusUseCase`) — verify via log/behavior record.

**READ & SYNC**
- [ ] T5. After GRN confirm (F4.5), revisit: `"Linked Receipts (1)"` shows `LinkedGrnCard`: CheckCircle, grnNumber, `"$<amount> · <date>"`, status green if `"CONFIRMED"` else orange, ChevronRight → `grn_detail/{grnId}`.

**UPDATE & SYNC**
- [ ] T6. Progress bar: `Recv'd 0/2` grey → `1/2` orange (`>0`) → `2/2` green (`>=1.0`).

**DELETE & SYNC**
- [ ] T7. No edit fields on detail (read-only display) — confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. Share when vendor phone empty → `wa.me/?text=` without phone. Record.
- [ ] E2. `"Mark as SENT"` double-tap → record duplicate status updates / rows.

---

## F4.5 CREATE GRN (CreateGrnScreen) — full receive cycle

**Navigation:** PO detail (SENT) → `"Receive Goods"` → route `create_grn/{poId}`.

**Field/Variable Level Test**
- [ ] T1. Header shows `"PO: <poNumber>"`, `"Vendor: <vendorName>"`. Notes field `"Delivery Notes"`. Attachment: `"Attach Document"` (GetContent `*/*`) → becomes filename or `"File selected"` (green). Top-bar Check icon (desc `"Confirm"`, enabled `!isLoading`).
- [ ] T2. Item accordion `GrnItemAccordion` collapsed: `"Recv: <qty> <unit> | Cost: $<cost>"`; expanded fields: `"Received Qty"` (Number kb).
- [ ] T3. `inventoryAction` RADIO group (not dropdown) — options EXACT: `"PENDING"`→`"Review Later"`, `"NEW_PRODUCT"`→`"Create New Product"`, `"ADD_BATCH"`→`"Add as New Batch"`, `"UPDATE_BATCH"`→`"Add to Existing Batch"`. `isNewProduct = (action == "NEW_PRODUCT")`.
- [ ] T4. When `NEW_PRODUCT`: fields `"Category"`, `"Brand"`, `"Selling Price"` (shown only if > 0). When `NEW_PRODUCT`/`ADD_BATCH`: `"Batch Information"` section with `"Batch / Lot Number"`, `"Mfg Date"`, `"Exp Date"` (free text, NO pickers). When `UPDATE_BATCH`: `"Select Existing Batch"` (placeholder `"Select a batch..."`, items `"<batchNumber> — Stock: <stockQty> | Exp: <expiryDate>"`, empty message `"No active batches found for this product. Select 'Add as New Batch' instead."`).

**CREATE & SYNC**
- [ ] T5. For HERMES-PO-003 (PROD-002 ×1 @150): set Received Qty `1`, action `UPDATE_BATCH` → select existing batch HERMES-BATCH-002. `"Confirm"`.
- [ ] T6. GRN number auto-gen: `"GRN-<yyyy>-<NNNN>"` (e.g. `GRN-2026-0001`; full year). Record it. Navigation → `grn_success/{grnId}/{newProducts}/{batches}/{updatedBatches}`.
- [ ] T7. Force Sync → Sheet `GRN_Headers`: A `grn_id` UUID, B grn_number, C po_id, D vendor_id, E vendor_name, F status=`"CONFIRMED"`, G notes, H `received_by`=`"admin_user_id"` (**hardcoded — XF**), I `total_amount`=150, J `"synced"`, K pos_terminal_id=`"terminal_1"`, L attached_file_id, M attached_file_url, N/O timestamps.
- [ ] T8. Sheet `GRN_Items`: A UUID, B grn_id, C po_item_id, D product_id, E name, F barcode, G sku, H `received_qty`=1, I unit cost 150, J total 150, K unit, L batch_number, M mfg, N exp, O `inventory_action`=`"UPDATE_BATCH"`, P `is_new_item`=false, Q `"synced"`, R/S timestamps.
- [ ] T9. Sheet `Product_Batches` for HERMES-BATCH-002: G `stock_qty` incremented by 1.
- [ ] T10. Sheet `Purchase_Orders` for HERMES-PO-003: E status=`"RECEIVED"` (all received) and `PO_Items` H `received_qty`=1.

**READ & SYNC**
- [ ] T11. GRN Success screen text: `"Receipt Successful"`, `"Goods Received Successfully"`, `"Inventory Sync Summary"`, `"New Products Created:"` (0), `"New Batches Added:"` (0), `"Existing Batches Updated:"` (1). Button `"Return to PO List"`.
- [ ] T12. GRN detail (`grn_detail/{grnId}`): rows `"Vendor"`, `"Phone"` (blank), `"PO Reference"`, `"Received On"` (`dd MMM yyyy, hh:mm a`), `"Received By"` (`admin`), `"Notes"`, `"Total Items: 1"`, `"$150"`; item badge `"Updated Batch"`; `"Received: 1 KG"`, `"Batch: ..."`, `"Exp: ..."`, `"SKU: ..."`.

**UPDATE & SYNC**
- [ ] T13. Partial receipt: PO with PROD-001 ×2 → GRN received qty 1, action PENDING → Sheet PO status = `"PARTIALLY_RECEIVED"`; GRN item O=`"PENDING"`. PO list chip `"Partial Receipt"` (orange) — but visible ONLY under `"All"` (F4.3 E1).

**DELETE & SYNC**
- [ ] T14. GRN has NO edit/delete/void. Confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. Received Qty `0` → record.
- [ ] E2. Received Qty > ordered (e.g. 5 on ordered 1) → record (no clamp found).
- [ ] E3. Received Qty `abc` → record.
- [ ] E4. `NEW_PRODUCT` action: fill Category `Uncategorized` (fallback), Brand, Selling Price → after Confirm, verify NEW product in `Inventory` (category fallback `"Uncategorized"`, barcode fallback = productId, `hasBatches`=true) + first batch created + count `"New Products Created: 1"`.
- [ ] E5. `ADD_BATCH` action with blank batch number → record auto-batch behavior in ConfirmGrnUseCase.
- [ ] E6. Attachment: attach a PDF → verify upload to Drive folder `"Tillzo POS Uploads"`; Sheet M (`attached_file_url`) populated. Attach a corrupt file → record silent-catch behavior.
- [ ] E7. `"Confirm"` with network OFF → record local-first behavior (attachment upload silently skipped, empty bytes skipped).
- [ ] E8. Confirm double-tap → record duplicate GRN rows.
- [ ] E9. GRN for a PO with `received_qty` already = ordered → record (should not exceed).
- [ ] E10. `inventory_action` = `PENDING` on ALL items → PO status becomes `"SENT"` (not PARTIALLY) — record.

---

## F4.6 GRN LIST (GrnListScreen)

**Field/Variable Level Test**
- [ ] T1. Title `"Receiving History"`. NO search, NO filters (verified absent). Card: grnNumber, raw status label (green `0xFF4CAF50` if `"CONFIRMED"`, orange `0xFFFF9800` otherwise), vendorName, `"PO: <poNumber>"`, `"$<totalAmount>"`, date `dd MMM yyyy`. Empty: `"No receipts yet"` + `"Create a receipt from a Purchase Order"` + `"Back"` button.

**CREATE & SYNC**
- [ ] T2. With GRN-2026-0001 created: verify card + status green.

**READ & SYNC**
- [ ] T3. Tap card → `grn_detail/{grnId}`.

**UPDATE & SYNC**
- [ ] T4. No status actions on detail (read-only) — confirm absence → `[PASS]`.

**DELETE & SYNC**
- [ ] T5. No delete — confirm absence → `[PASS]`.

**Edge cases**
- [ ] E1. `"View Attached Document"` OutlinedButton appears only when `attachedFileUrl.isNotEmpty()` → opens browser Intent. Verify.

---

## F4.7 PO/GRN USE-CASE LAYER (verification-only)

- [ ] T1. `CreatePurchaseOrderUseCase` = `insertPO + insertPOItems`, NO validation, NO numbering (dead code — not called by any ViewModel; numbering lives in `CreatePurchaseOrderViewModel`). Verify no UI path uses it → `[PASS]`.
- [ ] T2. `GetPurchaseOrdersUseCase` = `poDao.getAllPOs()` (`WHERE isDeleted = 0 ORDER BY createdAt DESC`). Verify list ordering newest-first.
- [ ] T3. `SharePurchaseOrderUseCase.shareAsPdf`: PDF 612×792 letter, text `"PURCHASE ORDER"`, `"Phone: <shopPhone>"`, `"PO Number: ..."`, `"Date: ..."`, `"Expected Delivery: ..."`, `"VENDOR: ..."`, heads `"ITEM"/"QTY"/"PRICE"/"TOTAL"`, name truncated 20 chars, `"TOTAL AMOUNT: <currency> <total>"`, `"Notes: ..."`; file `cacheDir/pdfs/PO_<poNumber>.pdf` via FileProvider; share to WhatsApp package `com.whatsapp` type `application/pdf`; fallback chooser title `"Share PO"`. NOT wired to any screen — record absence in UI.
- [ ] T4. `UpdatePOStatusUseCase` — no status whitelist (accepts any string). Record.

---

## F4.8 KNOWN-BUG CHECKPOINTS (PO/GRN)

- [ ] XF1. PO `currency` is hardcoded `"$"` in `CreatePurchaseOrderViewModel` regardless of Settings currency symbol. Test: set Settings currency to `PKR` → create PO → Sheet `Purchase_Orders` H must still be `"$"`. Record actual.
- [ ] XF2. `CANCELLED` PO status exists in list filter but NO UI path sets it. Test: attempt to cancel a PO via all screens → record inability.
- [ ] XF3. GRN `received_by` hardcoded `"admin_user_id"` + `receivedByName` `"Admin"` — GRN detail shows `"Received By: admin"`. Record actual display.
- [ ] XF4. PO list filters use exact equality — `PARTIALLY_RECEIVED` POs invisible under `"Sent"`/`"Received"` chips. Verify and record.
- [ ] XF5. GRN item `lowStockThreshold=5.0` default in delta upsert — record if any UI ever reads it (expected: never).
