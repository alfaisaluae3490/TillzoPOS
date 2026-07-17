# Tillzo POS — Finalized Project Analysis Report

This report provides the finalized, comprehensive analysis of the **Tillzo POS** codebase. It has been updated after deeply reviewing the latest codebase index (`repomix-output.md` and the most recent `Project_Progress.txt` updates, including fixes 1 through 42).

---

## 1. Fully Completed Features (End-to-End Ready)
These modules are fully implemented with their Jetpack Compose UI screens, local Room database cache, background sync operations, and Google Sheets REST API sync targets.

### A. Core POS Checkout Engine
* **Product Search & Cart:** Debounced search queries by name, SKU, or barcode. Cart logic handles quantity additions/subtractions with full Double support for loose/decimal weights.
* **Inline Camera Barcode Scanner:** Integrated `CameraX` (480p preview) with `ML Kit Vision` for QR/barcode scanning, featuring success haptic feedback, colored border indicators, and a **4-minute inactivity auto-sleep timer** to save device battery.
* **HID Hardware Scanner:** Intercepts `KEYCODE_ENTER` in the POS search bar for external handheld physical barcode scanners.
* **Quick Pinned Grid:** Home screen quick grid showing pinned products in sorted order (uses database migration v14->v15).
* **Multi-Tender & Split Payments:** Cart supports splitting bills between Cash, Card, Mobile Wallet (JazzCash/EasyPaisa), or Udhaar (Credit). Breakdown stored in dedicated database columns.
* **Cash Change Calculator:** Live cash change calculation shown on-screen and printed on the invoice receipt.
* **Digital WhatsApp Receipts:** Generates a formatted receipt summary and shares it natively via WhatsApp API intents.
* **ZXing QR Receipt Verification:** Unique invoice UUID encoded as a high-density QR code on the receipt for fraud check.
* **Till Session Management:** Cash entry screens and opening/closing cash drawers, recording drawer status, and calculating live discrepancies upon closing.

### B. Inventory & Product Administration
* **Product Form CRUD:** Full add/edit/delete form dialogs covering SKU, barcodes, category selection, price per unit, stock levels, low-stock alerts threshold, dates (mfg/expiry), and alert lead days.
* **Advanced Alert Screens:** A 3-tab `StockAlertsScreen` (Low Stock / Out of Stock / Expiring Soon) showing colored badges, item details, and days-remaining counters.
* **Soft Deletions:** Deleting an item marks `is_deleted = 1` in Room and synchronizes the soft-deletion to Google Sheets instead of hard-deleting the row.
* **Category Management:** Standalone screen to add/delete categories, loaded as a dropdown menu in the inventory dialog.
* **Product Unit Dropdown:** Replaced the plain unit text field in the product form with a Hilt-bound `ExposedDropdownMenuBox` listing custom product units from the database.

### C. Store Operations & Accounting
* **Append-Only Khata Ledger (CRM):** Customer profiles with outstanding balances. Adding Udhaar or Jama creates ledger event records. Outstanding balances are dynamically aggregated in a scrollable `LazyColumn` showing complete event history.
* **Wastage Ledger Logging:** Standalone screen (`WastageLogScreen`) with product search, summaries of total loss values, and auto-deduction of stock upon saving a wastage item.
* **Z-Report (Shift Close):** Shift summary sheet showing cash drawer status, live discrepancy calculations, expected cash calculations, and a "Close Till" lock that blocks execution if there are pending sync records.
* **Expense Tracking:** Expense entry form logging categories, amounts, descriptions, and users, synced to the `Expenses` Google Sheets tab.
* **Returns & Refunds:** Search past invoice → perform full return → select Restock (recalculates/adds stock) or Damaged (writes to Wastage Ledger and does not add stock). Logs negative sale rows.

