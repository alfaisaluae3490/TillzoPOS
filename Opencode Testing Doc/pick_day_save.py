#!/usr/bin/env python3
"""Date picker is OPEN. Pick day 15, press OK, verify form still there with expiry set, then Save."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def fb(xml, pat):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pat, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

xml = dump()
# day 15 might need month nav; check if '15' exists
day = fb(xml, r'^15$')
print('day15:', day)
if day:
    d.click((day[0]+day[2])//2, (day[1]+day[3])//2)
    time.sleep(1.5)
    ok = fb(dump(), r'^OK$')
    print('ok:', ok)
    if ok:
        d.click((ok[0]+ok[2])//2, (ok[1]+ok[3])//2)
        time.sleep(2)

xml = dump()
print('picker closed:', 'Select date' not in xml)
print('form open:', 'Edit Product' in xml)
m = re.search(r'text="(2026-\d\d-15|15/08/2026|Aug 15, 2026|2027-12-31)"', xml)
print('expiry value:', m.group(1) if m else '?')

# now save
btn = d(description='Save Product')
print('save exists:', btn.exists)
if btn.exists:
    btn.click()
    time.sleep(5)
xml = dump()
closed = 'Edit Product' not in xml
mm = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('CLOSED:', closed, '| CARD STOCK:', mm.group(1) if mm else '?', '| errors:', errs)
