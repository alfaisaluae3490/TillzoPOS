#!/usr/bin/env python3
"""D3: VENDORS module test. Open Advanced Menu -> Vendors -> add AUDIT-VENDOR-1 ->
verify list -> sync -> sheet check -> edit -> sync -> delete -> sync."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

# open Advanced Menu
d.click(1003, 254); time.sleep(3)

ven = None
for _ in range(4):
    ven = find_bounds(dump(), r'^Vendors$')
    if ven: break
    d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)
print('vendors menu item:', ven)
if ven:
    d.click((ven[0]+ven[2])//2, (ven[1]+ven[3])//2)
    time.sleep(4)

xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('vendors screen:', texts[:12])
