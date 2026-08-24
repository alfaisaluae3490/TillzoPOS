#!/usr/bin/env python3
"""D8: RETURNS/WASTAGE/KHATA quick cycle. Open menu -> Returns & Refunds -> verify
list opens; Wastage Entry -> check form; Khata via customer ledger. Fast pass."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

# open Returns & Refunds from menu
d.click(1003, 254); time.sleep(3)

ret = None
for _ in range(5):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Returns &amp; Refunds|Returns & Refunds)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            ret = (x1,y1,x2,y2)
    if ret: break
    d.swipe(540, 1900, 540, 1200, duration=0.4); time.sleep(1.2)

print('returns:', ret)
if ret:
    d.click((ret[0]+ret[2])//2,(ret[1]+ret[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('returns screen:', texts[:16])
