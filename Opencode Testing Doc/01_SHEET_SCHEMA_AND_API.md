# 01 — PHASE 1: MICRO-MAPPING MATRIX (SHEETS / API / SYNC / DB)

Hermes: read this file completely. Do not execute anything in it. It is the ground-truth map every other file references.

---

## 1.1 GOOGLE SHEET TAB MANIFEST

Tabs created at workspace provisioning (`SheetsRepository.createWorkspace`, in this order; `properties: {title, index}`):

1. `Sales_MMM_YYYY` (current month, e.g. `Sales_Aug_2026`; created dynamically via `resolveSalesTab()`)
2. `Inventory`
3. `Customers`
4. `Khata_Events`
5. `Expenses`
6. `Categories`
7. `Returns`
8. `Wastage_Ledger`
9. `Users_Permissions`
10. `Purchase_Orders`
11. `PO_Items`
12. `GRN_Headers`
13. `GRN_Items`
14. `Vendors`
15. `Product_Batches`
16. `Product_Units`
17. `Till_Sessions`
18. `Stock_Adjustments`
19. `BarcodeGeneralConfigs`
20. `BarcodeFieldConfigs`
21. `Settings`
22. `Sync_Log`
23. `Dashboard`
24. `SYS_DB_DO_NOT_TOUCH` (hidden tab; never shown to users)

Additional tabs created later by workers:
- `Time_Clock` — header range written by `buildHeaders()` but **NOT in the createWorkspace tab list** (defect candidate — see 09)
- `ARCH_Sales_MMM_YYYY` — previous month renamed at monthly shard
- `Sales_MMM_YYYY_OVF` — overflow tab when current sales tab reaches 18,000 rows

Entity → tab write mapping (single source of truth):

| Entity (Room table) | Sheet tab |
|---|---|
| SaleEntity (`Sales`) | `Sales_MMM_YYYY` |
| InventoryEntity | `Inventory` |
| CustomerEntity | `Customers` |
| KhataEventEntity (`KhataEvents`) | `Khata_Events` |
| ExpenseEntity | `Expenses` |
| CategoryEntity | `Categories` |
| UserEntity (`Users_Permissions`) | `Users_Permissions` |
| TimeClockEntity (`Time_Clock`) | `Time_Clock` |
| ProductUnitEntity (`product_units`) | `Product_Units` |
| TillSessionEntity | `Till_Sessions` |
| PurchaseOrderEntity | `Purchase_Orders` |
| PurchaseOrderItemEntity | `PO_Items` |
| GrnHeaderEntity | `GRN_Headers` |
| GrnItemEntity | `GRN_Items` |
| VendorEntity | `Vendors` |
| ProductBatchEntity | `Product_Batches` |
| StockAdjustmentEntity | `Stock_Adjustments` |
| WastageEntity | `Wastage_Ledger` |
| (settings keys) | `Settings` (cols `setting_key, setting_value`; seeded `last_updated_timestamp`="0", `min_app_version`="1", `shop_name`) |
| (sync records) | `Sync_Log` (cols `sync_uuid, pos_id, status, timestamp, error_msg`) |
| (system) | `SYS_DB_DO_NOT_TOUCH` (cols `schema_version, last_verified, integrity_check`) |
| (no writer) | `Returns`, `Dashboard`, `BarcodeGeneralConfigs`, `BarcodeFieldConfigs` |

## 1.2 EXACT COLUMN ORDER PER TAB (from `utils/Constants.kt` — `SheetColumns`)

