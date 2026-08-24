#!/usr/bin/env python3
"""D6 EXPENSES test: menu -> Expenses -> Add expense -> save -> sync -> sheet verify."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

xml = dump(d)
if 'Advanced Options' not in xml and 'Expenses' not in xml:
    d.press('back'); time.sleep(2)
    xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

d.click(1003, 254); time.sleep(3)

exp = None
for _ in range(4):
    exp = find_bounds(dump(d), r'^Expenses$')
    if exp: break
    d.swipe(540, 1900, 540, 1200, duration=0.4); time.sleep(1)
print('expenses:', exp)
if exp:
    d.click((exp[0]+exp[2])//2,(exp[1]+exp[3])//2)
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('expenses screen:', texts[:16])
