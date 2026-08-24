#!/usr/bin/env python3
"""Card expanded: Print QR (761,1078) + Delete (893,1078). Edit must be at left of Print
(~630,1078)? Earlier vendor card had icons row. For inventory expanded card, tapping the
CARD opened this expansion; the 'Edit' entry is likely an icon at (629-695,1078)? Probe
clickable nodes in that band."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="true"', n)
    if b and clk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1000 < y1 < 1250:
            t = re.search(r'text="([^"]{0,30})"', n)
            cd = re.search(r'content-desc="([^"]{0,40})"', n)
            print((x1,y1,x2,y2), repr(t.group(1) if t else ''), repr(cd.group(1) if cd else ''))
