# Tillzo POS — Step-by-Step Complete Testing Guide

This guide is structured in a logical dependency-first order. You must perform the tests in this sequence to ensure that each feature has the necessary data (e.g. creating categories and vendors before adding products, opening the till before making sales, etc.).

---

## Phase 1: Authentication & Database Provisioning

### 1.1 Google Authentication Login
1. Open the app. You should see the **Splash/SignInScreen** with a "Continue with Google" button.
2. Tap **Continue with Google**.
3. Select your Google Account.
4. *Verification:* Verify it logs in successfully. If it fails, check the error message (ensure your debug SHA-1 signature matches GCP).

((((((Done))))))

### 1.2 Automatic Database Setup (Google Sheets)
1. After login, the app will auto-provision your system.
2. *Verification:* Open your Google Drive in a web browser. Verify a new spreadsheet named **TillzoPOS_[YourName]** has been created.
3. Open the spreadsheet and verify that all required tabs are created:
   * `Inventory`, `Customers`, `Khata_Events`, `Expenses`, `Users_Permissions`, `Settings`, `Sync_Log`, and a hidden `SYS_DB_DO_NOT_TOUCH` tab.

(((((((((Done)))))))))

### 1.3 Quick PIN Setup
1. Setup a 4-digit security PIN when prompted.
2. Close the app, re-open it, and verify you can unlock the app quickly using only this PIN.

(((((((((Done)))))))))

## Phase 2: Metadata & Configuration Setup (Dependencies)

### 2.1 Category Management
1. Open the drawer menu (Advanced Menu) on the top-left and select **Category Management**.
2. Tap **+ Add Category**. Create three categories: `Beverages`, `Snacks`, and `Dairy`.
3. Try to soft-delete one category.
4. *Verification:* Verify the deleted category is removed from the active screen list.

(((((((Done)))))))

### 2.2 Product Units Management
1. In the menu, navigate to **Product Units**.
2. Add custom units: `KG`, `Litre`, and `Pieces`.
3. *Verification:* Ensure the added units display correctly in the list.

((((((((((((((Done))))))))))))))


### 2.3 Vendor Profiles
1. Navigate to **Vendor Management** from the menu.
2. Tap **+ Add Vendor**.
3. Add a vendor with name, phone number, WhatsApp, email, and address.
4. *Verification:* Ensure the vendor is saved in the local list and is searchable.

((((((((((((((((((Done))))))))))))))))))

## Phase 3: Inventory Creation & Intake

### 3.1 Product CRUD & Auto Barcode Allocation
1. In the menu, go to **Inventory**. Tap **+ Add Product**.
2. Fill in the fields:
   * **Name:** `Mineral Water 1L`
   * **SKU/Barcode:** Tap the *Auto-Generate* button (it should allocate a `CUST-10000x` style ID).
   * **Category:** Select `Beverages` from the dropdown.
   * **Unit:** Select `Pieces` from the dropdown.
   * **Prices:** Cost Price: `40`, Selling Price: `60`, Tax: `0%`.
   * **Stock:** Initial Stock: `10`, Low Stock Threshold: `3`.
3. Save the product.
4. Add another product: `Fresh Milk 1L` (Category: `Dairy`, Unit: `Litre`, Cost: `180`, Selling: `220`, Initial Stock: `2`, Low Threshold: `5`, Expiry Date: Set to 3 days from today).
5. *Verification:* Ensure both products show up in the inventory list with correct stock levels and alerts (Milk should show a **Low Stock** and **Expiring Soon** warning).


((((((((((((((((Done))))))))))))))))

### 3.2 Verification of Product Edits & Soft Deletion
1. Edit `Mineral Water 1L`, change the Selling Price to `70`. Save and verify the update.
2. Delete `Mineral Water 1L` using the delete icon.
3. *Verification:* Verify it disappears from the active inventory list.

### 3.3 Barcode/QR Generation
1. Select `Fresh Milk 1L` from the inventory list.
2. Tap **Print QR**.
3. *Verification:* Ensure a high-density QR code based on the milk's barcode ID is displayed on-screen.

((((((((((((((((Done))))))))))))))))
---

## Phase 4: Purchase Orders (PO) & Receipts (GRN)

### 4.1 Create & Share Purchase Order
1. Go to Menu > **Purchase Orders**. Tap **+ Create PO**.
2. Select the vendor created in Phase 2.
3. Search and add `Fresh Milk 1L` to the items list. Set ordered quantity to `20`.
4. Save the PO.
5. *Verification:* Verify the PO shows status as `SENT`. Open the PO details and tap **Share PO**. Verify the native WhatsApp share intent opens with a correct text summary.

### 4.2 Receive Stock via GRN (FIFO Batch Intake)
1. Go to Menu > **GRN List**. Tap **Create GRN** (or navigate from the PO detail screen).
2. Select the PO created in 4.1.
3. Edit the received quantity (e.g. `20`).
4. Select the **Inventory Action**:
   * Choose **ADD_BATCH** (since this is a new intake of milk).
   * Enter Batch Number: `BATCH-MILK-99`.
   * Set Manufacturing Date (today) and Expiry Date (7 days from today).
