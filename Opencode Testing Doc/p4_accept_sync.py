#!/usr/bin/env python3
"""Backup & Sync Consent screen — click 'Accept & Sync' (this is the data-restore consent)."""
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
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

c = find_bounds(dump(), r'Accept & Sync|Accept &amp; Sync')
print('accept btn:', c)
if c:
    d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
    print('clicked Accept & Sync')

# wait for main app + first sync (data restore)
for i in range(12):
    time.sleep(5)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    if 'Search items' in texts or 'LOW STOCK' in texts:
        print(f'MAIN APP after ~{(i+1)*5}s')
        break
    print(i, texts[:4])

xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('final:', texts[:8])
# check restored items
time.sleep(10)  # allow pull sync
xml = dump()
print('ALPHA restored:', 'AUDIT-ITEM-ALPHA' in xml)
print('BETA restored:', 'AUDIT-ITEM-BETA' in xml)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('ALPHA stock shown:', m.group(1) if m else '?')
