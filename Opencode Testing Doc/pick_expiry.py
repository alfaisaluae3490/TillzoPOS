#!/usr/bin/env python3
"""Expiry missing! The expiry field is a date-picker View next to batch. Click it and pick a date."""
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

# scroll so Batch&Expiry section visible
lbl = find_bounds(dump(), r'Batch Number \*')
if not lbl:
    d.swipe(540, 1500, 540, 1100, duration=0.3)
    time.sleep(1.5)
    lbl = find_bounds(dump(), r'Batch Number \*')
print('batch label:', lbl)

# expiry picker View: right column at same Y as batch input (batch left col ~166-529, expiry right 551-892)
# from earlier debug: (551,1178,892,1354) was mfg/expiry pair. Find clickable View right of batch field.
xml = dump()
exp_view = None
for n in re.findall(r'<node[^>]*>', xml):
    cls = re.search(r'class="([^"]+)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="(\w+)"', n)
    m = re.search(r'text="([^"]*)"', n)
    if not (cls and b and clk):
        continue
    x1, y1, x2, y2 = map(int, b.groups())
    if lbl and abs(y1 - lbl[3]) < 250 and x1 > 500 and clk.group(1) == 'true':
        exp_view = (x1, y1, x2, y2)
        print('expiry picker candidate:', exp_view, repr(m.group(1) if m else ''))

if exp_view:
    d.click((exp_view[0]+exp_view[2])//2, (exp_view[1]+exp_view[3])//2)
    time.sleep(2.5)
    xml = dump()
    print('date picker open:', 'Select date' in xml or 'date' in xml.lower()[:2000])
    # pick day 15 of current month
    day = find_bounds(xml, r'^15$')
    if day:
        d.click((day[0]+day[2])//2, (day[1]+day[3])//2)
        time.sleep(1.5)
        # confirm OK
        ok = find_bounds(dump(), r'^OK$')
        if ok:
            d.click((ok[0]+ok[2])//2, (ok[1]+ok[3])//2)
            time.sleep(2)
            print('date picked')

# verify expiry text now present
xml = dump()
m = re.search(r'text="(2026-\d\d-15|15/08/2026|2027-12-31)"', xml)
print('expiry value found:', m.group(1) if m else '?')

# SAVE
btn = d(description='Save Product')
btn.click()
time.sleep(5)
xml = dump()
closed = 'Edit Product' not in xml
mm = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('CLOSED:', closed, '| CARD STOCK:', mm.group(1) if mm else '?', '| errors:', errs)
