# 🧪 TillzoPOS — Complete Hierarchical Testing Plan

> **Version**: 1.0 | **Date**: 2026-08-02  
> **Target**: Autonomous AI Agent running on Android VM  
> **Database**: Google Sheets (synced via REST API)  
> **App Architecture**: MVVM + Clean Architecture, Room DB → Google Sheets REST API

---

## 📋 Pre-Test Setup & Environment Preparation

### SETUP-1: Google Account Login
- [ ] Launch TillzoPOS app
- [ ] Complete Google Sign-In (OAuth 2.0)
- [ ] Verify access token is stored (app should proceed past login screen)
- [ ] **LOG CHECK**: Verify no auth errors in Logcat

### SETUP-2: Sheet Provisioning
- [ ] If first launch → SheetPickerScreen appears
- [ ] Verify Google Sheet is created with correct name format: `{ShopName} — TillzoPOS`
- [ ] **SHEET CHECK**: Open Google Sheet on desktop → verify ALL 24 tabs exist:
  - `Sales_Aug_2026`, `Inventory`, `Customers`, `Khata_Events`, `Expenses`, `Categories`, `Returns`, `Wastage_Ledger`, `Users_Permissions`, `Purchase_Orders`, `PO_Items`, `GRN_Headers`, `GRN_Items`, `Vendors`, `Product_Batches`, `Product_Units`, `Till_Sessions`, `Stock_Adjustments`, `BarcodeGeneralConfigs`, `BarcodeFieldConfigs`, `Settings`, `Sync_Log`, `Dashboard`, `SYS_DB_DO_NOT_TOUCH`
- [ ] Verify each tab has correct column headers in Row 1
- [ ] Verify `SYS_DB_DO_NOT_TOUCH` tab is **hidden**
- [ ] **LOG CHECK**: Verify provisioning logs show success

### SETUP-3: Root Detection Check
- [ ] Verify RootBeer check runs on startup
- [ ] If VM is non-rooted → app proceeds normally
- [ ] If VM is rooted → `RootBlockedScreen` should appear (test this scenario if possible)
- [ ] **LOG CHECK**: Verify root detection log entry

### SETUP-4: Permissions
- [ ] Verify runtime permission requests on first launch:
  - CAMERA permission
  - BLUETOOTH_CONNECT permission
  - POST_NOTIFICATIONS permission
- [ ] Grant all permissions
- [ ] **LOG CHECK**: Verify permission grants logged

---

## 🏗️ PHASE 1: Foundation & Navigation Integrity (Priority: CRITICAL)

> Test all navigation routes exist and don't crash. This MUST pass before any functional testing.

### NAV-1: Primary Routes (AppNavHost)
| # | Action | Expected | Route |
|---|--------|----------|-------|
| 1 | App starts | HomeScreen loads | `home` |
| 2 | Open Advanced Menu | AdvancedMenuSheet appears | — |
| 3 | Tap "Inventory" | InventoryModule loads | `inventory_module` |
| 4 | Back | Returns to Home | — |
| 5 | Tap "CRM" from menu | CrmScreen loads | `store_module/crm_screen` |
| 6 | Tap "Returns" from menu | ReturnsScreen loads | `store_module/returns_screen` |
| 7 | Tap "History" from menu | HistoryScreen loads | `store_module/history_screen` |
| 8 | Tap "Z-Report" from menu | ZReportScreen loads | `store_module/zreport_screen` |
| 9 | Tap "Expenses" from menu | ExpenseScreen loads | `store_module/expense_screen` |
| 10 | Tap "Settings" from menu | SettingsModule loads | `settings_module` |
| 11 | Tap "Purchase Orders" | PurchaseOrderListScreen loads | `po_list` |
| 12 | Tap "GRN" from menu | GrnListScreen loads | `grn_list` |
| 13 | Tap "Vendors" from menu | VendorManagementScreen loads | `vendor_management` |
| 14 | Tap "Stock Adjustment" | StockAdjustmentScreen loads | `stock_adjustment` |
| 15 | Tap "Open/Close Till" | TillOpenScreen loads | `till_open` |
| 16 | Tap "Wastage Log" | WastageLogScreen loads | `wastage_log` |
| 17 | Tap "Stock Alerts" | StockAlertsScreen loads | `stock_alerts` |
| 18 | Tap "Hardware Diagnostics" | HardwareDiagnosticScreen loads | `hardware_diagnostics` |

- [ ] **LOG CHECK** after each navigation: Verify `NAVIGATION` log shows correct route
- [ ] **CRASH CHECK**: No crashes on any navigation

### NAV-2: Dynamic Routes with Parameters
| # | Action | Expected | Route |
|---|--------|----------|-------|
| 1 | Complete a sale → receipt | ReceiptScreen loads | `receipt/{invoiceId}` |
| 2 | Open PO detail | PODetailScreen loads | `po_detail/{poId}` |
| 3 | Create GRN from PO | CreateGrnScreen loads | `create_grn/{poId}` |
| 4 | GRN confirmed | GrnSuccessScreen loads | `grn_success/{grnId}/{...}` |
| 5 | Navigate to GRN detail | GrnDetailScreen loads | `grn_detail/{grnId}` |
| 6 | Navigate to QR generator | QrGeneratorScreen loads | `qr/{barcodeId}` |
| 7 | Navigate to category mgmt | CategoryManagementScreen loads | `category_management` |
| 8 | Navigate to product units | ProductUnitsScreen loads | `product_units` |

