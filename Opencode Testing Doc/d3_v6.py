#!/usr/bin/env python3
"""Menu is STILL open (Vendors text visible). The find_bounds regex '^Vendors$' failed
because of exact-match vs the node having different text. Use contains match."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
ven = find_bounds(xml, r'Vendors')
print('vendors bounds:', ven)
print('exact text check:')
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Vendors[^"]*)"', n)
    b = re.search(r'bounds="(\[[^]]+\]\[[^]]+\])"', n)
    if m:
        print(repr(m.group(1)), b.group(1) if b else None)
        if b and 'Manage' not in m.group(1):
            x1,y1,x2,y2 = map(int, re.findall(r'\d+', b.group(1)))
            print('clicking Vendors at', (x1,y1,x2,y2))
            d.click((x1+x2)//2,(y1+y2)//2)
            time.sleep(4)
            xml = dump(d)
            texts = re.findall(r'text="([^"]{1,60})"', xml)
            print('vendors screen:', texts[:16])
            break
