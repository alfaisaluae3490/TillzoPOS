package com.tillzo.pos.utils

/**
 * App-wide constants shared across modules.
 * Architecture Law: No business logic here — constants only.
 */
object Constants {

    /**
     * Google OAuth WEB client ID.
     *
     * FIX (2026-08-06): MUST be the WEB client (`default_web_client_id` from
     * google-services.json / strings.xml), NOT the Android client ID.
     *
     * Why: Google Play Services issues the server auth code against the WEB
     * client (the consent URL in logcat shows client_id=3m583... even when the
     * Android ID was passed). Exchanging that code with the Android client ID
     * returns HTTP 400 invalid_grant. Both `requestServerAuthCode()` and the
     * token exchange MUST use this same web client ID.
     *
     * Used by: SignInViewModel (requestServerAuthCode), OAuthTokenManager,
     * AuthRepositoryImpl.
     */
    const val WEB_CLIENT_ID =
        "191290481305-3m583fdj0hq5je8mnj34frqih33lssqc.apps.googleusercontent.com"

    /**
     * Google OAuth ANDROID client ID (SHA-1 verified in Google Cloud Console).
     *
     * FIX (2026-08-06): `requestIdToken()` MUST use the Android client ID —
     * passing the web client ID there fails with DEVELOPER_ERROR (10)
     * "SHA-1 mismatch or Client ID misconfiguration" because Play Services
     * verifies the Android SHA-1 fingerprint for idToken requests.
     *
     * Correct split:
     *   requestIdToken(ANDROID_CLIENT_ID)          ← SHA-1 verified
     *   requestServerAuthCode(WEB_CLIENT_ID, true) ← code bound to web client
     *   token exchange client_id = WEB_CLIENT_ID   ← matches the code
     */
    const val ANDROID_CLIENT_ID =
        "191290481305-3ag6k2hakgtdjkted28bulmig9eb1eaq.apps.googleusercontent.com"

    /**
     * Google OAuth token refresh endpoint.
     * Used by OAuthTokenManager (M2.10) for manual token refresh on 401.
     */
    const val GOOGLE_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"

    /**
     * OAuth scope — drive.file only (blueprint security rule: broader scope never allowed).
     * drive.file: app can only access files it created — user's Sheet is safe.
     */
    const val OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"

    // ── Sync Constants ───────────────────────────────────────────────────────

    /** Micro-batch window: collect sales for 20s, then fire single upload. */
    const val MICRO_BATCH_WINDOW_MS = 20_000L

    /** Delta sync polling interval: check for remote updates every 60s. */
    const val DELTA_SYNC_INTERVAL_MS = 60_000L

    /** Hidden DB tab name — never visible to users. */
    const val SYS_DB_TAB_NAME = "SYS_DB_DO_NOT_TOUCH"

    /** WorkManager tag for charging-constrained sync workers. */
    const val WORK_TAG_SYNC_CHARGING = "sync_charging"

    /** WorkManager tag for idle-constrained sync workers. */
    const val WORK_TAG_SYNC_IDLE = "sync_idle"
}

/**
 * Single Source of Truth for Google Sheet Column Ordering.
 * The order of these lists EXACTLY dictates the order of values written to Sheets.
 */
object SheetColumns {
    val INVENTORY = listOf(
        "system_row_id",
        "barcode_id", 
        "name",
        "sku",
        "category",
        "brand",
        "description",
        "cost_price",
        "selling_price",
        "tax_percent",
        "unit",
        "stock_qty",
        "low_threshold",
        "batch_number",
        "expiry_date",
        "manufacturing_date",
        "expiry_alert_days",
        "is_damaged",
        "damaged_qty",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )

    val SALES = listOf(
        "invoice_id",
        "pos_id",
        "timestamp",
        "items_json",
        "subtotal",
        "tax",
        "discount",
        "total",
        "payment_method",
        "cash_amount",
        "card_amount",
        "wallet_amount",
        "udhaar_amount",
        "customer_id",
        "payment_split_json",
        "reference_id",
        "cashier_id",
        "sync_uuid",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "system_row_id",
        "created_at",
        "updated_at"
    )

