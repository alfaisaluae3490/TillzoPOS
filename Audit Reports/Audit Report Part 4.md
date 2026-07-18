1. Critical Workflows & Bugs: Data Sync Integrity
The hybrid Local-to-Google Cloud architecture is a highly efficient, serverless approach, but relying on Google Sheets/Drive as a backend introduces specific concurrency and rate-limiting bottlenecks that the current implementation does not fully mitigate.

Workflow Trace: Sale Completion to Cloud Sync

UI Trigger: CheckoutScreen.kt captures the cart and triggers the checkout event.

Business Logic: CompleteSaleUseCase.kt validates the sale and deducts local inventory.

Local Persistence: SaleRepositoryImpl.kt writes to Room via SaleDao.kt (Atomic transaction).

Sync Trigger: SyncOrchestrator.kt enqueues SyncWorker.kt via WorkManager.

Cloud Upload: SalesUploadUseCase.kt -> SheetsRemoteDataSource.kt -> SheetsApiService.kt.

Identified Breakages & Bugs:

Google API Rate Limiting (429 Errors): Google Sheets API has strict quotas (typically 60 requests per user per minute). If SyncWorker.kt attempts to upload multiple queued offline sales individually, the API will reject the requests. The local DB marks them as synced (or fails and retries infinitely), leading to silent data fragmentation.

Token Expiration in Background: OAuthTokenManager.kt handles tokens, but if the refresh token expires while the app is in the background (or Doze mode kills the network), the SyncWorker throws an unhandled HttpException.

Schema Fragility: If a column in the destination Google Sheet is accidentally shifted or deleted, the payload mapping in RestApiSyncImpl.kt will silently write data into the wrong columns because Sheets relies on A1 notation ranges rather than strict JSON key-value pairs.

2. Missing Critical Features (Prioritized)
For a single-user, single-store environment, the focus must be on speed and immediate visibility.

Offline State Indicator: The user needs to know at a glance if they are operating out of the local Room DB or if the Google Sync is live. A simple UI toggle/indicator on the HomeScreen.kt is mandatory.

Quick Cash/Tender Buttons: The PaymentDialog.kt flow requires manual entry. Standard POS systems need pre-configured exact change or standard bill denomination buttons (e.g., $10, $20, $50) for fast, one-tap checkouts.

Hardware Diagnostic Ping: Since the app utilizes external hardware (via EscPosPrinter.kt and BarcodeScannerScreen.kt), there needs to be a unified dashboard to test local port APIs, thermal print routing, and scanner connectivity without running a dummy sale.

3. Features to Remove/Simplify (Bloat & Over-engineering)
The current codebase contains enterprise-level modules that are pure bloat for an individual business owner acting as both cashier and inventory manager.

Till & Session Management:

Files: TillOpenScreen.kt, TillViewModel.kt, TillSessionEntity.kt, TillSessionDao.kt.

Action: Completely strip this out. A single user does not need to "Open" or "Close" a till, float tracking, or shift handovers. The app should default to an "Always Open" state bound to the single active user.

User & Role Management:

Files: UserManagementScreen.kt, UserManagementViewModel.kt, UserDao.kt.

Action: Remove role-based access control (RBAC). The authentication flow (SessionGuardUseCase.kt) should authorize the device and default to an Admin state.

Complex Purchase Order (PO) Workflows:

Files: CreatePurchaseOrderScreen.kt, PODetailScreen.kt, PurchaseOrderDao.kt.

Action: Simplify this to a basic "Restock" screen. A single operator doesn't generate formal POs for internal approval; they just receive goods (GrnEntity.kt) and update inventory.

4. Actionable Code Fixes
Fix A: Batching Google Sheets API Requests (Mitigating 429 Errors)
To fix the rate-limiting breakage in SheetsRemoteDataSource.kt, implement a batch update request via JSON payloads rather than single row appends.

Kotlin
// Inside SheetsRemoteDataSource.kt or SyncUploadUseCase.kt

suspend fun batchUploadSales(sales: List<SaleEntity>, spreadsheetId: String) {
    if (sales.isEmpty()) return

    // Construct a single JSON payload for a batchUpdate
    val valueRanges = sales.map { sale ->
        ValueRange().apply {
            range = "Sales!A:Z"
            values = listOf(listOf(sale.id, sale.timestamp, sale.totalAmount, sale.itemsJson))
        }
    }

    val batchUpdateRequest = BatchUpdateValuesRequest().apply {
        valueInputOption = "USER_ENTERED"
        data = valueRanges
    }

    try {
        sheetsApiService.spreadsheets().values()
            .batchUpdate(spreadsheetId, batchUpdateRequest)
            .execute()
        
        // Mark all as synced in local DB only AFTER successful batch
        saleDao.markAsSynced(sales.map { it.id })
    } catch (e: Exception) {
        // Log failure to SyncLogDao for retry via WorkManager
        syncLogDao.insert(SyncLogEntity(error = e.message, timestamp = System.currentTimeMillis()))
    }
}
Fix B: Bypassing Till Management
Bypass the TillOpenScreen completely in the navigation flow to remove unnecessary UX friction for the single user.

Kotlin
// Inside AppNavHost.kt

NavHost(navController = navController, startDestination = "home") {
    // Remove or comment out the "till_open" destination
    /*
    composable("till_open") {
        TillOpenScreen(
            onTillOpened = { navController.navigate("home") }
        )
    }
    */
    
    composable("home") {
        // Initialize an implicit infinite session on ViewModel init
        HomeScreen(
            viewModel = hiltViewModel()
        )
    }
}
Fix C: Robust Barcode Scanner Input
Ensure the barcode scanner input payload is parsed cleanly without blocking the main thread, routing the data instantly to the cart.

Kotlin
// Inside ScannerViewModel.kt

fun processBarcodeScan(rawPayload: String) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            // Extract exact GTIN/SKU from JSON payload or raw string
            val cleanBarcode = rawPayload.trim().replace("\n", "")
            
            val product = inventoryRepository.getProductByBarcode(cleanBarcode)
            if (product != null) {
                withContext(Dispatchers.Main) {
                    cartManager.addItem(product)
                }
            } else {
                // Trigger UI event for "Item Not Found"
                _uiEvents.emit(UiEvent.ShowError("Item $cleanBarcode not found in local DB"))
            }
        } catch (e: Exception) {
             _uiEvents.emit(UiEvent.ShowError("Scan routing failed"))
        }
    }
}