#!/usr/bin/env python3
"""Still on batches sheet. Press back multiple times to exit to inventory, then
check PO status + fix data. First: how many total GRNs were created? Check sheet
GRN_Headers count via fresh export later. For now escape UI loops."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
for i in range(6):
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,40})"', xml) if t.strip()]
    if 'Search items' in texts or 'Tap to activate' in texts or 'Tap + to add' in texts:
        print('reached list/home at press', i)
        print(texts[:8])
        break
    d.press('back'); time.sleep(1.5)
