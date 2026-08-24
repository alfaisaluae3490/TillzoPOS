#!/usr/bin/env python3
"""Switch = View (849,1243,992,1375) checked=false. Tap center (920,1309), verify flips."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(920, 1309); time.sleep(2)

xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    chk = re.search(r'checked="(true|false)"', n)
    if b and chk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1200 < y1 < 1400 and x1 > 800:
            print('switch now:', chk.group(1))
