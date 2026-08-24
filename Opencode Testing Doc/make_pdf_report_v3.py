#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""TillzoPOS QA Final Report v3 — full audit rounds 1-4 (2026-08-23)"""
from fpdf import FPDF

class PDF(FPDF):
    def header(self):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(120, 120, 120)
        self.cell(0, 6, "TillzoPOS QA Execution Report v3 - Jarvis (Hermes Agent)", align="R")
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
pdf.set_title("TillzoPOS QA Final Report v3")
pdf.add_page()

pdf.set_font("Helvetica", "B", 18)
pdf.cell(0, 10, "TillzoPOS QA FINAL REPORT v3", ln=True)
pdf.set_font("Helvetica", "", 10)
pdf.cell(0, 6, "2026-08-23 | Full Audit Rounds 1-4 (CRUD marathon + Reinstall + Defect-fix cycles)", ln=True)
pdf.ln(4)

pdf.section("EXECUTIVE SUMMARY")
pdf.kv("Result:", "AUDIT COMPLETE (Round 4) - 87 defects registered, 30+ FIXED + verified, 0 unmarked. GAPs 4 (2 fixed). Reinstall persistence PASS (8/8). All CRUD flows sheet-verified.")
pdf.kv("Fixes this round (RUN #4):", "DEF-83 (stock restock overwrite - root-caused + sheet-verified), GAP-4 (wastage delete UI), DEF-64 (GTIN lookup), DEF-79 verified closed.")
pdf.kv("Live proof:", "Refund 845ED833 -> UI stock 11.0 == sheet stock_qty 11.0 (xlsx export) + REFUND_OF_845ed833_Restock row (-100) on Sales_Aug_2026. Wastage delete -> Month Loss $200->$50. Force Sync: all tables 200, zero 401.")
pdf.kv("Registry:", "DEF-01..87 + GAP-1..4 - 09_KNOWN_DEFECTS_REGISTRY.md (87 defects, 30+ fixed/verified, new DEF-84/85/86/87).")

pdf.section("WATCHDOG RUN #4 - FIXED THIS RUN (VERIFIED)")
fixes = [
    ("DEF-83", "Returns restock overwrite: ReturnsViewModel summed STALE batch list (fetch before update) -> +1 restock overwritten by old total. Fixed re-fetch-after-update. Sheet timeline proved 1-unit divergence (x3/x4=10.0 -> sale -> x5=9.0 -> x6=10.0). VERIFIED: refund 845ED833 -> UI 11.0 == sheet 11.0 + REFUND_OF_845ed833_Restock row."),
    ("GAP-4", "Wastage delete UI: WastageDao 'deleted' filters (list+totals), ViewModel.deleteWastage (soft-delete, stock untouched, sheet audit intact), WastageLogScreen delete icon + confirm. VERIFIED: THEFT $150 deleted -> Month Loss $200->$50."),
    ("DEF-64", "GTIN lookup: InventoryDao.getItemByGtin (ItemGtins JOIN) + fallback in ScannerViewModel/InlineScannerViewModel/InventoryRepositoryImpl. Build+install verified (scanner UI test limited by virtual camera)."),
    ("DEF-79", "Keystore getEncoded null: source never touches key.encoded (AES-GCM keystore ciphertext) - verified + closed. Residual security item DEF-84 opened."),
]
for d, desc in fixes:
    pdf.bullet(f"{d} [{desc}]")

pdf.section("NEW DEFECTS (RUN #4 DEEP SCAN)")
for d, desc in [
    ("DEF-84", "Existing DBs still on derivable legacy passphrase (tillzo-db-fallback-<hash>) - DEF-74 fix only fresh-install; rotation/migration missing. MEDIUM security."),
    ("DEF-85", "Home screen scroll restores mid-menu on relaunch - POS search hidden on fresh launch. LOW UX."),
    ("DEF-86", "Returns invoice lookup requires full UUID (short ID fails). LOW."),
    ("DEF-87", "SyncWorker 'Unknown table name: delta_cursor' log noise (no handler). LOW."),
]:
    pdf.bullet(f"{d}: {desc}")

pdf.section("PRIOR ROUNDS - KEY FIXED DEFECTS (SAMPLE)")
for d, desc in [
    ("DEF-81", "Stale cached access token -> permanent 401 sync death; authenticator invalidates access token only (Round-3). 10/10 tables 200."),
    ("DEF-82", "Units list FAB overlap - contentPadding bottom 96dp (Round-3)."),
    ("DEF-80", "SchemaGuard hardcoded Sales_1:1 HTTP 400 -> dynamic Sales_[MMM_YYYY] resolution."),
    ("DEF-34", "Sales restore garbage from header-less monthly tabs -> positional fallback."),
    ("DEF-32", "RBAC dead: SessionGuardUseCase zero call sites -> RbacViewModel + nav gates."),
    ("DEF-27", "Sales rows appended 20-col right-shift -> explicit !A1 range anchor."),
    ("DEF-25", "Discount UI/backend mismatch $25 overcharge -> combine() flows (verified $490)."),
    ("DEF-35/36/37/38", "Sync double-append, Sales header deletion, delta poll dead, SchemaGuard 429 repair - all fixed."),
    ("DEF-39..53", "Till change netting, GRN idempotency, hasBatches, discount clamp, PO receivedQty, echo-clobber, refund khata, loyalty sync, terminal-scoped till, Z-report accuracy."),
]:
    pdf.bullet(f"{d}: {desc}")

pdf.section("REINSTALL PERSISTENCE TEST - PASS (RUN #2)")
pdf.bullet("adb uninstall -> install -> same Gmail (yourtutorial3490@gmail.com) login -> auto-connect -> RestoreWorker 43 rows -> 8/8 sales + inventory + customers + khata + vendors + expenses + till sessions restored; Force Sync 0 pending; sheet intact (no garbage pushed).")

pdf.section("KEY OPEN ITEMS (NEXT ROUNDS)")
for d, desc in [
    ("GAP-1", "PrinterSettingsScreen orphaned (Round-2 wired route per v2 report; re-verify in final build)."),
    ("GAP-3", "Returns tab vestigial - negative-sale design functional; tab removal recommended."),
    ("DEF-84", "Passphrase rotation for existing DBs (security)."),
    ("DEF-31", "Vendors sheet duplicate row (sheet-side cleanup, untouched per rules)."),
    ("CLEANUP", "Scattered old-sale rows cols 20-100 (skip-logic ignores; manual sheet cleanup deferred)."),
]:
    pdf.bullet(f"{d}: {desc}")

pdf.section("METHOD & ENVIRONMENT")
pdf.bullet("Every CRUD op: open -> add/update/delete -> Force Sync -> xlsx/CSV export -> openpyxl row-level verify (sheet 14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU).")
pdf.bullet("Device: Pixel_4 emulator Android 10; SQLCipher Room DB (encrypted - direct read blocked; verification via UI + sheet).")
pdf.bullet("Runs 1-4 source fixes: 30+ files patched surgical; no git commit/push; google-services.json/secrets untouched; sheet never deleted.")
pdf.bullet("Emulator notes: uiautomator rapid dumps crash ('already registered') - 5-6s gaps required; home scroll restore (DEF-85) needs scroll-to-top after launch.")

pdf.output("TillzoPOS_QA_FINAL_REPORT_v3.pdf")
print("PDF written: TillzoPOS_QA_FINAL_REPORT_v3.pdf")