    val CUSTOMERS = listOf(
        "system_row_id",
        "name",
        "phone",
        "whatsapp",
        "email",
        "address",
        // FIX (2026-08-06): loyalty program columns
        "loyalty_points",
        "lifetime_spend",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )

    val KHATA_EVENTS = listOf(
        "system_row_id",
        "customer_id",
        "event_type",
        "amount",
        "note",
        "reference_sale_id",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )
    
    val EXPENSES = listOf(
        "system_row_id",
        "category",
        "amount",
        "description",
        "timestamp",
        "logged_by_user_id",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )
    
    val CATEGORIES = listOf(
        "system_row_id",
        "category_name",
        "parent_category_id",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )
    
    val USERS = listOf(
        "system_row_id",
        "email",
        "name",
        "role",
        "password_hash",
        "permissions_json",
        "is_deleted",
        "deleted_at",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )

    val PURCHASE_ORDERS = listOf("po_id", "po_number", "vendor_id", "vendor_name", "status", "notes", "total_amount", "currency", "expected_delivery_date", "created_by", "sync_status", "pos_terminal_id", "created_at", "updated_at")
    val PO_ITEMS = listOf("po_item_id", "po_id", "product_id", "product_name", "sku", "barcode_id", "ordered_qty", "received_qty", "unit_cost_price", "total_cost", "unit", "sync_status", "created_at", "updated_at")
    val GRN_HEADERS = listOf("grn_id", "grn_number", "po_id", "vendor_id", "vendor_name", "status", "notes", "received_by", "total_amount", "sync_status", "pos_terminal_id", "attached_file_id", "attached_file_url", "created_at", "updated_at")
    val GRN_ITEMS = listOf("grn_item_id", "grn_id", "po_item_id", "product_id", "product_name", "barcode_id", "sku", "received_qty", "unit_cost_price", "total_cost", "unit", "batch_number", "manufacturing_date", "expiry_date", "inventory_action", "is_new_item", "sync_status", "created_at", "updated_at")
    val VENDORS = listOf(
        "vendor_id", "name", "phone", "whatsapp", "email", "address",
        "city", "credit_limit",
        "is_active",
        "is_deleted", "sync_status", "created_at", "updated_at"
    )
    val PRODUCT_BATCHES = listOf("batch_id", "product_id", "barcode_id", "batch_number", "manufacturing_date", "expiry_date", "stock_qty", "cost_price", "selling_price", "is_active", "is_deleted", "deleted_at", "sync_status", "pos_terminal_id", "created_at", "updated_at")
    val PRODUCT_UNITS = listOf(
        "unitId",
        "unitName",
        "abbreviation",
        "isDeleted",
        "syncStatus",
        "createdAt",
        "updatedAt"
    )

    val TILL_SESSIONS = listOf(
        "session_id", "cashier_id", "cashier_name", "pos_terminal_id",
        "opening_cash", "closing_cash", "expected_cash", "total_cash_sales",
        "total_card_sales", "total_wallet_sales", "total_udhaar_sales",
        "total_sales_count", "total_refunds", "net_cash", "status",
        "notes", "shift_date", "opened_at", "closed_at", "sync_status",
        "created_at", "updated_at"
    )

    // FIX (2026-08-06): employee time-tracking
    val TIME_CLOCK = listOf(
        "system_row_id", "employee_email", "employee_name", "event_type",
        "timestamp", "note", "pos_terminal_id", "created_at", "updated_at",
        "sync_status"
    )

    val WASTAGE_LEDGER = listOf(
        "wastage_id",
        "product_id",
        "product_name",
        "batch_id",
        "batch_number",
        "quantity",
        "unit",
        "cost_price",
        "total_loss",
        "reason",
        "notes",
        "logged_by",
        "wastage_date",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )

    val STOCK_ADJUSTMENTS = listOf(
        "adjustment_id",
        "product_id",
        "adjustment_type",
        "quantity_changed",
        "reason",
        "adjusted_by",
        "sync_status",
        "pos_terminal_id",
        "created_at",
        "updated_at"
    )

    val RETURNS = listOf(
        "return_id",
        "system_row_id",
        "original_invoice_id",
        "item_id",
        "qty_returned",
        "condition",
        "refund_method",
        "amount",
        "last_updated",
        "sync_status",
        "created_at",
        "updated_at",
        "pos_terminal_id"
    )
}