5. Tap **Confirm GRN**.
6. *Verification:* Verify the success screen shows the summary counts. Go to **Inventory** and verify that `Fresh Milk 1L` current stock has increased from `2` to `22` (2 + 20).

---

## Phase 5: Till Session & Manual Adjustments

### 5.1 Manual Stock Adjustment (Stock Audits)
1. Go to Menu > **Stock Adjustment**.
2. Search and select `Fresh Milk 1L`.
3. Enter Adjustment Qty: `-2` (simulating a broken/spilled bottle). Select type as *Deduction*, enter the reason, and save.
4. *Verification:* Go to **Inventory** and verify that `Fresh Milk 1L` stock has updated to `20`.

### 5.2 Opening the Cash Drawer (Till Session)
1. Go to Menu > **Open Till**.
2. Enter the opening float cash: `5,000`. Save.
3. *Verification:* Ensure the Till status changes to open, which unlocks the POS billing screen.

---

## Phase 6: POS Selling & Checkout Engine

### 6.1 HomeScreen Quick Grid Pinning
1. Go to Menu > **Inventory**.
2. Select `Fresh Milk 1L`. Tap the **Pin** icon and enter your Admin PIN.
3. *Verification:* Go back to the main POS/Checkout screen. Verify that `Fresh Milk 1L` appears as a quick-selection tile in the pinned items grid.

### 6.2 Cart Operations & Decimal Weight Inputs
1. On the POS screen, tap the `Fresh Milk 1L` tile. Verify it is added to the cart with quantity `1.0`.
2. Tap the item in the cart to change quantity to `1.5` (simulating loose sales). Verify the subtotal calculates correctly.
3. Use the search bar to find products by name/SKU.
4. Use the inline camera scanner toggle. Scan a barcode and verify it is auto-added to the cart.

### 6.3 Checkout & Payment Split
1. Tap the green **Pay / Checkout** button.
2. Select **Split Payment**:
   * Cash Amount: `200`
   * Card Amount: (Enter the remaining balance)
3. Select **Udhaar (Credit)** option:
   * Search and select a customer. If none exists, tap **+ Create Customer** inline, fill in name and phone, and save.
4. Complete the transaction.
5. *Verification:* Ensure the **Receipt Screen** displays showing invoice ID, items, subtotal, payment split breakdown, cash change, and the QR verification code.

---

## Phase 7: CRM Ledger, Returns & Wastage Logs

### 7.1 Customer CRM Ledger Verification
1. Go to Menu > **CRM**.
2. Select the customer used for the credit sale in Phase 6.
3. *Verification:* 
   * Verify their aggregated Udhaar balance displays in the statistics card.
   * Verify the ledger list shows the credit transaction details (date, time, amount, and invoice link).
4. Tap **Record Jama (Payment)**. Enter `100` and save. Verify the balance decreases and a payment event is added to the ledger list.

### 7.2 Sales Returns & Condition Actions
1. Go to Menu > **Returns**.
2. Scan the receipt QR code or enter the invoice UUID from Phase 6.
3. Select the return item and toggle the condition:
   * **Restock:** Complete the return and verify the product's inventory stock increments.
   * **Damaged (Wastage):** Complete the return and verify the stock does NOT increase.

### 7.3 Wastage Ledger Validation
1. Go to Menu > **Wastage Log**.
2. *Verification:* Verify that the item returned as "Damaged" in 7.2 appears as an entry in the wastage ledger with correct cost value losses.

---

## Phase 8: Financial Closure & Sync Verification

### 8.1 Expense Logging
1. Go to Menu > **Expenses**. Tap **+ Add Expense**.
2. Enter category: `Utilities`, amount: `1500`, description: `Electricity bill`, and save.
3. *Verification:* Verify it appears in the list.

### 8.2 Z-Report Shift Close
1. Go to Menu > **Z-Report**.
2. Verify it displays:
   * Expected cash in drawer (Opening Cash + Cash Sales - Cash Returns - Expenses).
   * Card/Wallet payment receipts.
3. Enter the physical cash counted in your drawer.
4. *Verification:* Verify it calculates the variance automatically. Tap **Close Till** to close the session.

### 8.3 Google Sheets Synchronization Check
1. Turn off Wi-Fi/data on the device. Create a sale. Verify the sync status indicator shows "Pending".
2. Turn Wi-Fi back on. Tap **Force Sync** in the advanced menu.
3. Open your Google Sheet in a web browser.
4. *Verification:* Verify that:
   * The new products are added to the `Inventory` tab.
   * The sale is appended to the monthly `Sales_[Month]_[Year]` tab.
   * The customer and payment events appear in `Customers` and `Khata_Events` tabs.
   * The expense is logged in the `Expenses` tab.
   * The `SYS_DB_DO_NOT_TOUCH` sheet remains hidden from the tab list.
