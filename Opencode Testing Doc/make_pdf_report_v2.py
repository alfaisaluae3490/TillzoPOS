#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""TillzoPOS QA Final Report v2 — full audit round (2026-08-22)"""
from fpdf import FPDF

class PDF(FPDF):
    def header(self):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(120, 120, 120)
        self.cell(0, 6, "TillzoPOS QA Execution Report v2 - Jarvis (Hermes Agent)", align="R")
        self.ln(8)
        self.set_text_color(0, 0, 0)

    def footer(self):
        self.set_y(-12)
        self.set_font("Helvetica", "I", 8)
        self.set_text_color(140, 140, 140)
        self.cell(0, 6, f"Page {self.page_no()}/{{nb}}", align="C")
        self.set_text_color(0, 0, 0)

    def _clean(self, s):
        return (s.replace("\u2014", "-").replace("\u2013", "-")
                 .replace("\u2192", "->").replace("\u2713", "OK")
                 .replace("\u2705", "(OK)").replace("\u2022", "-")
                 .replace("\u00d7", "x").replace("\u2019", "'")
                 .replace("\u201c", '"').replace("\u201d", '"')
                 .replace("\u20ac", "EUR").replace("\u00a3", "GBP"))

    def section(self, title):
        self.set_font("Helvetica", "B", 13)
        self.set_fill_color(24, 39, 66)
        self.set_text_color(255, 255, 255)
        self.cell(0, 9, self._clean(title), fill=True, ln=True)
        self.ln(4)
        self.set_text_color(0, 0, 0)

    def bullet(self, text):
        self.set_font("Helvetica", "", 9)
        self.multi_cell(0, 4.5, self._clean("- " + text))
        self.ln(1)

    def kv(self, k, v):
        self.set_font("Helvetica", "B", 9)
        self.cell(0, 5, self._clean(k))
        self.ln()
        self.set_font("Helvetica", "", 9)
        self.multi_cell(0, 4.5, self._clean(v))
        self.ln(2)


pdf = PDF()
pdf.set_title("TillzoPOS QA Final Report v2")
pdf.add_page()

pdf.set_font("Helvetica", "B", 18)
pdf.cell(0, 10, "TillzoPOS QA FINAL REPORT v2", ln=True)
pdf.set_font("Helvetica", "", 10)
pdf.cell(0, 6, "2026-08-22 | Full Audit Round 2 (Delegation Findings + Reinstall Persistence)", ln=True)
pdf.ln(4)

pdf.section("EXECUTIVE SUMMARY")
pdf.kv("Result:", "REINSTALL PERSISTENCE TEST PASS (100%) - uninstall/reinstall/same Gmail login -> 8/8 sales, inventory, vendors, expenses, customers ALL restored from cloud.")
pdf.kv("Fixes this round:", "24 defects fixed + verified at code level (DEF-30,32,33,38,39,40,41,42,43,44,44b,45,46,47,48,49,50,51,52,53,57,60,74,80) + GAP-1 Printer Settings route wired.")
pdf.kv("Registry:", "DEF-01..80 + GAP-1/2 (98 entries) - 7 HIGH + 27 MED + 9 LOW from subagent deep-scan merged, re-numbered.")
pdf.kv("Live proof:", "Sale 14 INVOICE 023A1FCB $220 CASH -> sheet row 11 (9 sales on sheet). Sync LIVE: 'Remote updates detected', Sales_Aug_2026!1:1 reads. Sales_1:1 HTTP 400 eliminated (DEF-80).")