### INVENTORY (25 cols)
`system_row_id, barcode_id, name, sku, category, brand, description, cost_price, selling_price, tax_percent, unit, stock_qty, low_threshold, batch_number, expiry_date, manufacturing_date, expiry_alert_days, is_damaged, damaged_qty, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### SALES (25 cols)
`invoice_id, pos_id, timestamp, items_json, subtotal, tax, discount, total, payment_method, cash_amount, card_amount, wallet_amount, udhaar_amount, customer_id, payment_split_json, reference_id, cashier_id, sync_uuid, is_deleted, deleted_at, sync_status, pos_terminal_id, system_row_id, created_at, updated_at`

### CUSTOMERS (14 cols)
`system_row_id, name, phone, whatsapp, email, address, loyalty_points, lifetime_spend, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### KHATA_EVENTS (12 cols)
`system_row_id, customer_id, event_type, amount, note, reference_sale_id, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### EXPENSES (12 cols)
`system_row_id, category, amount, description, timestamp, logged_by_user_id, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### CATEGORIES (9 cols)
`system_row_id, category_name, parent_category_id, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### USERS (12 cols)
`system_row_id, email, name, role, password_hash, permissions_json, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### PURCHASE_ORDERS (14 cols)
`po_id, po_number, vendor_id, vendor_name, status, notes, total_amount, currency, expected_delivery_date, created_by, sync_status, pos_terminal_id, created_at, updated_at`

### PO_ITEMS (14 cols)
`po_item_id, po_id, product_id, product_name, sku, barcode_id, ordered_qty, received_qty, unit_cost_price, total_cost, unit, sync_status, created_at, updated_at`

### GRN_HEADERS (15 cols)
`grn_id, grn_number, po_id, vendor_id, vendor_name, status, notes, received_by, total_amount, sync_status, pos_terminal_id, attached_file_id, attached_file_url, created_at, updated_at`

### GRN_ITEMS (19 cols)
`grn_item_id, grn_id, po_item_id, product_id, product_name, barcode_id, sku, received_qty, unit_cost_price, total_cost, unit, batch_number, manufacturing_date, expiry_date, inventory_action, is_new_item, sync_status, created_at, updated_at`

### VENDORS (13 cols)
`vendor_id, name, phone, whatsapp, email, address, city, credit_limit, is_active, is_deleted, sync_status, created_at, updated_at`

### PRODUCT_BATCHES (16 cols)
`batch_id, product_id, barcode_id, batch_number, manufacturing_date, expiry_date, stock_qty, cost_price, selling_price, is_active, is_deleted, deleted_at, sync_status, pos_terminal_id, created_at, updated_at`

### PRODUCT_UNITS (7 cols — camelCase!)
`unitId, unitName, abbreviation, isDeleted, syncStatus, createdAt, updatedAt`

### TILL_SESSIONS (22 cols)
`session_id, cashier_id, cashier_name, pos_terminal_id, opening_cash, closing_cash, expected_cash, total_cash_sales, total_card_sales, total_wallet_sales, total_udhaar_sales, total_sales_count, total_refunds, net_cash, status, notes, shift_date, opened_at, closed_at, sync_status, created_at, updated_at`

### TIME_CLOCK (10 cols)
`system_row_id, employee_email, employee_name, event_type, timestamp, note, pos_terminal_id, created_at, updated_at, sync_status`

### WASTAGE_LEDGER (17 cols)
`wastage_id, product_id, product_name, batch_id, batch_number, quantity, unit, cost_price, total_loss, reason, notes, logged_by, wastage_date, sync_status, pos_terminal_id, created_at, updated_at`

### STOCK_ADJUSTMENTS (10 cols)
`adjustment_id, product_id, adjustment_type, quantity_changed, reason, adjusted_by, sync_status, pos_terminal_id, created_at, updated_at`

### RETURNS (13 cols — no writer exists)
`return_id, system_row_id, original_invoice_id, item_id, qty_returned, condition, refund_method, amount, last_updated, sync_status, created_at, updated_at, pos_terminal_id`

### SPECIAL TABS
- `Settings`: `setting_key, setting_value` (seeded rows: `last_updated_timestamp`=0, `min_app_version`=1, `shop_name`)
- `Sync_Log`: `sync_uuid, pos_id, status, timestamp, error_msg`
- `SYS_DB_DO_NOT_TOUCH`: `schema_version, last_verified, integrity_check`

