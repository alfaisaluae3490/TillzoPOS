#!/usr/bin/env python3
"""Edit/Delete icons are in the card Row (right side). They're Icons.Default.Edit/Delete
without contentDescription (null). Find by class+position: right edge of AUDIT-VENDOR-1 card.
Card row y=660-708; icons should be at x>800 same y. Tap there."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
row = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="AUDIT-VENDOR-1"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        row = tuple(map(int,b.groups()))
print('card:', row)

# edit icon ~ right side of card at same vertical center
cy = (row[1]+row[3])//2
# scan right side for clickable icon nodes
for n in re.findall(r'<node[^>]*>', xml):
    cls = re.search(r'class="android.widget.ImageButton|class="android.view.View"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="true"', n)
    if b and clk:
        x1,y1,x2,y2 = map(int,b.groups())
        if y1 <= cy <= y2 and x1 > 600 and (x2-x1) < 120 and (y2-y1) < 120:
            t = re.search(r'text="([^"]*)"', n)
            cd = re.search(r'content-desc="([^"]*)"', n)
            print('icon cand:', (x1,y1,x2,y2), repr(t.group(1) if t else ''), repr(cd.group(1) if cd else ''))
