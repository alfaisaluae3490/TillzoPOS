#!/usr/bin/env python3
"""Check if AUDIT-CAT-X exists locally at all: reopen Manage Categories and look.
If missing -> addCategory failed on ENTER submit (IME issue). If present but not synced ->
sync_status bug. Also try adding again and watch logcat live."""
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

# go to inventory -> Add Item -> category dropdown -> Manage Categories
xml = dump()
if 'Search items' not in xml:
    d.click(871, 254); time.sleep(3)
add = find_bounds(xml, r'Add Item')
if not add:
    add = find_bounds(dump(), r'Add Item')
if add:
    d.click((add[0]+add[2])//2, (add[1]+add[3])//2)
    time.sleep(3)
d.click(529, 899)
time.sleep(2)
mgmt = find_bounds(dump(), r'Manage Categories')
if mgmt:
    d.click((mgmt[0]+mgmt[2])//2, (mgmt[1]+mgmt[3])//2)
    time.sleep(3)

xml = dump()
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('manager now:', texts[:12])
print('AUDIT-CAT-X present locally:', 'AUDIT-CAT-X' in xml)
