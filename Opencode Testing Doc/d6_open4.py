#!/usr/bin/env python3
"""App IS on scanner home. Menu icon (1003,254) tap + Expenses search with proper waits."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# open menu and verify it opened
d.click(1003, 254); time.sleep(3)
xml = dump(d)
opened = 'Advanced Options' in xml or 'Wastage' in xml
print('menu opened:', opened)
if not opened:
    d.click(1003, 254); time.sleep(4)
    xml = dump(d)
    opened = 'Advanced Options' in xml or 'Wastage' in xml
    print('menu retry:', opened)

exp = None
for _ in range(6):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Expenses)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 100:  # visible on screen
                exp = (x1,y1,x2,y2)
    if exp: break
    d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.3)

print('expenses:', exp)
if exp:
    d.click((exp[0]+exp[2])//2,(exp[1]+exp[3])//2)
    time.sleep(4)
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
    print('screen:', texts[:14])
