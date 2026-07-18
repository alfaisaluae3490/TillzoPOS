1\. Critical Workflows \& Bugs (Step-by-Step Breakdown)

🚨 Bug 1: Silent Data Loss in Background Sync (CRITICAL)

Location: app/src/main/java/com/tillzo/pos/data/sync/options/worker/SyncWorker.kt

Affected Methods: uploadPendingPurchaseOrders(), uploadPendingGRNs(), uploadPendingProductBatches(), uploadPendingStockAdjustments()

The Flaw: The code calls uploadTableIfNeeded() or sheetsRepository.uploadBatch(), but then unconditionally marks the local records as synced, regardless of whether the network request succeeded, failed, or threw an exception.

Example Trace:

uploadPendingProductBatches() fetches pending batches.

Calls uploadTableIfNeeded("Product\_Batches", rows).

uploadTableIfNeeded calls sheetsRepository.uploadBatch(), which returns a SyncResult, but the return value is ignored.

The very next line executes: pendingBatches.forEach { productBatchDao.markSynced(it.batchId) }.

Impact: If the device loses internet mid-upload, or the Google Sheets API returns a 429 (Quota Exceeded) or 500 error, the local database falsely marks the data as "synced". The data is permanently stranded and will never be uploaded.

🚨 Bug 2: Delayed Local Stock Deduction (UX \& Data Integrity)

Location: CompleteSaleUseCase.kt (no deduction) and SyncWorker.kt (deductStockForSyncedSales)

The Flaw: The app follows a strict "Blind Selling" rule where local stock is only deducted inside SyncWorker after receiving an HTTP 200 OK from the cloud.

Impact: If a cashier makes 10 sales while offline, the local Inventory UI will still show the original, higher stock levels. The cashier has no way of knowing they just sold the last unit of an item. For a single-store POS, the local database must be the source of truth for the UI, even if offline.

⚠️ Bug 3: Inefficient Cloud Reads Burning API Quota

Location: SyncWorker.kt (uploadPendingBarcodeGeneralConfigs, uploadPendingBarcodeFieldConfigs)

The Flaw: To determine if a row needs an UPDATE or APPEND, the code calls sheetsRemoteDataSource.readRange("$tableName!A:ZZ"), pulling the entire sheet into memory to build an idToRowMap.

Impact: Google Sheets API has strict quotas (\~100 requests per 100 seconds). Pulling entire sheets for config tables on every sync cycle will quickly exhaust quotas, causing SyncWorker to fail and retry exponentially, draining the device battery.

2\. Missing Critical Features (Prioritized for Single-Store POS)

Immediate Local Stock Decrement (P0): When a sale is completed, local inventory must decrement immediately. The cloud sync should be treated as a backup/replication mechanism, not the trigger for local business logic.

Local-First Backup \& Export (P0): Relying solely on Google Sheets is a single point of failure. If the user accidentally deletes the sheet or loses Google account access, the business is crippled. A "Export Local DB to JSON/CSV" or "Create Local SQLite Backup" feature is mandatory.

Robust Return/Refund Stock Restoration (P1): While SaleEntity has a reference\_id and CompleteSaleUseCase filters out REFUND\_OF\_ from stock deduction, there is no visible, robust UseCase that adds stock back to the local InventoryDao and ProductBatchDao when a refund is processed.

Hard Stop on Negative Stock (Configurable) (P2): While "Blind Selling" is good for speed, a single-store owner needs a toggle in Settings: "Prevent sale if stock is 0". Currently, the system allows unlimited negative stock locally.

3\. Features to Remove or Simplify (Bloat \& Over-engineering)

ShardingWorker \& DisasterWorker:

Why: Splitting data by month (ShardingWorker) and running midnight disaster backups are enterprise-grade features. For a single till/single user, this is massive overkill. It adds complexity, failure points, and unnecessary API calls.

Action: Remove them. Rely on Google Sheets' native version history for "disaster" recovery, and use a single, simple daily sync for everything.

Syncing Barcode UI Configs to Cloud:

Why: BarcodeGeneralConfigEntity and BarcodeFieldConfigEntity are stored in Room and synced to Sheets.

Action: Move these to Android DataStore (Preferences). A single-user does not need their label printer margin settings synced to a cloud spreadsheet.

Overly Granular SyncLogDao Table Tracking:

Why: Tracking sync state per-table (SyncLogEntity) is useful for multi-tenant systems.

Action: Simplify to a single last\_sync\_timestamp and rely on sync\_status = 'pending' flags on individual entity rows (which you already have).

4\. Actionable Code Fixes

Fix 1: Prevent Silent Data Loss in SyncWorker.kt

Replace the flawed uploadPending\* methods with a pattern that strictly checks SyncResult.Success before marking local data as synced.

kotlin

12345678910111213141516171819202122232425262728293031323334353637383940414243444546

(Apply this exact same if (result is SyncResult.Success) pattern to uploadPendingPurchaseOrders, uploadPendingGRNs, and uploadPendingStockAdjustments)

Fix 2: Immediate Local Stock Deduction in CompleteSaleUseCase.kt

Inject InventoryDao and ProductBatchDao into CompleteSaleUseCase and deduct stock immediately upon local save. This ensures the UI is always accurate, even offline.

kotlin

123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354

Note: If you implement this, remove the deductStockForSyncedSales function from SyncWorker.kt entirely to prevent double-deduction. The cloud sheet can calculate its own stock from the sales ledger, or you can periodically sync the final local stock levels, rather than every individual transaction deduction.

Fix 3: Optimize Barcode Config Sync (Quota Protection)

Instead of reading the entire sheet (A:ZZ), maintain the system\_row\_id to Sheet Row Index mapping locally in a simple DataStore or Room table, or rely on appending new rows and letting a cloud-side Apps Script handle deduplication. For a single user, appending is 99% of use cases.

kotlin

12345678910111213141516171819202122

Summary Recommendation

The codebase is well-structured but suffers from "Enterprise Feature Creep" applied to a SMB/Single-User product.

Immediately patch the silent failure bugs in SyncWorker.

Shift to Local-First: Make local Room DB the absolute source of truth for inventory and sales. Treat Google Sheets purely as an asynchronous backup target, not as a required step for local business logic to complete.

Strip out ShardingWorker, DisasterWorker, and complex cloud-based UI config syncing to stabilize the app and reduce Google API quota exhaustion.