pdf.section("FIXED THIS ROUND (24) - CODE-LEVEL, VERIFIED")
fixes = [
    ("DEF-30", "SyncWorker single-flight - duplicate uploads impossible ('Another SyncWorker is already running - skipping' verified)"),
    ("DEF-32", "Sales header repair - SchemaGuard dynamic Sales_[MMM_YYYY] tabs + blank sync_uuid skip + deleteCorruptSales (History 8/8 clean)"),
    ("DEF-33", "Settings last_updated_timestamp duplicate + loop-0 skip -> delta pull was DEAD; poll now LIVE (remote=local verified)"),
    ("DEF-38", "SchemaGuard 429/empty-response repair guard (no false header repair on rate-limit)"),
    ("DEF-39", "Cash over-tender change now netted from till (Z-report Expected Cash accurate)"),
    ("DEF-40", "GRN confirm idempotent - double-tap no longer double-increments stock"),
    ("DEF-41", "hasBatches set on GRN/adjustment batches (phantom stock inflation eliminated)"),
    ("DEF-42", "Discount clamp [0, subtotal+tax] - negative/over-discount impossible (clamped -50->0, 300->220 verified)"),
    ("DEF-43", "Batch stock updates now PUT to sheet (was append-only -> updates silently dropped)"),
    ("DEF-44", "PO receivedQty increment + status flip SENT->Received on GRN (UI verified)"),
    ("DEF-44b", "PO/GRN header updates via updateRowByUuid (existing rows now sync)"),
    ("DEF-45", "Echo-clobber protection on 13 delta tables (local PENDING rows never overwritten by stale sheet copy)"),
    ("DEF-46", "Refund now creates Khata JAMA credit for customer-linked sales"),
    ("DEF-47", "Loyalty points/spend now mark sync_status=pending (reaches sheet)"),
    ("DEF-48", "Till session queries terminal-scoped (multi-terminal day-close reconciles correct session)"),
    ("DEF-49", "openTill idempotent guard (no duplicate OPEN sessions)"),
    ("DEF-50", "Z-report no longer silently skips with no open session - explicit error"),
    ("DEF-51", "NET IN DRAWER includes opening cash (was sales-expenses only)"),
    ("DEF-52", "Expense update/delete re-adjusts till drawer (delta applied)"),
    ("DEF-53", "createNewSheet includes Time_Clock tab (was missing -> punches never synced on new workspace)"),
    ("DEF-57", "Time Clock IN/OUT state machine fixed (was stuck on IN after OUT)"),
    ("DEF-60", "Time_Clock tab in workspace provisioning"),
    ("DEF-74", "DbEncryption fallback migration-safe (derivable key fixed; existing DBs keep legacy key to avoid 'file is not a database' crash - observed & fixed)"),
    ("DEF-80", "SchemaGuard hardcoded 'Sales_1:1' -> HTTP 400 every check; now resolves real Sales_Aug_2026 from metadata (log verified)"),
    ("GAP-1", "PrinterSettingsScreen was ORPHANED (no nav route - printing unconfigurable). Now Settings > App Info > Printer Settings: Bluetooth MAC + Wi-Fi IP + test buttons (UI verified)"),
]
for d, desc in fixes:
    pdf.bullet(f"{d} [{desc}]")

pdf.section("GAP-2 (OPEN) - BarcodeScannerScreen unwired")
pdf.bullet("Full-screen barcode scanner screen exists but is not routed from the POS home scanner. MEDIUM - wiring pending.")

pdf.section("REINSTALL PERSISTENCE TEST - PASS")
pdf.bullet("adb uninstall com.tillzo.pos -> install -> camera Allow -> Continue with Google -> Accept & Sync")
pdf.bullet("Restored: 8/8 sales (A0B82B17 $220, 47713D33 $220, 845ED833 $100, 136CFE36 $200, 60EF9E52 $220, 775425E4 $500, C94ACB74 $490, 0D0AD550 $110)")
pdf.bullet("Restored: Inventory 2 (PROD-001 stock 12.0 - GRN +10 applied), Vendors 2, Expenses 10, Customers 1")
pdf.bullet("Data Viewer counts confirm: Sales 8, Inventory 2, Customers 1, Expenses 10")

pdf.section("KEY OPEN DEFECTS (TRIAGED, NEXT ROUND)")
opens = [
    ("DEF-07", "PIN unlimited attempts, no lockout (brute-forceable)"),
    ("DEF-08", "No barcode checksum validation (EAN/UPC)"),
    ("DEF-31", "Vendors sheet duplicate row 3 (app-route cleanup pending)"),
    ("DEF-79", "Keystore getEncoded null on hardware-backed keys (Android 10 emulator) - fallback works, data safe"),
    ("DEF-50s", "Scattered old sales in sheet cols 20-100 (col A empty - skip logic ignores them; cleanup pending)"),
]
for d, desc in opens:
    pdf.bullet(f"{d}: {desc}")

pdf.section("METHOD")
pdf.bullet("Every CRUD op: ADD -> Force Sync -> xlsx re-download -> openpyxl row-level verify -> UPDATE -> sync -> verify -> DELETE -> sync -> verify")
pdf.bullet("Canonical verify route: Desktop Chrome (Forex Studio) xlsx export; emulator Chrome disabled (pm disable-user) to stop download-dialog interference")
pdf.bullet("Subagent deep-scan (51 api_calls, 20,585 chars) DEF-30..69 + feature-gap audit merged into registry after re-numbering (manual DEF-30..34 collision resolved)")
pdf.bullet("30 files patched, +744/-96 lines, no git commit/push (per protocol)")

pdf.output("TillzoPOS_QA_FINAL_REPORT_v2.pdf")
print("PDF written: TillzoPOS_QA_FINAL_REPORT_v2.pdf")
