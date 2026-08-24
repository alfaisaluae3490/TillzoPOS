#!/usr/bin/env python3
"""List shows old items only. CSV import of d10_items may have failed silently OR
items are below. Check: did import banner appear? Search for SKU-D10A directly."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
search = find_bounds(dump(d), r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text D10'); time.sleep(3)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('D10 search results:', texts[:12])
print('found A:', 'AUDIT-CUST-A' in xml, '| B:', 'AUDIT-CUST-B' in xml)