## 1.3 REST ENDPOINTS (Retrofit `SheetsApiService`, base `https://sheets.googleapis.com/v4/`)

| # | Method + URL | Params | Purpose |
|---|---|---|---|
| E1 | `POST spreadsheets` | body `{properties:{title}, sheets:[{properties:{title,index}}]}` | create spreadsheet |
| E2 | `GET spreadsheets/{spreadsheetId}/values/{range}` | range path (not encoded) | read values |
| E3 | `POST spreadsheets/{spreadsheetId}/values/{range}:append` | `valueInputOption=RAW`, `insertDataOption=INSERT_ROWS`; body `{range, majorDimension:"ROWS", values}` | append rows (range `"$tab!A:ZZ"` for backup, `"$tab!A:B"` for Settings) |
| E4 | `POST spreadsheets/{spreadsheetId}/values:batchUpdate` | `valueInputOption:"RAW"`, `data:[{range, majorDimension:"ROWS", values}]` | in-place cell updates; 200-with-`responses[].error` = failure |
| E5 | `POST spreadsheets/{spreadsheetId}:batchUpdate` (structural, NO `/values/`) | `requests`: `addSheet{properties:{title}}`, `updateSheetProperties{properties:{sheetId,title}, fields:"title"}` / `{properties:{sheetId,hidden}, fields:"hidden"}`, `deleteDimension{range:{sheetId, dimension:"ROWS", startIndex:rowIndex-1, endIndex:rowIndex}}` | add/rename/hide tabs, delete rows |
| E6 | `GET spreadsheets/{spreadsheetId}?fields=sheets.properties` | — | metadata: title → numeric sheetId |
| E7 | `GET https://www.googleapis.com/drive/v3/files` | `q`, `fields=files(id,name,createdTime,modifiedTime,appProperties)`, `spaces=drive` | search Drive files |
| E8 | `POST https://www.googleapis.com/drive/v3/files` | body `{name, mimeType:"application/vnd.google-apps.folder"}` | create Drive folder |
| E9 | `PATCH https://www.googleapis.com/drive/v3/files/{fileId}` | body `{appProperties:{isTillzoPosSheet:"true", shopName, createdByApp:"TillzoPOS", version:"1"}}` | tag sheet as POS sheet |
| E10 | `POST https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,mimeType,webViewLink` | multipart: metadata part + media part | upload GRN attachment |
| E11 | `POST https://oauth2.googleapis.com/token` | `grant_type=refresh_token` | OAuth refresh |
| E12 | `https://accounts.google.com/o/oauth2/v2/auth` | redirect URI `com.tillzo.pos:/oauth2redirect`, scopes `openid, email, drive.file` | OAuth login (AppAuth) |

Drive queries used (`DriveSearchHelper` + `SheetsRemoteDataSource.searchExistingPosSheets`):
- Tagged: `appProperties has { key='isTillzoPosSheet' and value='true' } and trashed=false`
- By name: `name contains 'Tillzo POS' and mimeType='application/vnd.google-apps.spreadsheet' and trashed=false`
- Folders: `mimeType='application/vnd.google-apps.folder' and trashed=false`
- URL form: `drive/v3/files?q={encoded}&fields=files(id,name,createdTime,modifiedTime,appProperties)&orderBy=modifiedTime+desc&pageSize=20`

OAuth constants (`Constants.kt`):
- `WEB_CLIENT_ID = "191290481305-3m583fdj0hq5je8mnj34frqih33lssqc.apps.googleusercontent.com"`
- `ANDROID_CLIENT_ID = "191290481305-3ag6k2hakgtdjkted28bulmig9eb1eaq.apps.googleusercontent.com"`
- `GOOGLE_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"`
- `OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"` (drive.file ONLY)

## 1.4 TIMEOUTS / RETRIES / BACKOFF

