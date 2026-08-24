import re, io

path = r"C:\Users\Faisal Khan\Desktop\Tillzo\Opencode Testing Doc\09_KNOWN_DEFECTS_REGISTRY.md"
with io.open(path, encoding='utf-8') as f:
    content = f.read()

results = {
    "DEF-01": "`[X]` CONFIRMED 2026-08-21 — ReturnsScreen sends `\"Damaged/Wastage\"`; ReturnsViewModel tests `\"Damaged\".equalsIgnoreCase` — never matches → dead branch (source-verified)",
    "DEF-02": "`[X]` CONFIRMED 2026-08-21 — `currency = \"$\"` literal in CreatePurchaseOrderViewModel L145 (source-verified)",
    "DEF-03": "`[X]` CONFIRMED 2026-08-21 — UI flow verified: SENT ke baad sirf `Receive Goods` action; koi Cancel path nahi; CANCELLED chip unreachable",
    "DEF-05": "`[X]` CONFIRMED 2026-08-21 — CreateGrnViewModel L214-215 `receivedBy=\"admin_user_id\"`, `receivedByName=\"Admin\"` (source-verified)",
    "DEF-06": "`[X]` CONFIRMED 2026-08-21 — MICRO_BATCH_WINDOW_MS defined (Constants L58), ZERO usages in data layer",
    "DEF-07": "`[X]` CONFIRMED 2026-08-21 — AuthRepositoryImpl.verifyPIN plain equality; koi attempt counter/lockout nahi (source-verified)",
    "DEF-08": "`[X]` CONFIRMED 2026-08-21 — BarcodeHelper sirf generateQRCode/autoGenerateBarcodeId; koi EAN/UPC checksum math nahi",
    "DEF-09": "`[X]` FIXED (pre-existing) 2026-08-21 — Time_Clock ab createWorkspace tab list mein hai (SheetsRepository L314) — source-verified",
    "DEF-10": "`[X]` CONFIRMED 2026-08-21 — PO list chips: All/Draft/Sent/Received/Cancelled (UI verified); PARTIALLY_RECEIVED kisi chip se match nahi karta",
    "DEF-11": "`[X]` CONFIRMED 2026-08-21 — ReceiptGenerator: ZERO callers (orphaned); `Rs` hardcoded; `SPLIT` vs `\"Split\"` branch mismatch",
    "DEF-12": "`[X]` CONFIRMED 2026-08-21 — HomeViewModel HomeScreen mein use nahi hota (PosViewModel use hota hai) — placeholder",
    "DEF-13": "`[X]` CONFIRMED 2026-08-21 — KEY_TAX_INCLUSIVE/KEY_LOYALTY_ENABLED camelCase prefs keys (AppSetupPrefs L122-129) — functional lekin naming inconsistent (LOW)",
    "DEF-14": "`[X]` CONFIRMED 2026-08-21 — BillingManager L94-95 USER_CANCELED → `_billingError = \"Purchase was cancelled.\"` (error severity)",
    "DEF-15": "`[X]` CONFIRMED 2026-08-21 — HistoryViewModel pageSize 30 + offset paging (L48-76); in-memory contains filter sirf loaded pages par",
    "DEF-16": "`[X]` CONFIRMED 2026-08-21 — InventoryCrudScreen L223 deleteItem direct — koi confirmation dialog nahi",
    "DEF-17": "`[X]` CONFIRMED 2026-08-21 — VerifyQrViewModel local-only lookup (SaleDao), koi expired state nahi",
    "DEF-18": "`[X]` CONFIRMED 2026-08-21 — ExpenseViewModel L58-60 deductExpenseFromSession try/catch `(_: Exception)` — deduction failure swallowed",
    "DEF-19": "`[X]` CONFIRMED 2026-08-21 — CompleteSaleUseCase L95 payment_split_json = `\"{}\"` jab SPLIT nahi (LOW)",
    "DEF-20": "`[X]` CONFIRMED 2026-08-21 — ConfirmGrnUseCase L46 low_stock_threshold = item.lowStockThreshold; default 5.0 kisi UI field se surface nahi hota",
    "DEF-21": "`[X]` CONFIRMED 2026-08-21 — UpdatePOStatusUseCase L9-10 invoke(poId, status) — koi status whitelist nahi",
    "DEF-22": "`[X]` CONFIRMED 2026-08-21 — AppSetupPrefs currency_symbol default `\"$\"` vs BarcodePrefs default `\"Rs\"` vs SettingsScreen `currencySymbol.ifBlank { \"Rs\" }` (L243) — mismatch LIVE",
    "DEF-23": "`[X]` CONFIRMED 2026-08-21 — EditBatchDialog stockQty edit vs recalculateTotalStock; batch stock consistency risk (LOW-MED)",
    "DEF-24": "`[X]` CONFIRMED 2026-08-21 — SheetPickerViewModel L67 `if (sheets.size == 1)` auto-select — bina user confirmation",
}

updated = 0
for def_id, note in results.items():
    m = re.search(rf"(## {def_id} [^\n]*\n(?:.*\n)*?)- \*\*RESULT:\*\* `\[ \]`[^\n]*\n", content)
    if m:
        block = m.group(0)
        new_block = re.sub(r"- \*\*RESULT:\*\* `\[ \]`[^\n]*\n",
                           f"- **RESULT:** {note}\n", block, count=1)
        content = content.replace(block, new_block)
        updated += 1
    else:
        print(f"NOT FOUND: {def_id}")

with io.open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)

print(f"Updated {updated}/24 DEF entries")
