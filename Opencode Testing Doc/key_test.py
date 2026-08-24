#!/usr/bin/env python3
"""KEY TEST: fresh form, click Save immediately (nothing filled) -> does error text appear?
If yes: onClick fires and the earlier 'no-op' was actually validation failing on hidden state.
If no: onClick truly dead."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
c = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="([^"]*)"', n)
    if m and 'SKU-AUD-1' in m.group(1):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            x1, y1, x2, y2 = map(int, b.groups())
            c = ((x1+x2)//2, (y1+y2)//2)
            break
print('card at:', c)
if c:
    d.click(*c)
    time.sleep(3)
    btn = d(description='Save Product')
    print('save exists:', btn.exists)
    btn.click()
    time.sleep(3)
    xml = d.dump_hierarchy()
    errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
    print('ERRORS SHOWN:', errs)
    print('=> onClick', 'FIRES (validation works)' if errs else 'DOES NOT FIRE')