| Area | Value |
|---|---|
| SheetsApiClient OkHttp | connect 30s, read 30s, write 30s |
| OAuthTokenManager OkHttp | defaults (10s each) |
| Token refresh 401 handling | 1 retry per request guarded by `Authorization-Retry` header; new token via `getValidToken()`; null after 1 retry (no loop) |
| `SyncWorker` | periodic 15 min; `setBackoffCriteria(EXPONENTIAL, 5, SECONDS)`; in-code retry `if (runAttemptCount < 4) Result.retry() else Result.failure()` |
| `RestoreWorker` (`initial_restore`) | backoff EXPONENTIAL 10s; `if (runAttemptCount < 3) Result.retry() else Result.failure()` |
| `DeltaSyncManager` poll | `delay(Constants.DELTA_SYNC_INTERVAL_MS)` = 60,000 ms |
| `MICRO_BATCH_WINDOW_MS` | 20,000 ms — **defined in Constants but never used** (see 09) |
| Bluetooth/Network printer | 3 attempts, `delay(1000L * attempt)`; socket `soTimeout = 3000` ms |

## 1.5 SYNC ORCHESTRATION

Workers (`SyncOrchestrator.scheduleAll()`):

| Unique name | Worker | Schedule | Notes |
|---|---|---|---|
| `AUTO_SYNC_WORKER` | SyncWorker | 15 min periodic | `NetworkType.CONNECTED`; `ExistingPeriodicWorkPolicy.KEEP` |
| `manual_sync_override` | SyncWorker | one-time | `ExistingWorkPolicy.REPLACE` (Force Sync) |
| `DAILY_EXPIRY_CHECK` | ExpiryCheckWorker | 1 day | first run next 23:59 |
| `MonthlyShardWorker` | MonthlyShardWorker | 1 day | first run next month 1st 00:01 (min delay 60s) |
| `NightlyBackupWorker` | NightlyBackupWorker | 1 day | first run 23:59 |
| `auto_local_backup` | AutoLocalBackupWorker | 1 day | first run 00:15 |
| `POST_SALE_INSTANT_SYNC` | SyncWorker | one-time after each sale | `ExistingWorkPolicy.REPLACE`, CONNECTED |
| `initial_restore` | RestoreWorker | one-time after sheet selection | REPLACE |

SyncWorker execution order: log cleanup (>48h) → `ensureCoreTables` (registers `Sales, Inventory, KhataEvents, Categories, Product_Units, Vendors, Customers, Expenses, Users, Time_Clock` in sync_log) → `getAllTrackedTables()` → `uploadPendingPurchaseOrders()` → `uploadPendingGRNs()` → `uploadPendingProductBatches()` → `uploadPendingStockAdjustments()` → `uploadPendingTillSessions()` → `uploadPendingWastage()` → per-table `uploadTable(...)` → low-stock log → `verifyAndHideSysDbTab()` → `updateLastUpdatedTimestamp(now)`.

`uploadTable` dispatch: `Sales`→SalesUploadUseCase, `Inventory`→InventoryUpsertUseCase, `KhataEvents`→KhataEventUseCase, `Categories`→CategoryUpsertUseCase, `Product_Units`→ProductUnitUpsertUseCase, `Customers`→CustomerUpsertUseCase, `Expenses`→ExpenseUpsertUseCase, `Vendors`→vendorUpsertUseCase, `Users`/`Users_Permissions`→uploadPendingUsers(), `Time_Clock`→uploadPendingTimeClock(), else silent pass.

PO/GRN upload: items uploaded BEFORE header; dedupe via `getExistingUuids(<tab>, "A")` (column A, skip header); append only new IDs; header appended only if its UUID not already present; success → `poDao.markSynced` / `grnDao.markGrnSynced`. Till_Sessions: new appended, existing updated in place via `updateRowByUuid("Till_Sessions", sessionId, sessionRow)` — row found by matching UUID in column A, write `"$tab!A${rowIdx + 1}"`.

Any failure → `Result.retry()` (exponential 5s→15s→1m→5m); success → `updateLastUpdatedTimestamp` in `Settings!A:B` row `last_updated_timestamp`.

## 1.6 DELTA SYNC (`DeltaSyncManager`)

