#!/usr/bin/env python3
"""Switch didn't flip (tap at x950 missed — switch may be elsewhere). Find the switch
node bounds near Block Negative Stock label y1261-1313."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    cls = re.search(r'class="([^"]+)"', n)
    chk = re.search(r'checked="(true|false)"', n)
    if b and chk and cls:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1200 < y1 < 1400:
            print((x1,y1,x2,y2), cls.group(1).split('.')[-1], 'checked='+chk.group(1))
