#!/usr/bin/env python3
"""Batches: 7x1.0 + 1x15.0 = 22.0 total (duplicate GRN batches confirmed: seven 1.0s!).
Original stock before GRNs was 19 manual + GRN added... messy history.
CORRECT business value: product should be 19 (manual) + 1 (one legit GRN) = 20.
Repair plan: edit product stock via UI to 20, save, sync, verify sheet.
Then the CODE fixes prevent future duplicate GRNs."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
# close batches sheet
d.press('back'); time.sleep(2)

# tap Edit icon on HERMES card — card is expanded in search view; find Edit by position:
xml = dump(d)
card = find_bounds(xml, r'HERMES-PROD-001')
print('card:', card)