### D. Sync, Security & Architecture
* **Google OAuth 2.0 PKCE:** Verified user login using Google Accounts via `AppAuth-Android`.
* **SyncWorker Uploads:** Syncs Sales, Inventory, Customers, Khata Events, Expenses, Categories, and User Permissions. Now also supports uploading **Purchase Orders (PO)**, **GRNs**, **Product Batches**, **Vendors**, and **Till Sessions** to Google Sheets.
* **Monthly Sharding:** Automatic creation of monthly sales sheets (e.g., `Sales_Jun_2026`) and archiving of past sales sheets.
* **Delta Sync & Schema Guard:** Polling Settings tab for changes every 60s, pulling new remote rows, and verifying workbook schemas on launch.
* **Root Detection:** `RootBeer` checking on startup that redirects users to a blocked screen if the device is rooted.

---

## 2. Partially Developed Features (Gaps Identified)
These features are implemented in the code, but they contain logical bugs or configuration gaps that prevent production-grade usage.

### A. Non-Persistent Printer Config & Hardcoded MAC Addresses
* **Status:** Printer Driver Done ↔ Settings Not Saved
* **The Gap:** The `EscPosPrinter` and `TsplPrinter` classes are fully written (Bluetooth serial socket connection, network TCP/IP port 9100, 3-retry logic). The `PrinterSettingsScreen` allows users to type MAC and IP addresses. 
* **The Bug:** These values are only kept in-memory in the `PrinterSettingsViewModel` and **are not saved to local storage** (`SharedPreferences` or database). When the app restarts, they reset. Furthermore, `HistoryViewModel` and `QrGeneratorViewModel` use a **hardcoded test MAC address (`00:00:00:00:00:00`)** instead of reading user-defined values, making real printing impossible outside the test screen.

### B. Role-Based UI Gating
* **Status:** Database Fields Done ↔ UI Logic Missing
* **The Gap:** The database structure supports roles (Admin, Manager, Cashier), but the presentation layer does not restrict access to settings, user creation, or price modifications based on the logged-in user's role.

---

## 3. Missing / Unstarted Features (From Blueprint)
These specifications from the blueprint have not yet been started or implemented.

1. **Google Play Subscription Billing:** Official Play Billing Library integration to manage subscriptions, trials, and EXPIRED blocks (grace periods).
2. **Forced App Updates UI:** Comparing the local version code to the `min_app_version` cell fetched on launch from the Sheets `Settings` tab and prompting a blocking update dialog on Day 4.
3. **Disaster Recovery Restore Flow:** While daily backups are uploaded to the backup sheet, the interface lacks a "Restore Database from Cloud Backup" button to reload a workbook into a clean local Room database.
4. **Flashlight Toggle & Beep Sounds on ML Kit Scan:** Toggle for flashlight inside the Camera Scanner Box, and beep sounds on a successful barcode read.

---

## 4. Crucial Business Logic Missing (Operational Gaps)
To operate a real retail or wholesale business, the app currently lacks key operational features:

### 1. Localization & Currency Formatting
* **Current Issue:** Currency symbols are hardcoded as `Rs.` or `PKR`.
* **Requirement:** Dynamic currency formatting based on location settings (e.g., `$` for US, `€` for Europe, `₹` for India).

### 2. Tax Configurations (Inclusive vs. Exclusive)
* **Current Issue:** A simple tax percentage is saved per product, but there is no logic to toggle between Tax-Inclusive pricing (standard in retail) and Tax-Exclusive pricing (standard in wholesale).

### 3. Discount Engine (Cart & Item Level)
* **Current Issue:** There is no way to apply flat-amount discounts, percentage discounts, or discount codes at checkout.
* **Requirement:** Add a discount field to the POS cart, allowing cashiers to subtract value from the subtotal.

### 5. Cashier Manager Overrides (Price Locks)
* **Current Issue:** Cashiers are not allowed to edit prices, but there is no mechanism for an Admin/Manager to enter their PIN on the cashier's screen to temporarily override a price or approve a refund.

---

> [!IMPORTANT]
> **Primary Recommendation:** 
> Before introducing new modules, the very first task should be **resolving the Printer Configuration Bug (Section 2-A)**. By saving the printer MAC/IP addresses to `AppSetupPrefs` (or a dedicated Settings Room Table) and referencing them in the print viewModels instead of the hardcoded `"00:00:00:00:00:00"`, the app's printing flows (Receipts, Z-Reports, Duplicates, and Labels) will instantly become functional and testable on physical hardware.