- Cursor persisted as `delta_cursor` row in `sync_log`, key `delta_cursor`, column `lastSyncedAt`, status `"synced"`.
- `pollOnce()`: `localTimestamp = getLastSyncedAt("delta_cursor") ?: 0L`; `remoteTimestamp = settings.lastUpdatedTimestamp`; skip if `localTimestamp > 0 && remoteTimestamp <= localTimestamp`; else `fetchDelta(lastTimestamp)`; upsert all rows; advance cursor ONLY on full success (`"Delta upsert had failures — cursor NOT advanced, will retry"`).
- Tabs polled every cycle: all `Sales_*` tabs (metadata keys `startsWith("Sales_")`, else current) + `[Inventory, Customers, Khata_Events, Expenses, Returns, Users_Permissions, Categories, Product_Units, Till_Sessions, Vendors, Product_Batches, Purchase_Orders, PO_Items, GRN_Headers, GRN_Items, Wastage_Ledger, Stock_Adjustments, BarcodeGeneralConfigs, BarcodeFieldConfigs]`.
- Read via `readRange("$tab!A:ZZ")`; skip tabs with < 2 rows.
- Header detection: row 0 trimmed+lowercased; timestamp column = first of `updated_at|updatedat|last_updated|timestamp|created_at|createdat|contains("updated")|contains("timestamp")`.
- Row filter: keep row iff `lastTimestamp == 0 || rowTs > lastTimestamp`.
- Conflict rule: Inventory and Sales — insert only if `local == null || local.sync_status != "pending"` (local PENDING wins); all other tabs unconditional `@Insert(onConflict = REPLACE)`.
- Delta-upserted rows always stored with `sync_status = "synced"`.
- Column fallbacks: Inventory `price_per_unit` ← `selling_price` then `price`; `current_stock` ← `stock_qty`; `low_stock_threshold` ← `low_threshold`; unit default `"Pieces"`; Sale `sync_uuid` ← `invoice_id`, `pos_terminal_id` ← `pos_id`, `payment_method` default `"CASH"`, `items_json` default `"[]"`; KhataEvent `system_row_id` ← `event_id`, `event_type` ← `type`; User role default `"CASHIER"`; PO status default `"DRAFT"`, currency `"PKR"`; POItem unit `"PC"`; GRN status `"DRAFT"`, GRN item unit `"PC"`, `inventoryAction="PENDING"`, `lowStockThreshold=5.0`; Vendor `isActive=true` default; Batch `isActive=true`; Wastage unit `"PC"`, reason `"OTHER"`; StockAdjustment type `"SET"`; TillSession status `"CLOSED"`, `posTerminalId="terminal_1"`; ProductUnit camelCase only.

## 1.7 MONTHLY SHARDING (`MonthlyShardWorker`)

- `ROW_LIMIT = 18_000`.
- Current month tab `Sales_MMM_yyyy`; create if missing.
- Archive: previous month `Sales_<prevMonth>` renamed to `ARCH_Sales_<prevMonth>`.
- Overflow: if `getRowCount(currentTab) >= 18_000` → create `Sales_<currentMonth>_OVF`.

## 1.8 RESTORE FLOW (`RestoreWorker`)

- State machine `RestoreState`: `Idle`, `Running(progress: Float, status: String)`, `Success`, `Failed(error: String)`.
- Progress strings: `"Starting cloud restore..."` → `"Fetching cloud data..."` → `"Processing N records..."` → `"Finalizing..."`.
- `fetchDelta(lastTimestamp = 0L)` then upsert; cursor set to now with status `"synced"`.
- Failure → `Failed(e.message ?: "Unknown error")`.
- SheetPicker dialog copy: title `"Restoring cloud database..."` + status + `"This may take up to a minute. Please do not close the app."`; failure dialog `"Restore Failed"` + error + button `"Retry Restore"`.

## 1.9 LOCAL DATABASE (Room, `tillzo_pos_db`, version 31, SQLCipher)

