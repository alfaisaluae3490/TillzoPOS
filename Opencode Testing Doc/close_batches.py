#!/usr/bin/env python3
"""MAJOR BUG #D7-2 CONFIRMED: HERMES-PROD-001 has ~10+ duplicate batches of qty 1.0
(plus 15.0 rows) — GRN Confirm executed MULTIPLE times (double-tap + repeated menu
clicks each triggered a fresh GRN confirm!). Each Confirm creates a NEW batch instead
of incrementing. Root causes:
  A) UI: Confirm button not disabled during in-flight GRN → multiple taps = multiple GRNs
  B) My automation clicked it repeatedly across retries — but REAL users double-tap too.
  C) Product current_stock (19) never reflected batch sums → recalc ran per-confirm but
     sheet shows 19 & app showed 19/18 at times = recalc result overwritten by pulls.

PLAN: 
1) Count batches precisely from sheet export.
2) CODE FIX in ConfirmGrnUseCase / PODetailScreen: disable button + guard by PO status
   transition SENT→RECEIVED before creating GRNs; also make Confirm idempotent.
3) Repair data: delete extra 1.0 batches via UI, set stock to correct value."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
# close batch sheet
d.press('back'); time.sleep(1.5)
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print(texts[:10])
