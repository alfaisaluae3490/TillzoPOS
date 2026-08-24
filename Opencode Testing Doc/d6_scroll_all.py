#!/usr/bin/env python3
"""Rent chip sel=false (chips don't use selected attr). Form closed after save click
but expense not visible. Check the FULL list + logcat + maybe it saved with category
default. Scroll list completely."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
found = False
for i in range(6):
    xml = dump(d)
    if '45.75' in xml or 'AUDIT-EXP-1' in xml:
        found = True
        break
    d.swipe(540, 1700, 540, 900, duration=0.4); time.sleep(1.2)

xml = dump(d)
print('FOUND:', found)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print(texts[:24])