### NAV-3: Store Module Internal Routes
| # | Action | Expected |
|---|--------|----------|
| 1 | In CRM → tap "WhatsApp Statement" | StatementScreen loads via `statement_screen/{customerId}` |
| 2 | Verify `statement/{customerId}` route also works | StatementScreen loads |

> ⚠️ **KNOWN POTENTIAL ISSUE**: Statement route was previously missing — verify BOTH route variants work

---

## 🛒 PHASE 2: Product & Inventory Management (Module 1 + 2)

### INV-1: Category Management
1. Navigate to Category Management screen
2. **ADD** a new category (e.g., "Fruits")
3. Verify it appears in the list immediately
4. **ADD** 2 more categories (e.g., "Dairy", "Bakery")
5. **LOG CHECK**: Verify Room insert logged
6. **SYNC** (Force Sync from menu)
7. **SHEET CHECK**: Open `Categories` tab → verify all 3 categories appear with correct columns
8. **DELETE** one category (soft delete)
9. Force Sync again
10. **SHEET CHECK**: Verify deleted category is removed from sheet

### INV-2: Product Units Management
1. Navigate to Product Units screen
2. **ADD** custom units: "KG", "ML", "PC", "GM", "Dozen"
3. Verify they appear in list
4. **SYNC** → Force Sync
5. **SHEET CHECK**: Open `Product_Units` tab → verify all units synced
6. **DELETE** one unit
7. Verify deletion works in UI
8. ⚠️ **KNOWN ISSUE CHECK**: Is units dropdown wired in InventoryFormDialog? (Was marked incomplete)

### INV-3: Add Product (Full Field Test)
1. Navigate to Inventory → Add Product
2. Fill ALL 17+ fields:
   - `item_name`: "Test Chicken Breast"
   - `sku`: "SKU-001"
   - `barcode_id`: "8901234567890"
   - `category`: Select from dropdown (should show categories from INV-1)
   - `description`: "Fresh chicken breast 1KG pack"
   - `cost_price`: 250
   - `selling_price`: 350
   - `tax_percent`: 5
   - `current_stock`: 50
   - `low_stock_threshold`: 10
   - `unit`: "KG" (check if dropdown or text field)
   - `batch_number`: "BATCH-001"
   - `manufacturing_date`: Use DatePicker (set to today)
   - `expiry_date`: Use DatePicker (set to 30 days from now)
   - `expiry_alert_days`: 7
   - `damaged_stock`: 0
3. Save product
4. **LOG CHECK**: Verify Room insert, syncStatus="pending"
5. Verify product appears in inventory list with correct badges
6. **SYNC** → Force Sync
7. **SHEET CHECK**: Open `Inventory` tab → verify product with ALL fields correct
8. **RECORD**: Note the `system_row_id` for later verification

### INV-4: Add Multiple Products (Variety Test)
Add 5 more products with different characteristics:
| # | Name | Type | Stock | Threshold | Notes |
|---|------|------|-------|-----------|-------|
| 1 | "Mutton Leg" | KG, weight item | 20 | 5 | Near-expiry (set expiry 5 days out) |
| 2 | "Milk 1L Pack" | PC, unit item | 100 | 20 | Normal |
| 3 | "Wheat Flour 5KG" | PC | 0 | 10 | OUT OF STOCK |
| 4 | "Cooking Oil 1L" | ML | 8 | 15 | LOW STOCK (below threshold) |
| 5 | "Expired Yogurt" | PC | 30 | 5 | Expired (set expiry yesterday) |

After adding all:
- [ ] Verify filter chips work: ALL, LOW_STOCK, OUT_OF_STOCK, NEAR_EXPIRY, DAMAGED
- [ ] Verify colored badges appear correctly on each card
- [ ] Force Sync → **SHEET CHECK**: All 6 products in `Inventory` tab
- [ ] **LOG CHECK**: Verify all inserts logged

### INV-5: Edit Product
1. Tap on "Test Chicken Breast" → Edit
2. Change `selling_price` from 350 → 400
3. Change `current_stock` from 50 → 45
4. Save
5. Verify changes reflected in list immediately
6. Force Sync → **SHEET CHECK**: Verify updated values in Inventory tab
7. **LOG CHECK**: Verify update logged with syncStatus changed to "pending"

### INV-6: Delete Product (Soft Delete)
1. Delete "Expired Yogurt" from list (soft delete)
2. Verify it disappears from UI list
3. Force Sync
4. **SHEET CHECK**: Verify product removed from Inventory sheet (via deleteDimension)
5. **LOG CHECK**: Verify soft delete and sync deletion logged