Tables: `sync_log, Users_Permissions, Sales, Inventory, Customers, KhataEvents, Expenses, StockAdjustments, Categories, product_batches, purchase_orders, purchase_order_items, vendors, grn_headers, grn_items, product_units, till_sessions, wastage_log, ItemGtins, app_logs, Time_Clock`.

DAOs (19): `syncLogDao, userDao, saleDao, inventoryDao, customerDao, khataEventDao, expenseDao, stockAdjustmentDao, categoryDao, productBatchDao, purchaseOrderDao, vendorDao, grnDao, productUnitDao, tillSessionDao, timeClockDao, wastageDao, logDao` (+ `BaseDao`).

Entity fields (beyond `BaseEntity`: `system_row_id` PK, `sync_status`, `created_at`, `updated_at`, `pos_terminal_id`, `is_deleted`, `deleted_at: Long?`):
- **Inventory:** `item_name, item_number, category, barcode_id, unit, price_per_unit, current_stock, low_stock_threshold, sku, brand, description, cost_price, tax_percent, batch_number, expiry_date, manufacturing_date, expiry_alert_days, is_damaged_stock, damaged_qty, totalStock, hasBatches, isPinned, pinnedOrder`
- **Sale:** `sync_uuid, cashier_id, timestamp, items_json, subtotal, tax, discount, total, payment_method, cash_amount, card_amount, wallet_amount, udhaar_amount, customer_id?, payment_split_json?, reference_id?`
- **Category:** `category_name, parent_category_id?`
- **Customer:** `name, phone, whatsapp?, email?, address?, loyalty_points, lifetime_spend`
- **Expense:** `category, amount, description, timestamp, logged_by_user_id`
- **KhataEvent:** `customer_id, event_type ("UDHAAR"|"JAMA"), amount, note?, reference_sale_id?`
- **User:** `email, name, role, password_hash?, permissions_json?`
- **TimeClock:** `employee_email, employee_name, event_type ("IN"|"OUT"), timestamp, note?`
- **ProductUnit:** `unitId, unitName, abbreviation, isDeleted, syncStatus, createdAt, updatedAt`
- **Vendor:** `vendorId, name, phone, whatsapp?, email?, address?, city?, creditLimit, isActive, isDeleted, syncStatus, createdAt, updatedAt`
- **PurchaseOrder:** `poId, poNumber, vendorId, vendorName, status, notes?, totalAmount, currency, expectedDeliveryDate?, createdBy, syncStatus, isDeleted, createdAt, updatedAt`
- **PurchaseOrderItem:** `poItemId, poId, productId, productName, sku?, barcodeId?, orderedQty, receivedQty, unitCostPrice, totalCost, unit, syncStatus, createdAt, updatedAt`
- **GrnHeader:** `grnId, grnNumber, poId, vendorId, vendorName, status, notes?, receivedBy, receivedByName, totalAmount, syncStatus, posTerminalId, attachedFileId?, attachedFileUrl?, createdAt, updatedAt`
- **GrnItem:** `grnItemId, grnId, poItemId, productId, productName, barcodeId?, sku?, receivedQty, unitCostPrice, totalCost, unit, batchNumber?, manufacturingDate?, expiryDate?, inventoryAction, isNewProduct, syncStatus, createdAt, updatedAt`
- **ProductBatch:** `batchId, productId, barcodeId?, batchNumber, manufacturingDate?, expiryDate?, stockQty, costPrice, sellingPrice, isActive, isDeleted, deletedAt?, syncStatus, posTerminalId, createdAt, updatedAt`
- **StockAdjustment:** `adjustmentId, productId, adjustmentType, quantityChanged, reason, adjustedBy, syncStatus, posTerminalId, createdAt, updatedAt`
- **Wastage:** `wastageId, productId, productName, batchId?, batchNumber?, quantity, unit, costPrice, totalLoss, reason, notes?, loggedBy, wastageDate, syncStatus, posTerminalId, createdAt, updatedAt`
- **TillSession:** `sessionId, cashierId, cashierName, posTerminalId, openingCash, closingCash, expectedCash, totalCashSales, totalCardSales, totalWalletSales, totalUdhaarSales, totalSalesCount, totalRefunds, netCash, status, notes?, shiftDate, openedAt, closedAt, syncStatus, createdAt, updatedAt`
- **ItemGtin:** `item_id, gtin`
- **AppLog:** `id, tag, level, message, timestamp`

