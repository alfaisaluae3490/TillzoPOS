#!/usr/bin/env python3
"""D3 VENDOR UPDATE test: open AUDIT-VENDOR-1, edit phone -> 0501112222, save, sync, verify."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# go to vendors (app should still be on home after sync)
xml = dump(d)
if 'Vendors' not in xml or 'Search vendors' not in xml:
    d.click(1003, 254); time.sleep(3)
    ven = None
    for _ in range(4):
        ven = find_bounds(dump(d), r'Vendors')
        if ven: break
        d.swipe(540, 1500, 540, 1000, duration=0.4); time.sleep(1)
    if ven:
        d.click((ven[0]+ven[2])//2,(ven[1]+ven[3])//2); time.sleep(4)

# tap AUDIT-VENDOR-1 row to edit
xml = dump(d)
row = find_bounds(xml, r'AUDIT-VENDOR-1')
print('row:', row)
if row:
    d.click((row[0]+row[2])//2,(row[1]+row[3])//2)
    time.sleep(3)

xml = dump(d)
els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text')) for i in range(els.count)]
print('edit fields:', fields[:6])
