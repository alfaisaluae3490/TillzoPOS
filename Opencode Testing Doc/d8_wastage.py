#!/usr/bin/env python3
"""Returns graceful error works ('No invoice found'). D8a done.
Now Wastage Entry: menu -> Wastage Entry -> add wastage for AUDIT-ITEM-BETA qty 1 -> sync."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.press('back'); time.sleep(2)
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

d.click(1003, 254); time.sleep(3)
w = None
for _ in range(4):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Wastage Entry)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            w = (x1,y1,x2,y2)
    if w: break
    d.swipe(540, 1900, 540, 1300, duration=0.4); time.sleep(1.2)
print('wastage:', w)
if w:
    d.click((w[0]+w[2])//2,(w[1]+w[3])//2); time.sleep(4)
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
    print('wastage screen:', texts[:16])
