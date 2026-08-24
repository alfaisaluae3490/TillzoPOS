#!/usr/bin/env python3
"""Icons not found at that Y — the card bounds from name text are just the text node.
Card is taller. Icons vertically centered in ~100dp card. Probe right side x=850-950
at several Y positions for clickable nodes."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
nodes = re.findall(r'<node[^>]*>', xml)
for n in nodes:
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="true"', n)
    if b and clk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 600 < x1 < 1058 and 600 < y1 < 900:
            t = re.search(r'text="([^"]{0,30})"', n)
            print((x1,y1,x2,y2), repr(t.group(1) if t else ''))
