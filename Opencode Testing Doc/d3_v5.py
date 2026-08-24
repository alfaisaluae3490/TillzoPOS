#!/usr/bin/env python3
"""Vendors item visible! Click it and run full vendor CRUD cycle."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# menu already open with Vendors visible
ven = find_bounds(dump(d), r'^Vendors$')
print('vendors:', ven)
if ven:
    d.click((ven[0]+ven[2])//2, (ven[1]+ven[3])//2)
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('vendors screen:', texts[:16])

# find add button (FAB or Add Vendor)
add = find_bounds(xml, r'Add Vendor|^Add$|content.*Add')
print('add btn:', add)
