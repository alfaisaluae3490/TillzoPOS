# Tillzo POS — Static Code Trace & Testing Guide Compliance Review

This report presents a static code analysis and data flow trace of the Tillzo POS application codebase against the provided **8-Phase Testing Guide**. It maps out where features will fail, identify database calls lacking error handling, and highlights sections of the testing guide that currently have **zero code** implemented.

---

## 🚨 Crucial Severity Summary

| Phase | Feature / Test Case | Code Status | Crash / Data Bug Risk |
| :--- | :--- | :--- | :--- |
| **Phase 1** | 1.1 Live Camera preview binding | Implemented but flawed | **High** (Blocks main thread, potential ANR/Crash) |
| **Phase 3** | 3.1 Initial stock batched inventory | Implemented but flawed | **Critical** (Initial manual stock wiped out during GRN recalculations) |
| **Phase 4** | 4.1 Create PO "Save & Share" Status | Implemented but flawed | **Medium** (Saves as `DRAFT` instead of `SENT` as expected by guide) |
| **Phase 5** | 5.1 Manual Stock Adjustment | Implemented but flawed | **Critical** (Stock adjusted in Product but not in Batches; gets overwritten) |
| **Phase 6** | 6.1 HomeScreen Quick Grid Pinning | **Unimplemented** (0% Code) | **Blocker** (UI pin action and PIN verification does not exist) |
| **Phase 6** | 6.2 Cart Operations (Decimal Qty / Litre Qty) | Implemented but flawed | **Medium** (Decimal inputs blocked in cart view; non-decimal step values for Litres) |
| **Phase 6** | 6.3 Checkout & Payment Split (Udhaar sign mismatch) | Implemented but flawed | **High** (Mathematically inverted signs between POS credit sales and CRM ledger) |
| **Phase 8** | 8.1 Expense Logging vs. Active Till Session | Implemented but flawed | **High** (Expense not recorded in active till drawer, causing cash variance error) |
| **Phase 8** | 8.2 Z-Report Shift Lock & Close Day | Implemented but flawed | **High** (Z-Report does not close the session, registers are not locked on close) |

---

## 🔍 Detailed Data Flow Trace & Compliance Analysis

