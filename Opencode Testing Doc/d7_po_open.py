#!/usr/bin/env python3
"""D7 PO/GRN test: open existing Draft PO -> receive it (GRN flow) or verify its sync.
Also check Vendors module had HERMES-VENDOR-001 dup — investigate via sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# check current state & navigate to home first
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

# open menu -> Purchase Orders
d.click(1003, 254); time.sleep(3)

po = None
for _ in range(5):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Purchase Orders)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            po = (x1,y1,x2,y2)
    if po: break
    d.swipe(540, 1900, 540, 1200, duration=0.4); time.sleep(1.2)

print('PO item:', po)
if po:
    d.click((po[0]+po[2])//2,(po[1]+po[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('PO screen:', texts[:18])
