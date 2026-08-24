#!/usr/bin/env python3
"""BUG #D3-1: HERMES-VENDOR-001 duplicated in vendors list (appears 2x).
This was supposedly fixed in DEF-31 (ghost vendor cleanup). Regression or sheet dup?
Check: scroll full list, count entries. Then add AUDIT-VENDOR-1 via FAB."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds, click_center

d = connect()

# count duplicates by scrolling
names = []
for _ in range(5):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(HERMES-VENDOR[^"]*|AUDIT[^"]*)"', n)
        if m and m.group(1) not in names:
            names.append(m.group(1))
    d.swipe(540, 1800, 540, 800, duration=0.4)
    time.sleep(1.2)
print('vendor names seen:', names)

# add new vendor via FAB
fab = find_bounds(dump(d), r'Add Vendor')
print('FAB:', fab)
if fab:
    click_center(d, fab)
    time.sleep(3)
    xml = dump(d)
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print('add vendor form:', texts[:16])