### Phase 1: Authentication & Database Provisioning
*   **1.1 Google Authentication Login & 1.2 Database Provisioning (Sheets):**
    *   **Status:** Implemented.
    *   **⚠️ Crash Risk / UI Thread Block:** In [InlineCameraBox.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/hardware/scanner/InlineCameraBox.kt#L74) and line 125, the code calls `ProcessCameraProvider.getInstance(context).get()` directly inside the `LaunchedEffect` coroutine without switching dispatchers. Since `LaunchedEffect` runs on the main thread (composition dispatcher) by default, calling `.get()` on a `ListenableFuture` is a blocking call. This blocks the main UI thread during camera provider initialization, which can trigger an Android **ANR (Application Not Responding) crash** on slower devices.
*   **1.3 Quick PIN Setup:**
    *   **Status:** Implemented and functional. No immediate crash vectors identified.

---

### Phase 2: Metadata & Configuration Setup
*   **2.1 Category Management, 2.2 Product Units, and 2.3 Vendor Profiles:**
    *   **Status:** Implemented.
    *   **⚠️ Missing DB Error Handling:** In the viewmodels (`CategoryViewModel`, `ProductUnitsViewModel`, `VendorViewModel`), Room inserts/deletes are executed inside `viewModelScope.launch` with no `try-catch` blocks. A constraint violation (e.g., trying to write a duplicate unit or category name if unique indexes are enforced, or SQLite disk write failure) will raise an unhandled exception, causing the app to crash.
    *   **⚠️ Thread Safety:** Several database writes inside `VendorViewModel` and `ProductUnitsViewModel` do not explicitly define `Dispatchers.IO` inside the launch block, running on the Main thread by default.

---

### Phase 3: Inventory Creation & Intake
*   **3.1 Product CRUD & Auto Barcode Allocation:**
    *   **Status:** Implemented but contains a **Critical Logic & Data Integrity Bug**.
    *   **🔥 The "Phantom Initial Stock" Bug:** When a user creates a product manually via `InventoryCrudScreen` and inputs an initial stock (e.g. `Fresh Milk 1L` with initial stock: `2` and `Mineral Water 1L` with initial stock: `10`), the product row is inserted into the `Inventory` table with `current_stock = 2` or `10`. However, **no batch is created** in the `ProductBatchEntity` table.
    *   When the user later confirms a GRN (or updates any batch) for that product:
        1. `ConfirmGrnUseCase` performs its actions.
        2. It calls `inventoryRepository.recalculateTotalStock(productId)`.
        3. `recalculateTotalStock` queries all active batches for this product from the database, sums their quantities, and writes the total back to `current_stock`.
        4. Since the initial stock of `2` was never written to a batch, the total active batch stock is calculated solely from the GRN intake (e.g., 20).
        5. The product's `current_stock` is updated from `22` (2 + 20) to `20`! The initial `2` units of stock are **completely wiped out** and lost.
*   **3.2 Product Edits & Soft Deletion:**
    *   **Status:** Implemented. Soft deletion works, but there is no cascade logic to clean up or mark active product batches as deleted in the `ProductBatchEntity` table, leaving orphaned active batches in the DB.
*   **3.3 Barcode/QR Generation:**
    *   **Status:** Implemented.
    *   **⚠️ Hardcoded Currency Defect:** The barcode detail screen and other inventory views have the currency symbol hardcoded to `"₹"` (Rupees), whereas other parts of the application (such as the cart/receipt) display `"Rs"`.

---

### Phase 4: Purchase Orders (PO) & Receipts (GRN)
*   **4.1 Create & Share Purchase Order:**
    *   **Status:** Implemented but deviates from expectations.
    *   **⚠️ Status Logic Mismatch:** Clicking the "Save & Share" button on the PO screen calls `CreatePurchaseOrderViewModel.savePO`, which always sets the PO status to `"DRAFT"`. The testing guide explicitly specifies: *"Verify the PO shows status as SENT."* Under the current implementation, the user must open the saved PO details and manually tap the "Mark as SENT" button in the bottom bar to transition it out of draft status.
*   **4.2 Receive Stock via GRN:**
    *   **Status:** Implemented.
    *   **⚠️ Missing DB Error Handling:** In [CreateGrnViewModel.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/viewmodel/CreateGrnViewModel.kt#L142), `saveAndConfirmGRN()` has no try-catch surrounding the database insert methods (`saveGrnDraftUseCase` and `confirmGrnUseCase`). Any primary key collision or foreign key issue will cause the app to crash.

---

### Phase 5: Till Session & Manual Adjustments
*   **5.1 Manual Stock Adjustment (Stock Audits):**
    *   **Status:** Implemented but contains a **Critical Logic/Data Reversion Bug**.
    *   **🔥 The "Stock Adjustment Reversion" Bug:** In [StockAdjustmentScreen.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/StockAdjustmentScreen.kt#L88), `saveAdjustment()` updates the `current_stock` column directly on the product's `InventoryEntity` in the database. It **does not update or adjust any batch quantities** in the `ProductBatchEntity` table.
    *   Because the batches remain unadjusted, the next time `recalculateTotalStock(productId)` is triggered (such as during a GRN receipt or when editing a batch manually), the system will recalculate the stock based on the unadjusted batches, **reverting the manual stock adjustment** (re-introducing the damaged/lost inventory back into the active stock count).
*   **5.2 Opening the Cash Drawer (Till Session):**
    *   **Status:** Implemented.

---

### Phase 6: POS Selling & Checkout Engine
*   **6.1 HomeScreen Quick Grid Pinning:**
    *   **❌ Zero Code Written (Unimplemented):** Although the `Inventory` table schema contains an `isPinned` column and the DAO has an `updatePinStatus` query, there is **no UI or ViewModel code** allowing the user to select/pin an item. The Pin button does not exist in `InventoryItemCard` or product details. There is also **no Admin PIN prompt** or verification logic implemented.
*   **6.2 Cart Operations & Decimal Weight Inputs:**
    *   **Status:** Implemented but contains severe UX and logical defects.
    *   **⚠️ Click Interaction Blocked:** The testing guide requests: *"Tap the item in the cart to change quantity to 1.5."* In [HomeScreen.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/home/HomeScreen.kt#L555), the `CartRow` item layout is **not clickable**. There is no input dialog triggered when tapping a cart row; quantity can only be altered using the plus/minus buttons.
    *   **⚠️ Unit Step Bug:** The step-size check is hardcoded to `if (cartItem.unit in listOf("KG", "GM", "ML")) 0.1 else 1.0`. Since the unit for "Fresh Milk 1L" is `Litre` or `L`, it does not match this list. Therefore, tapping `+`/`-` will step by `1.0` instead of `0.1` and the cart row formatter (`item.quantity.toInt()`) will hide any decimals, blocking the decimal weight input verification.
*   **6.3 Checkout & Payment Split:**
    *   **Status:** Implemented but contains a **Severe Mathematical Bug**.
    *   **🔥 The "Inverted Udhaar Signs" Bug:** When a credit sale is processed in [CompleteSaleUseCase.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/domain/usecase/CompleteSaleUseCase.kt#L125), the `KhataEventEntity` amount is saved as a positive value (`amount = udhaarAmount`).
    *   However, when a manual `UDHAAR` event is recorded through the CRM in [CrmViewModel.kt](file:///c:/Users/Faisal%20Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/store/options/crm/CrmViewModel.kt#L113), the amount is saved as a negative value (`-amount`).
    *   Because positive values represent `JAMA` (payments received), the credit sale transaction of e.g. `200` is recorded as a customer payment in the database. When the customer pays back `100` via Jama (recorded as positive `+100`), the customer's total outstanding balance is calculated as `+300` instead of `+100` (increasing their outstanding debt calculation).

---

### Phase 7: CRM Ledger, Returns & Wastage Logs
*   **7.1 Customer CRM Ledger Verification:**
    *   **Status:** Implemented. However, due to the inverted sign bug in Phase 6.3, the ledger statistics cards and balance decrease/increase calculations display corrupt figures when recording payments.
*   **7.2 Sales Returns & Condition Actions:**
    *   **Status:** Implemented.
    *   **⚠️ Inconsistent Cashier Session:** In `ReturnsViewModel.kt`, the process return function has a hardcoded `cashierId = "user_1"` instead of querying the active cashier session from configuration preference settings.
*   **7.3 Wastage Ledger Validation:**
    *   **Status:** Implemented.

---

### Phase 8: Financial Closure & Sync Verification
*   **8.1 Expense Logging:**
    *   **Status:** Implemented.
    *   **🔥 Cash Drawer Mismatch:** Adding an expense in `ExpenseViewModel` updates the `Expenses` database table but **does not deduct the cash amount** from the active `TillSessionEntity`. Consequently, the cashier's expected cash in drawer does not decrease, which triggers a cash drawer variance mismatch when attempting to end the shift.
*   **8.2 Z-Report Shift Close:**
    *   **Status:** Implemented but incomplete.
    *   **⚠️ POS Register Lock Missing:** There is **no logic** inside the main billing view (`HomeScreen`) to lock the interface when no till session is open or after a shift is closed. Cashiers can continue checking out carts and making transactions indefinitely.
    *   **⚠️ Z-Report Payment Breakdowns Missing:** `ZReportViewModel` calculates only total gross sales and total expenses. It does not calculate or display the split payments breakdown (Card, Wallet, Udhaar, and Cash Sales) as requested in the Z-Report verification list.
    *   **⚠️ Close Day Action Incomplete:** Tapping "CLOSE DAY (Z-REPORT)" in `ZReportViewModel` sets a UI message and triggers a print draft but **fails to close the active till session** in the database.

---

## 🛠️ Actionable Correction Recommendations

1.  **Thread Blocking (Phase 1.1):** Update `InlineCameraBox.kt` to load `ProcessCameraProvider` asynchronously utilizing standard CameraX listenable futures and a callback executor instead of a direct `.get()` call.
2.  **Initial Stock Batches (Phase 3.1):** Update `saveItem` inside `InventoryCrudViewModel.kt` to insert an initial, active `ProductBatchEntity` record matching the initial stock and purchase price whenever a new product is added.
3.  **Stock Adjustments (Phase 5.1):** Update `saveAdjustment` in `StockAdjustmentViewModel.kt` to adjust the stock quantity of the product's most recent active batch in addition to updating the product's total inventory count.
4.  **Pinning Feature (Phase 6.1):** Build the missing quick-pin icon on the inventory list card, add the Admin PIN verification popup, and call `inventoryDao.updatePinStatus()` on success.
5.  **Decimal Weights (Phase 6.2):** Make the cart row card clickable to open the decimal quantity dialog. Add `"Litre"` and `"L"` to the fractional unit list so that quantity increments behave correctly for liquid sales.
6.  **Ledger Signs (Phase 6.3):** Standardize `KhataEventEntity` so that debt increases outstanding balance consistently, and verify that POS sales and manual CRM adjustments apply matching signs.
7.  **Financial Integrity (Phase 8.1 & 8.2):**
    *   Call `tillSessionDao.deductCash` when cash-source expenses are recorded.
    *   Update `HomeScreen.kt` to display a lock overlay or redirect to `TillOpenScreen` if there is no open till session.
    *   Update `ZReportViewModel` to sum split sale fields and close the database till session upon close day execution.
