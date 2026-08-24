#!/usr/bin/env python3
"""Tap didn't flip switch. Try u2 click on the View node via its bounds using d.click
with exact center; or use set_value-style a11y action. u2: find by bounds not supported;
use click at (920,1309) already tried. Try swipe-tap on it."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.swipe(920, 1300, 921, 1302, duration=0.15); time.sleep(1.5)
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    chk = re.search(r'checked="(true|false)"', n)
    if b and chk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1200 < y1 < 1400 and x1 > 800:
            print('switch:', chk.group(1))
            break
