#!/usr/bin/env python3
"""D3 Vendors test using tz helper: navigate menu -> Vendors -> add -> verify."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds, click_center, ensure_app, open_menu

d = connect()
xml = ensure_app(d)

# open menu
menu = open_menu(d)
print('menu open:', 'Advanced Options' in menu or 'Force Sync' in menu or 'Wastage' in menu)

# find Vendors with bidirectional scroll
ven = None
for direction in range(8):
    xml = dump(d)
    ven = find_bounds(xml, r'^Vendors$')
    if ven:
        break
    if direction % 2 == 0:
        d.swipe(540, 1700, 540, 1100, duration=0.4)
    else:
        d.swipe(540, 1100, 540, 1700, duration=0.4)
    time.sleep(1)

print('vendors item:', ven)
if ven:
    click_center(d, ven)
    time.sleep(4)
    xml = dump(d)
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print('vendors screen:', texts[:16])
