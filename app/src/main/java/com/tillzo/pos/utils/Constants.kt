package com.tillzo.pos.utils

/**
 * App-wide constants shared across modules.
 * Architecture Law: No business logic here — constants only.
 */
object Constants {

    /**
     * Google OAuth 2.0 Web Client ID.
     * Obtained from Google Cloud Console → APIs & Services → Credentials.
     * Used by: SignInViewModel, OAuthTokenManager.
     */
    const val WEB_CLIENT_ID =
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

    /** Maximum rows per monthly Sales tab before forced early sharding. */
    const val MAX_ROWS_PER_SHARD = 18_000

    /** Hidden DB tab name — never visible to users. */
    const val SYS_DB_TAB_NAME = "SYS_DB_DO_NOT_TOUCH"

    /** WorkManager tag for charging-constrained sync workers. */
    const val WORK_TAG_SYNC_CHARGING = "sync_charging"

    /** WorkManager tag for idle-constrained sync workers. */
    const val WORK_TAG_SYNC_IDLE = "sync_idle"

    /** WorkManager unique work name for monthly sharding. */
    const val WORK_NAME_SHARDING = "monthly_sharding"

    /** WorkManager unique work name for disaster recovery backup. */
    const val WORK_NAME_DISASTER = "disaster_recovery"
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
        "total",
        "payment_method",
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
        "city", "province", "country", "billing_address", "owner_name",
        "bank_account_title", "bank_name", "bank_account_number", "bank_iban",
        "bank_swift_code", "bank_branch", "payment_terms", "preferred_currency",
        "credit_limit", "registration_number", "ntn_number", "cnic_number",
        "trn_number", "trade_license_number", "trade_license_expiry_date",
        "primary_manager_name", "primary_manager_phone", "primary_manager_email",
        "tech_support_name", "tech_support_phone", "tech_support_email",
        "billing_contact_name", "billing_contact_phone", "billing_contact_email",
        "escalation_l1_name", "escalation_l1_phone", "escalation_l1_email",
        "escalation_l2_name", "escalation_l2_phone", "escalation_l2_email",
        "escalation_l3_name", "escalation_l3_phone", "escalation_l3_email",
        "contract_start_date", "contract_expiry_date", "sla_response_times",
        "warranty_terms", "compliance_certificates", "contract_file_id",
        "contract_file_url",
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

    val BARCODE_GENERAL_CONFIGS = listOf(
        "system_row_id", "sync_status", "created_at", "updated_at", "pos_terminal_id", "is_deleted", "deleted_at",
        "labelWidth", "labelHeight", "titleTextSize", "isTitleBold", "barcodeSize", "currencySymbol", "companyName",
        "companyLogoPath", "showCompanyName", "showCompanyLogo", "titleX", "titleY", "priceX", "priceY", "skuX",
        "skuY", "gtinX", "gtinY", "lotX", "lotY", "expX", "expY", "snX", "snY", "barcodeX", "barcodeY",
        "companyNameSize", "companyLogoSize", "companyNameX", "companyNameY", "companyLogoX", "companyLogoY",
        "usePrefix", "customPrefix", "prefixPosition", "useSuffix", "customSuffix", "suffixPosition", "useSeparator"
    )

    val BARCODE_FIELD_CONFIGS = listOf(
        "system_row_id", "sync_status", "created_at", "updated_at", "pos_terminal_id", "is_deleted", "deleted_at",
        "fieldId", "fieldName", "aiCode", "isEnabled", "sequenceOrder", "useFnc1Separator", "customValue"
    )
}