### INV-7: Product Search
1. From inventory list, use search bar
2. Search by name: "chicken" → should find "Test Chicken Breast"
3. Search by SKU: "SKU-001" → should find same product
4. Search by barcode: "8901234567890" → should find same product
5. Search with no results: "xyznotfound" → empty state
6. **LOG CHECK**: Verify search queries logged

### INV-8: Stock Adjustment
1. Navigate to Stock Adjustment screen
2. Search for "Milk 1L Pack"
3. Set adjustment type: "ADD"
4. Quantity: +20
5. Reason: "New delivery received"
6. Save
7. Verify stock updated (100 → 120)
8. Force Sync → **SHEET CHECK**: `Stock_Adjustments` tab should have entry
9. Do another adjustment: SUBTRACT 5 from "Cooking Oil 1L"
10. Verify stock updated
11. **LOG CHECK**: Verify all adjustments logged

### INV-9: Stock Alerts Screen
1. Navigate to Stock Alerts (from Advanced Menu)
2. Verify 3 tabs exist: Low Stock / Out of Stock / Expiring Soon
3. **Low Stock tab**: Should show "Cooking Oil 1L" (stock < threshold)
4. **Out of Stock tab**: Should show "Wheat Flour 5KG" (stock = 0)
5. **Expiring Soon tab**: Should show "Mutton Leg" (expiry within 30 days)
6. Verify color-coded badges and day countdown
7. **LOG CHECK**: Verify alert queries logged

### INV-10: QR Code Generation
1. From inventory, tap on a product → "Print QR" button
2. Verify navigation to QR Generator screen (route `qr/{barcodeId}`)
3. Verify QR code image is rendered
4. Verify barcode ID label is displayed
5. **LOG CHECK**: Verify QR generation logged
6. ⚠️ **KNOWN ISSUE CHECK**: Test TSPL print button (expected: may be incomplete)

### INV-11: Wastage Log
1. Navigate to Wastage Log (from Advanced Menu)
2. Log a wastage entry:
   - Search product: "Mutton Leg"
   - Quantity: 2
   - Reason: "Expired"
3. Verify wastage entry appears in list
4. Verify stock is deducted (20 → 18)
5. Force Sync → **SHEET CHECK**: `Wastage_Ledger` tab should have entry
6. **LOG CHECK**: Verify wastage logging and stock deduction

---

## 💰 PHASE 3: POS Selling & Payment (Module 3)

### POS-1: Quick Grid / Pinned Items
1. Go to HomeScreen (POS)
2. Check if Quick Grid shows any items
3. ⚠️ **CHECK**: Are items pinned? If not, go to Inventory → set isPinned=true for 2-3 items
4. Return to Home → verify pinned items appear in quick grid
5. **LOG CHECK**: Verify pinned items query logged

### POS-2: Product Search from POS
1. In HomeScreen search bar, type "chicken"
2. Verify search results appear (live debounced search)
3. Tap on "Test Chicken Breast" → verify it adds to cart
4. **LOG CHECK**: Verify search and cart operations logged

### POS-3: Cart Operations
1. Add "Test Chicken Breast" to cart (qty=1)
2. Add "Milk 1L Pack" to cart (qty=3)
3. Verify cart shows both items with correct prices
4. **Change quantity**: Update chicken to qty=2
5. Verify subtotal recalculates
6. **Remove item**: Remove milk from cart
7. Verify cart updates correctly
8. **Clear cart**: Clear all items
9. Verify cart is empty
10. **LOG CHECK**: Verify all cart operations logged

### POS-4: Decimal Weight Input
1. Add "Mutton Leg" (KG unit) to cart
2. Enter decimal quantity: 1.5 KG
3. Verify price calculates correctly: 1.5 × price_per_kg
4. Verify receipt shows "1.500 KG" format
5. **LOG CHECK**: Verify decimal handling

### POS-5: Simple Cash Sale (End-to-End)
1. Add 2 items to cart:
   - "Test Chicken Breast" × 2 = Rs 800
   - "Milk 1L Pack" × 3 = (price × 3)
2. Tap Pay/Complete button → PaymentDialog opens
3. Select "Cash" payment
4. Enter cash amount (more than total for change)
5. Complete sale
6. **Verify**: Receipt screen opens with:
   - Shop header
   - Invoice number
   - Date & time
   - Cashier name
   - Line items with correct prices
   - Subtotal, tax, discount, total
   - Payment method: CASH
   - Change amount (cash - total)
   - QR code
7. **LOG CHECK**: Verify sale completion logged
8. Force Sync → **SHEET CHECK**: `Sales_Aug_2026` tab should have the sale row
9. Verify ALL sale columns are populated correctly in sheet
10. **RECORD**: Note invoice ID for later tests

### POS-6: Card Payment Sale
1. Add items to cart
2. Select "Card" payment method
3. Enter full amount as card
4. Complete sale
5. Verify receipt shows "CARD" as payment method
6. Force Sync → **SHEET CHECK**: Verify card_amount column populated

### POS-7: Wallet Payment (JazzCash/EasyPaisa)
1. Add items to cart
2. Select "Wallet" payment method
3. Complete sale
4. Verify receipt shows "WALLET"
5. Force Sync → **SHEET CHECK**: Verify wallet_amount column

