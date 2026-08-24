#!/usr/bin/env python3
"""Menu may have closed. Reopen & dump full menu with scroll positions to map it."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('state:', texts[:8])

# ensure scanner home
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)
    d.shell('am start -n com.tillzo.pos/.ui.MainActivity'); time.sleep(6)

# open menu
d.click(1003, 254); time.sleep(3)

# dump ALL visible menu items with bounds
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="([A-Z][^"]{2,50})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        print((y1,y2), m.group(1))
