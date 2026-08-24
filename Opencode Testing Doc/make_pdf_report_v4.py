#!/usr/bin/env python3
"""TillzoPOS QA FINAL REPORT v4 - fpdf2, clean single-helper version."""
from fpdf import FPDF
import os

OUT = r"C:/Users/Faisal Khan/Desktop/TillzoPOS_QA_FINAL_REPORT_v4.pdf"
pdf = FPDF()
pdf.set_auto_page_break(auto=True, margin=15)
pdf.add_page()
W = pdf.w - pdf.l_margin - pdf.r_margin  # usable width

def H1(t):
    pdf.set_font("Helvetica", "B", 15)
    pdf.multi_cell(W, 9, t)

def H2(t):
    pdf.set_font("Helvetica", "B", 11)
    pdf.multi_cell(W, 7, t)
    pdf.ln(1)

def B(t):
    pdf.set_font("Helvetica", "", 9)
    pdf.multi_cell(W, 5, t)

H1("TillzoPOS - QA FINAL REPORT v4")
B("Full audit marathon Rounds 1-4 | 2026-08-22/23 | QA: yourtutorial3490@gmail.com (Forex Studio)")
pdf.ln(2)

H2("1. ROUND-3 FIXES (13 total, code-level)")
B("- DEF-64  Barcode 14-digit -> EAN-13 (13-digit + checksum) - retail scannable")
B("- DEF-07  PIN lockout 5 wrong = 30s, 10+ = 5min - brute-force proof")
B("- DEF-08  GTIN validation (digits, 8-14 len, EAN checksum)")
B("- DEF-01  Returns Damaged/Wastage dead branch - UI/VM label match")
B("- DEF-02  PO currency hardcoded $ -> configured currency")
B("- DEF-03  PO Cancel - LIVE: PO-202608-0002 DRAFT -> CANCELLED -> sheet")
B("- DEF-05  GRN received_by -> real user email/name")
B("- DEF-10  PARTIALLY_RECEIVED POs visible in Received filter")
B("- DEF-61  Batch negative stock clamp")
B("- DEF-65  Old-month reprint - scans ALL Sales_* tabs (not just current)")
B("- DEF-66  GTIN collision impossible (9-digit base space)")
B("- DEF-79  Keystore null fix (fresh installs secure)")
B("- DEF-31b Vendor ghost re-import guard")

H2("2. ROUND-4 FIXES + VERIFICATIONS")
B("- GAP-3   Returns sheet tab FIXED - ReturnsEntity+DAO+DBv32+VM+Sync+DeltaSync+Dagger")
B("          LIVE: sale 023a1fcb refund -> Returns tab row 2 (qty 2.0 RESTOCK CASH)")
B("- DEF-46b Double-refund guard LIVE - already-refunded invoice blocked")
B("- DEF-51  Z-Report NET IN DRAWER = session.expectedCash (race fixed)")
B("          LIVE: $1000.00 consistent (was -363.74 garbage)")
B("- DEF-84  Legacy passphrase rotation to Keystore (APK-derivation khatam)")
B("- DEF-62  Sync TOCTOU MITIGATED - single-flight + UUID dedupe")
B("- Settings persistence - Tax-Inclusive ON survives restart")

H2("3. SCATTERED ROWS CLEANUP (2026-08-23 09:30)")
B("- Sales_Aug_2026 scattered rows 8/16/17/18/19 + ghost 21 DELETED via Live Desktop Chrome")
B("- Method: row-header click -> name box N:N verify -> Edit > Delete > Row N -> export verify")
B("- FINAL: scattered rows = [] - 13 sales contiguous rows 2-14, col-A IDs intact")
B("- Data preserved: 0d0ad550, c94acb74, 775425e4, 60ef9e52, 136cfe36, 845ed833,")
B("               47713d33, a0b82b17, 023a1fcb, d3950bea, 1ce4254c, 45849425, 54da16a7")

H2("4. FULL REGRESSION (all PASS)")
B("- Expense flow: add/save/list/sync - Internet $30.75 + Misc $12.99 (sheet rows 12-13)")
B("- PO/GRN flow: create -> item -> save draft -> cancel -> sheet CANCELLED")
B("- Scanner sale D3950BEA $110 (barcode scan end-to-end) - sheet verified")
B("- Force Sync cycle: SyncWorker completed, schema verified")
B("- DB migration v31->v32 PASS, app crash-free across builds")
B("- Refund flow: restock + wastage branches, sheet Returns tab populated")

H2("5. TOTALS")
B("- 60+ files patched, 140+ FIX markers, registry 100+ entries (R1-R4)")
B("- App stable, till open ($1000), data intact, watchdog active")
B("- Carried (low): DEF-79 emulator quirk, consent dialog by-design")

pdf.output(OUT)
print("PDF v4 written:", OUT, os.path.getsize(OUT), "bytes")
