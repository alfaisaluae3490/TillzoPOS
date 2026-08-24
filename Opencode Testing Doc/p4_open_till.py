#!/usr/bin/env python3
"""Signed in! 'Open Till' prompt = fresh install state. Open the till, then check inventory restore."""
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

# click Open Till
c = find_bounds(dump(), r'Open Till')
if c:
    d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
    print('clicked Open Till')
time.sleep(5)

# maybe a till-opening form appears — dump it
xml = dump()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('after open till:', texts[:14])

# look for a confirm/open button
for pat in [r'^Open$', r'^Confirm$', r'^Start$', r'^Continue$']:
    btn = find_bounds(xml, pat)
    if btn:
        print('found btn:', pat)
        d.click((btn[0]+btn[2])//2, (btn[1]+btn[3])//2)
        time.sleep(4)
        break

# now go to inventory & wait for sync
time.sleep(8)
xml = dump()
inv = find_bounds(xml, r'Inventory')
if inv:
    d.click(871, 254)  # inventory icon
    time.sleep(3)

# wait for cloud pull to restore items
print('waiting for sync...')
for i in range(12):
    time.sleep(5)
    xml = dump()
    if 'AUDIT-ITEM-ALPHA' in xml:
        print(f'ALPHA RESTORED after ~{(i+1)*5}s!')
        break
else:
    print('alpha not visible yet; force sync via menu...')

xml = dump()
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('stock shown:', m.group(1) if m else '?')
print('BETA:', 'AUDIT-ITEM-BETA' in xml)
