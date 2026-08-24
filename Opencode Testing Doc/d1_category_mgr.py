#!/usr/bin/env python3
"""D1 continued: category dropdown -> Manage Categories -> add AUDIT-CAT-X -> verify in sheet.
Then use it on a product."""
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
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# category field is focused area — click it to open dropdown
d.click(529, 899)
time.sleep(2)
xml = dump()
mgmt = find_bounds(xml, r'Manage Categories')
print('Manage Categories:', mgmt)
if mgmt:
    d.click((mgmt[0]+mgmt[2])//2, (mgmt[1]+mgmt[3])//2)
    time.sleep(3)

xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('category manager screen:', texts[:12])
