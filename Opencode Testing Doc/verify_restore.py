#!/usr/bin/env python3
"""Signed in (session persisted). Now: force sync to pull data, then verify inventory restored.
Then clean up orphan AUDIT-CAT-X sheet row via proper add->delete cycle with the FIXED code."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

# open inventory module & check restore
d.click(871, 254); time.sleep(4)
xml = dump()
print('ALPHA:', 'AUDIT-ITEM-ALPHA' in xml)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('stock:', m.group(1) if m else '?')
print('BETA:', 'AUDIT-ITEM-BETA' in xml)