### POS-8: Split Payment
1. Add items to cart (total = e.g., Rs 500)
2. In PaymentDialog:
   - Enter Cash: Rs 300
   - Enter Card: Rs 200
3. Complete sale
4. Verify receipt shows "SPLIT" with breakdown
5. **SHEET CHECK**: Verify cash_amount=300, card_amount=200, payment_method="SPLIT"
6. **LOG CHECK**: Verify split payment logged

### POS-9: Udhaar (Credit) Sale
1. Add items to cart
2. Select "Udhaar" payment
3. **Customer search** should appear
4. Search for existing customer OR create new customer inline:
   - Name: "Ali Khan"
   - Phone: "03001234567"
5. Complete Udhaar sale
6. Verify receipt shows "UDHAAR" with customer name
7. Force Sync → **SHEET CHECK**:
   - `Sales_Aug_2026`: udhaar_amount populated, customer_id linked
   - `Khata_Events`: New event with type="UDHAAR", correct amount
   - `Customers`: Ali Khan entry (if new customer)
8. **LOG CHECK**: Verify KhataEvent creation logged

### POS-10: WhatsApp Receipt Share
1. After any sale, on Receipt screen
2. Tap "Share on WhatsApp" button
3. Verify phone number prompt appears (or uses customer's number)
4. Verify WhatsApp Intent opens with formatted receipt text
5. **LOG CHECK**: Verify WhatsApp share action logged

### POS-11: Receipt QR Code
1. On Receipt screen, verify QR code is displayed
2. QR should encode the invoiceId
3. **LOG CHECK**: Verify QR generation

### POS-12: Print Receipt Button
1. Tap "Print Receipt" button
2. ⚠️ **KNOWN ISSUE**: This is a TODO/placeholder. Verify it shows snackbar/toast
3. **HIGHLIGHT** if button does nothing or crashes

### POS-13: Blind Selling Verification
1. Set a product stock to 0 in inventory
2. Go to POS → search for that product
3. Verify it CAN still be added to cart and sold (Blind Selling rule)
4. Complete the sale successfully
5. **LOG CHECK**: Confirm no stock check error logged during sale

---

## 🏪 PHASE 4: Vendor & Purchase Order Management (Module 4)

### VEN-1: Vendor Management
1. Navigate to Vendor Management
2. **ADD** vendor:
   - Name: "ABC Supplier"
   - Phone: "03211234567"
   - WhatsApp: "03211234567"
   - Email: "abc@supplier.com"
   - City: "Karachi"
3. Verify vendor appears in list
4. **ADD** 2 more vendors
5. **SEARCH**: Search by name → verify results
6. Force Sync → **SHEET CHECK**: `Vendors` tab should have all vendors
7. **LOG CHECK**: Verify vendor operations logged

### PO-1: Create Purchase Order
1. Navigate to Purchase Orders → Create PO
2. Select vendor: "ABC Supplier"
3. Add items from inventory:
   - "Test Chicken Breast" × 20 @ Rs 250 (cost price)
   - "Milk 1L Pack" × 50 @ (cost price)
4. Add a manual item not in inventory:
   - "New Product XYZ" × 10 @ Rs 100
5. Set expected delivery date
6. Add notes: "Urgent delivery needed"
7. Verify running total calculated
8. Save PO
9. **LOG CHECK**: Verify PO creation logged
10. Force Sync → **SHEET CHECK**: `Purchase_Orders` tab + `PO_Items` tab both populated

### PO-2: PO List Screen
1. Navigate to PO List
2. Verify created PO appears
3. Verify status shows "DRAFT" or initial status
4. ⚠️ **KNOWN ISSUE CHECK**: Status filter chips may be missing
5. **LOG CHECK**: Verify PO list query logged

### PO-3: PO Detail Screen
1. Tap on the PO from list
2. Verify PODetailScreen loads with:
   - Vendor name
   - All items with quantities and prices
   - Total amount
   - Status
   - "Create GRN" button
3. ⚠️ **CHECK**: Does GRN list section show within PO detail? (Was marked incomplete)
4. **LOG CHECK**: Verify PO detail loaded

### PO-4: PO WhatsApp Share
1. From PO Detail, tap Share button
2. Verify formatted PO text generated
3. Verify WhatsApp Intent opens
4. **LOG CHECK**: Verify share action

---

## 📦 PHASE 5: GRN (Goods Received Note) (Module 5)

### GRN-1: Create GRN from PO
1. From PO Detail → tap "Create GRN"
2. Verify CreateGrnScreen loads with PO items pre-loaded
3. For each item, fill:
   - Received qty (can be less than ordered)
   - Batch number
   - Manufacturing date
   - Expiry date
   - Inventory action (NEW_ITEM / ADD_BATCH / UPDATE_BATCH)
4. Set one item as "NEW_ITEM" (the manual item from PO)
5. Set one item as "ADD_BATCH" (existing product, new batch)
6. Set one item as "UPDATE_BATCH" (existing product, existing batch)
7. **LOG CHECK**: Verify all field entries logged

### GRN-2: Confirm GRN (All 3 Inventory Actions)
1. Confirm the GRN
2. Verify GrnSuccessScreen shows counts:
   - New products created
   - Batches added
   - Batches updated
3. **LOG CHECK**: Verify ConfirmGrnUseCase execution logged

### GRN-3: Verify Inventory Impact
1. Go to Inventory list
2. **NEW_ITEM**: Verify new product "New Product XYZ" now exists in inventory
3. **ADD_BATCH**: Verify existing product has a new batch (check via batch list if accessible)
4. **UPDATE_BATCH**: Verify batch stock incremented
5. Verify totalStock recalculated correctly for all affected products
6. **LOG CHECK**: Verify stock updates logged

### GRN-4: Verify PO Status Auto-Update
1. Go back to PO List → open the PO
2. If all items fully received → status should be "RECEIVED"
3. If partially received → status should be "PARTIALLY_RECEIVED"
4. **LOG CHECK**: Verify PO status update logged

### GRN-5: GRN Sync Verification
1. Force Sync
2. **SHEET CHECK**: 
   - `GRN_Headers` tab: Header row with all fields
   - `GRN_Items` tab: All item rows with correct data
   - `Product_Batches` tab: New/updated batches
   - `Inventory` tab: Updated stock for affected products
   - `Purchase_Orders` tab: Updated PO status
3. **Cross-verify**: GRN items match PO items in quantity and product

### GRN-6: GRN List & Detail
1. Navigate to GRN List
2. Verify the created GRN appears
3. Tap on it → GrnDetailScreen loads
4. Verify read-only view shows all header + item data correctly
5. **LOG CHECK**: Verify GRN list and detail queries logged

---

## 👥 PHASE 6: CRM & Khata Ledger (Module 7)

### CRM-1: Customer List
1. Navigate to CRM screen
2. Verify customer list on left panel
3. If "Ali Khan" exists from POS-9 Udhaar sale, verify it appears
4. Search by name → verify results
5. **LOG CHECK**: Verify customer queries logged

### CRM-2: Add Customer
1. Add new customer:
   - Name: "Bilal Ahmed"
   - Phone: "03451234567"
2. Verify customer appears in list
3. ⚠️ **KNOWN ISSUE CHECK**: WhatsApp/Email/Address fields may be missing from dialog
4. Force Sync → **SHEET CHECK**: `Customers` tab should have new customer
5. **LOG CHECK**: Verify customer insert logged

### CRM-3: Khata - UDHAAR Event (Manual)
1. Select customer "Ali Khan" from list
2. In right panel, add UDHAAR event:
   - Amount: Rs 500
   - Type: UDHAAR (credit given)
3. Verify balance updates:
   - Total Udhaar increases by 500
   - Baqaya (outstanding) increases
4. Force Sync → **SHEET CHECK**: `Khata_Events` tab should have UDHAAR event
5. **LOG CHECK**: Verify Khata event logged

### CRM-4: Khata - JAMA Event (Payment Received)
1. Same customer "Ali Khan"
2. Add JAMA event:
   - Amount: Rs 300
   - Type: JAMA (payment received)
3. Verify balance updates:
   - Total Jama increases by 300
   - Baqaya decreases accordingly
4. Force Sync → **SHEET CHECK**: `Khata_Events` tab should have JAMA event
5. Verify Append-Only: BOTH events exist, no row was updated/deleted
6. **LOG CHECK**: Verify JAMA event logged

### CRM-5: Khata Balance Verification
1. Verify displayed balance:
   - Total Udhaar = (POS-9 sale amount + 500)
   - Total Jama = 300
   - Baqaya = Total Udhaar - Total Jama
2. **SHEET CHECK**: Manually SUM Khata_Events for this customer in sheet → compare with app
3. **LOG CHECK**: Verify balance calculation logged

### CRM-6: WhatsApp Statement
1. Select customer → tap "WhatsApp Statement"
2. Verify StatementScreen loads (test both `statement_screen/{id}` and `statement/{id}` routes)
3. Verify statement shows all events with running balance
4. Tap share → WhatsApp Intent opens with formatted text
5. **LOG CHECK**: Verify statement generation logged

### CRM-7: Khata Event History List
1. In CRM right panel, verify event history list (LazyColumn)
2. ⚠️ **KNOWN ISSUE CHECK**: Was marked as possibly missing — verify if events list renders
3. Each event should show: date, type, amount, running balance
4. **LOG CHECK**: Verify event history query

---

## 🔄 PHASE 7: Sync & Cloud Verification (Module 8) — CRITICAL

### SYNC-1: Auto Sync (15-min Periodic)
1. Create data (sale, product, customer)
2. Wait for automatic sync (15-min interval) OR check WorkManager status
3. **SHEET CHECK**: Verify data appeared without manual force sync
4. **LOG CHECK**: Verify SyncWorker periodic execution logged

### SYNC-2: Force Sync
1. Create a new sale
2. Tap "Force Sync" from Advanced Menu
3. Verify sync dialog/progress appears
4. Verify Toast: success/failure message
5. **SHEET CHECK**: Verify sale appears in sheet immediately
6. **LOG CHECK**: Verify OneTimeWorkRequest logged

### SYNC-3: Full Sync Verification (All Tables)
After performing operations in all modules, Force Sync and verify EVERY tab:

| # | Sheet Tab | Expected Data |
|---|-----------|---------------|
| 1 | `Sales_Aug_2026` | All sales with correct amounts, items_json, payment methods |
| 2 | `Inventory` | All products with current stock, prices, all fields |
| 3 | `Customers` | All customers with names, phones |
| 4 | `Khata_Events` | All UDHAAR/JAMA events, append-only (no updates) |
| 5 | `Expenses` | All expense entries |
| 6 | `Categories` | All categories |
| 7 | `Purchase_Orders` | All POs with status |
| 8 | `PO_Items` | All PO line items |
| 9 | `GRN_Headers` | All GRN headers |
| 10 | `GRN_Items` | All GRN line items |
| 11 | `Vendors` | All vendors |
| 12 | `Product_Batches` | All batches with stock, expiry |
| 13 | `Product_Units` | All custom units |
| 14 | `Till_Sessions` | All till sessions |
| 15 | `Stock_Adjustments` | All stock adjustments |
| 16 | `Wastage_Ledger` | All wastage entries |
| 17 | `Users_Permissions` | User data |
| 18 | `Settings` | last_updated_timestamp updated |

For EACH tab, verify:
- [ ] Column headers match expected schema
- [ ] Row count matches local data count
- [ ] Data values are correct (spot check 2-3 rows per tab)
- [ ] `system_row_id` / primary IDs are unique UUIDs
- [ ] `sync_status` is "synced" for all uploaded rows
- [ ] `pos_terminal_id` is populated

### SYNC-4: Sync Status Tracking
1. Create a product while OFFLINE (airplane mode if possible)
2. Verify syncStatus = "pending" in Room
3. Go back online
4. Force Sync
5. Verify syncStatus changes to "synced"
6. **SHEET CHECK**: Verify data appears in sheet only after sync
7. **LOG CHECK**: Verify sync status transitions logged

### SYNC-5: Delta Sync (60-second Polling)
1. Manually add a row to `Inventory` tab in Google Sheet (simulate another terminal)
2. Wait ~60 seconds for delta sync
3. Verify new product appears in app's inventory list
4. **LOG CHECK**: Verify DeltaSyncManager polling logs

### SYNC-6: Monthly Sharding
1. Verify current sales tab name is `Sales_Aug_2026` format
2. Check if `ShardingWorker` is scheduled
3. **LOG CHECK**: Verify sharding worker logs

### SYNC-7: Schema Guard
1. Force Sync
2. **LOG CHECK**: Verify "Schema checked and verified" message
3. **SHEET CHECK**: Verify `SYS_DB_DO_NOT_TOUCH` tab is hidden

### SYNC-8: UUID Deduplication
1. Force Sync twice in quick succession
2. **SHEET CHECK**: Verify NO duplicate rows in any tab
3. UUID check should prevent double-writes
4. **LOG CHECK**: Verify dedup logic logged

### SYNC-9: Soft Delete Sync
1. Delete a product from inventory (soft delete)
2. Force Sync
3. **SHEET CHECK**: Verify row is physically removed from `Inventory` tab via deleteDimension
4. **LOG CHECK**: Verify delete sync logged

### SYNC-10: Sync Error Handling
1. If possible, simulate network error during sync
2. Verify SyncWorker retries with exponential backoff (5s → 15s → 1m → 5m)
3. Verify data is NOT marked as "synced" until HTTP 200
4. **LOG CHECK**: Verify retry logic and error messages

---

## 🏦 PHASE 8: Till / Shift Management (Module 12)

### TILL-1: Open Till
1. Navigate to Till Open screen
2. Enter opening cash: Rs 5000
3. Open till
4. Verify app returns to Home
5. **LOG CHECK**: Verify till session created

### TILL-2: Sales During Till Session
1. Complete 3-4 sales (mix of cash, card, wallet, udhaar)
2. Each sale should be linked to current till session
3. **LOG CHECK**: Verify `addSaleToSession()` called after each sale

### TILL-3: Z-Report Day Close
1. Navigate to Z-Report screen
2. Verify till card shows:
   - Opening cash
   - Total cash sales
   - Total card sales
   - Total wallet sales
   - Total udhaar sales
   - Total sales count
   - Expected cash in drawer
3. Enter physical cash count
4. Verify variance calculated (physical - expected)
5. **LOG CHECK**: Verify Z-report data queries

### TILL-4: Close Till / Day Close
1. If pending sync > 0 → Close button should be DISABLED
2. Force Sync to clear pending queue
3. If pending = 0 → Close button should be ENABLED
4. Close the till
5. Verify session status changes to "closed"
6. Force Sync → **SHEET CHECK**: `Till_Sessions` tab should have closed session
7. **LOG CHECK**: Verify till close logged

---

## ↩️ PHASE 9: Returns & Refunds (Module 7.3)

### RET-1: Return by Invoice Lookup
1. Navigate to Returns screen
2. Enter invoice ID from POS-5 (or search)
3. Verify original sale details load:
   - Items, quantities, amounts
   - Payment method
4. **LOG CHECK**: Verify invoice lookup logged

### RET-2: Full Return
1. Select "Full Return"
2. Select reason
3. Confirm return
4. Verify negative SaleEntity created in Room
5. Force Sync → **SHEET CHECK**: Negative sale row in `Sales_Aug_2026`
6. **LOG CHECK**: Verify return processing logged

### RET-3: Stock Increment on Return
1. ⚠️ **KNOWN ISSUE CHECK**: When "Return to Inventory" is chosen:
   - Does stock increment in InventoryEntity?
   - Verify stock before and after return
2. If damaged return → verify WastageEntity created, stock NOT incremented
3. **LOG CHECK**: Verify stock operations on return

### RET-4: Partial Return
1. ⚠️ **KNOWN ISSUE**: Partial item-level refund was marked as NOT implemented
2. Try to do partial return → note behavior
3. **HIGHLIGHT** if only full invoice return works

---

## 💸 PHASE 10: Expense Tracking (Module 7.6)

### EXP-1: Add Expense
1. Navigate to Expense screen
2. Add expense:
   - Category: "Rent"
   - Amount: Rs 15000
   - Description: "Monthly shop rent"
3. Verify expense appears in list
4. Add 2 more expenses (electricity, wages)
5. Force Sync → **SHEET CHECK**: `Expenses` tab should have all entries
6. **LOG CHECK**: Verify expense operations logged

### EXP-2: Expense in Z-Report
1. Go to Z-Report
2. Verify expenses are included in day's summary
3. Verify Net = Revenue - Expenses

---

## 📊 PHASE 11: Transaction History (Module 7.4)

### HIST-1: Transaction List
1. Navigate to History screen
2. Verify all sales from today appear (searchable list)
3. Search by invoice ID → verify correct sale found
4. **LOG CHECK**: Verify history query logged

### HIST-2: Reprint Receipt
1. Tap on a sale from history
2. Tap "Print Duplicate" / reprint button
3. Verify receipt shows "DUPLICATE" or "REPRINT" header
4. **LOG CHECK**: Verify reprint logged

### HIST-3: Date Range Filter
1. ⚠️ **KNOWN ISSUE CHECK**: Date range filter was marked incomplete
2. Try to filter by date → note if feature exists
3. **HIGHLIGHT** if missing

---

## 📡 PHASE 12: Hardware Integration (Module 5)

### HW-1: Camera Scanner (InlineCameraBox)
1. Go to HomeScreen
2. Toggle camera scanner (if toggle exists)
3. Verify CameraX preview appears at 480p
4. If barcode available: scan it → verify auto-add to cart
5. Verify debounce (1500ms) prevents double scan
6. **LOG CHECK**: Verify scan detection logged

### HW-2: Camera Auto-Sleep
1. Open camera scanner
2. Wait 4 minutes without scanning
3. Verify camera auto-sleeps (deactivates)
4. **LOG CHECK**: Verify SLEEP_TIMEOUT_MS triggered

### HW-3: HID Scanner
1. If HID scanner available: scan barcode
2. Verify KEYCODE_ENTER captured → product added to cart
3. **LOG CHECK**: Verify KeyEvent handling

### HW-4: Hardware Diagnostics
1. Navigate to Hardware Diagnostics screen
2. Verify screen loads without crash
3. Review available hardware status information
4. **LOG CHECK**: Verify diagnostics logged

### HW-5: Print Receipt (ESC/POS)
1. ⚠️ **KNOWN NOT STARTED**: ESC/POS printing not implemented
2. Verify Print button exists but functionality is placeholder
3. **HIGHLIGHT** as not implemented

### HW-6: Torch Toggle & Haptic/Beep
1. ⚠️ **KNOWN NOT STARTED**: Torch toggle not implemented
2. ⚠️ **KNOWN NOT STARTED**: Haptic + beep not implemented
3. **HIGHLIGHT** both as not implemented

---

## 🔒 PHASE 13: Security & Auth (Module 10)

### SEC-1: Google Sign-In Flow
1. Sign out (if possible from Settings)
2. Sign back in via Google OAuth 2.0
3. Verify access token stored
4. Verify app navigates to Home after auth
5. **LOG CHECK**: Verify auth flow logged

### SEC-2: Root Detection
1. Already tested in SETUP-3
2. Verify RootBlockedScreen behavior on rooted devices

### SEC-3: Role-Based Access
1. ⚠️ **KNOWN ISSUE**: Role-based UI gating NOT implemented
2. Check UserEntity.role field value
3. **HIGHLIGHT** that Admin/Manager/Cashier permissions are not enforced

### SEC-4: Forced Update
1. ⚠️ **KNOWN ISSUE**: Update prompt dialog NOT implemented
2. Check if `min_app_version` is read from Settings tab
3. **HIGHLIGHT** that forced update UI enforcement is missing

---

## 🔁 PHASE 14: Batch Tracking Deep-Dive (Module 6)

### BATCH-1: Batch List per Product
1. Go to inventory → select a product with batches (from GRN flow)
2. ⚠️ **CHECK**: Can BatchListBottomSheet be accessed? (navigation route was marked missing)
3. If accessible: verify batch list shows all batches with:
   - Batch number
   - Stock qty
   - Mfg/expiry dates
   - Active/inactive status
4. **HIGHLIGHT** if inaccessible

### BATCH-2: Batch Expiry Badges
1. Verify expired batches show red badge
2. Verify near-expiry batches show orange badge
3. Verify normal batches show green badge

### BATCH-3: FIFO Deduction on Sale
1. Product with multiple batches → sell some quantity
2. Force Sync (triggers stock deduction)
3. Verify oldest batch is deducted first (FIFO)
4. If oldest batch reaches 0 → verify it's deactivated
5. **SHEET CHECK**: `Product_Batches` tab reflects FIFO deduction
6. **LOG CHECK**: Verify FIFO logic logged

### BATCH-4: Add Batch (Standalone)
1. ⚠️ **KNOWN ISSUE**: No standalone "Add Batch" UI outside GRN flow
2. **HIGHLIGHT** as backend-only

### BATCH-5: Edit Batch
1. ⚠️ **KNOWN ISSUE**: No batch editing dialog/screen
2. **HIGHLIGHT** as not implemented

---

## 🧾 PHASE 15: End-to-End Workflow Tests

### E2E-1: Complete Business Day Simulation
Execute in this exact order:
1. Open Till → Rs 5000 opening cash
2. Add 3 products to inventory
3. Create a vendor
4. Create a PO for those products
5. Create a GRN → receive goods → confirm
6. Sell product via cash sale
7. Sell product via udhaar → new customer
8. Process a return for first sale
9. Add expense (electricity)
10. Log wastage for one product
11. Add JAMA payment to udhaar customer
12. Force Sync
13. Check Z-Report:
    - Total sales correct?
    - Expenses listed?
    - Net cash drawer accurate?
    - Pending sync = 0?
14. Close Till
15. **FINAL SHEET CHECK**: Verify ALL tabs have consistent data

### E2E-2: Data Integrity Cross-Check
After E2E-1, verify data consistency:
1. **Inventory stock accuracy**:
   - Initial stock + GRN additions - Sales - Wastage ± Adjustments = Current stock
   - Cross-check Room value vs Sheet value
2. **Khata balance accuracy**:
   - SUM(UDHAAR events) - SUM(JAMA events) = Baqaya balance in app
   - Cross-check with Sheet
3. **Sales total accuracy**:
   - Sum of all sales in app = Sum in Sheet
   - Cash + Card + Wallet + Udhaar = Total for each sale
4. **PO↔GRN linkage**:
   - GRN items match PO items
   - PO status reflects GRN completion
5. **Till session accuracy**:
   - Opening cash + total cash sales - cash refunds = Expected cash
   - Total sales count matches actual sales

---

## 📝 FINAL REPORT TEMPLATE

### Test Summary
| Category | Total Tests | Passed | Failed | Not Implemented | Skipped |
|----------|------------|--------|--------|-----------------|---------|
| Navigation | 26 | | | | |
| Inventory | 11 | | | | |
| POS/Sales | 13 | | | | |
| Vendors/PO | 5 | | | | |
| GRN | 6 | | | | |
| CRM/Khata | 7 | | | | |
| Sync | 10 | | | | |
| Till Mgmt | 4 | | | | |
| Returns | 4 | | | | |
| Expenses | 2 | | | | |
| History | 3 | | | | |
| Hardware | 6 | | | | |
| Security | 4 | | | | |
| Batch | 5 | | | | |
| E2E | 2 | | | | |
| **TOTAL** | **~108** | | | | |

### Known Issues Found (Template)
| # | Severity | Module | Description | Sheet Impact | Steps to Reproduce |
|---|----------|--------|-------------|--------------|-------------------|
| 1 | 🔴 Critical | | | | |
| 2 | 🟡 Medium | | | | |
| 3 | 🟢 Low | | | | |

### Google Sheets Sync Report (Template)
| Sheet Tab | Rows Expected | Rows Found | Data Match | Issues |
|-----------|--------------|------------|------------|--------|
| Sales_Aug_2026 | | | ✅/❌ | |
| Inventory | | | ✅/❌ | |
| (all tabs...) | | | | |

### Features Not Implemented (Pre-Known)
1. ESC/POS Bluetooth receipt printing (button is placeholder)
2. Torch toggle in camera scanner
3. Haptic + beep on scan
4. Role-based UI gating (Admin/Manager/Cashier permissions)
5. Forced update dialog enforcement
6. Partial item-level returns (only full invoice return)
7. Batch editing dialog
8. Standalone "Add Batch" UI
9. EncryptedSharedPreferences (may be using standard SharedPreferences)
10. BatchListBottomSheet navigation (may be inaccessible)
11. Product Units dropdown in InventoryFormDialog (may be text input)
12. PO List status filter chips (may be missing)
13. History date range filter (may be missing)
14. Edit Customer dialog with full fields