Key DAO behaviors: soft delete = `UPDATE ... SET is_deleted = 1, deleted_at = :ts, sync_status = 'pending'`; pending reads filter `is_deleted = 0`; delete-sync reads `WHERE is_deleted = 1 AND sync_status = 'pending'`. WastageDao soft delete sets `syncStatus = 'deleted'`.

## 1.10 PREFS (EncryptedSharedPreferences)

`tillzo_setup_secure_prefs`: `is_provisioned`=false, `spreadsheet_id`="", `user_email`="", `user_display_name`="", `is_pin_enabled`=true, `grn_folder_id`="", `grn_folder_name`="", `printer_mac`="", `printer_ip`="192.168.1.100", `currency_symbol`="$", `admin_passcode`="", `block_negative_stock`=false, `onboarding_complete`=false, `owner_name`="", `business_name`="", `business_address`="", `business_logo_path`="", `business_phone`="", `business_social`="", `business_website`="", `business_portal`="", `business_app_link`="", `business_folder_id`="", `KEY_TAX_INCLUSIVE`=false, `KEY_LOYALTY_ENABLED`=true, `KEY_LOYALTY_RATE`=1f.

`auth_prefs`: `access_token`, `refresh_token`, `app_pin`.

`tillzo_oauth_prefs`: `access_token`, `refresh_token`, `token_expiry_ms` (`EXPIRY_BUFFER_MS = 5*60*1000`).

`barcode_prefs`: `general_config` (JSON: labelWidth=144, labelHeight=72, titleTextSize=6f, isTitleBold=true, barcodeSize=48f, currencySymbol="Rs", companyName="Tillzo POS", companyLogoPath="", showCompanyName=true, showCompanyLogo=true, 16 X/Y position floats, companyNameSize=5f, companyLogoSize=8f, usePrefix=true, customPrefix="]d2", prefixPosition=0, useSuffix=false, customSuffix="", suffixPosition=0, useSeparator=true), `fields_config` (JSON array; defaults GTIN AI"01" seq0, EXPIRY AI"17" seq1, BATCH AI"10" seq2 FNC1, SN AI"21" seq3, SKU AI"240" seq4 disabled).

`tillzo_update_prefs`: `outdated_first_detected_at` (Long, default -1L).

## 1.11 SECURITY / MISC CONSTANTS

- Root detection: `RootBeer(context).isRooted` OR any of 10 su paths: `/system/app/Superuser.apk`, `/sbin/su`, `/system/bin/su`, `/system/xbin/su`, `/data/local/xbin/su`, `/data/local/bin/su`, `/system/sd/xbin/su`, `/system/bin/failsafe/su`, `/data/local/su`, `/su/bin/su`.
- PIN: key `app_pin` in `auth_prefs`; `verifyPIN` = plain equality; NO retry limit / NO lockout.
- Billing: `MONTHLY_SUB_ID = "com.tillzo.pos.sub.monthly"`; `SubscriptionStatus { LOADING, ACTIVE, EXPIRED, ERROR }`; subscriptions acknowledged, never consumed.
- Manifest permissions: INTERNET, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE (≤28), READ_EXTERNAL_STORAGE (≤32), BILLING, FOREGROUND_SERVICE, FOREGROUND_SERVICE_DATA_SYNC, RECEIVE_BOOT_COMPLETED, BLUETOOTH (≤30), BLUETOOTH_ADMIN (≤30), BLUETOOTH_CONNECT, BLUETOOTH_SCAN, CAMERA, VIBRATE, POST_NOTIFICATIONS. `allowBackup="false"`.
