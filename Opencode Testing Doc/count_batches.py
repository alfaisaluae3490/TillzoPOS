#!/usr/bin/env python3
"""SMOKING GUN: TWO batches of Stock 1.0 each (GRN ran TWICE — my double-click at
(1003,254)/(970,221) hit Confirm twice!). Total batches = multiple 1.0 rows.
'View Batches (19.0)' header is stale label; actual = sum of shown batches.
The GRN double-execution explains everything: two confirms created 2 batches.
Also earlier '18.0' reading was mid-state.

ACTIONS:
1. Count total batch rows & qty.
2. Fix data: set product stock to correct value via UI edit (sum).
3. CODE FIX: Confirm button must disable after first tap / guard double-invoke."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
# scroll batch sheet to count all
qtys = []
for _ in range(4):
    xml = dump(d)
    qtys += [t for t in re.findall(r'text="Stock: ([\d.]+)"', xml)]
    d.swipe(540, 1600, 540, 1100, duration=0.35); time.sleep(1)
total = sum(float(q) for q in qtys if q.replace('.','').isdigit())
print('batch qtys seen:', qtys, 'total:', total)
