#!/usr/bin/env python3
"""Back closed search too. Redo: inventory -> search -> tap card -> Edit icon -> stock 20."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
d.click(871,254); time.sleep(4)
search = find_bounds(dump(d), r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)
    d.press('back'); time.sleep(1)  # kb down, list remains

xml = dump(d)
card = find_bounds(xml, r'HERMES-PROD-001')
print('card:', card)

# find edit/delete icons near card (right side clickable squares like before)
icons = []
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="true"', n)
    if b and clk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 600 < x1 < 1058 and card and abs(y1 - (card[3]-40)) < 160 and (x2-x1)<140:
            icons.append((x1,y1,x2,y2))
print('icons:', icons)
