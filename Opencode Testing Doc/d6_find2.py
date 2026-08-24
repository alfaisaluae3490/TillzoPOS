#!/usr/bin/env python3
"""Dialog gone but expense still not in list top? Scroll list & search 45.75/AUDIT-EXP-1.
Also force sync then check sheet Expenses tab."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
found = False
for _ in range(4):
    xml = dump(d)
    if '45.75' in xml or 'AUDIT-EXP-1' in xml:
        found = True
        break
    d.swipe(540, 1600, 540, 1000, duration=0.4); time.sleep(1.2)

print('expense visible:', found)
if found:
    m = re.search(r'text="([^"]*45\.75[^"]*)"', dump(d))
    print('amount text:', m.group(1) if m else '?')
