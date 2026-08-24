#!/usr/bin/env python3
"""Back-press exited app. Relaunch and redo nav to Manage Categories."""
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

d.shell('am start -n com.tillzo.pos/.ui.MainActivity')
time.sleep(6)

# inventory module
d.click(871, 254); time.sleep(3)
# Add Item
add = find_bounds(dump(), r'Add Item')
print('add:', add)
if add:
    d.click((add[0]+add[2])//2, (add[1]+add[3])//2); time.sleep(3)
# category dropdown
d.click(529, 899); time.sleep(2)
mgmt = find_bounds(dump(), r'Manage Categories')
print('mgmt:', mgmt)
if mgmt:
    d.click((mgmt[0]+mgmt[2])//2, (mgmt[1]+mgmt[3])//2); time.sleep(3)

xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('manager:', texts[:12])

# find AUDIT-CAT-X row & check for edit affordance
ax = find_bounds(xml, r'AUDIT-CAT-X')
print('AUDIT-CAT-X row:', ax)
