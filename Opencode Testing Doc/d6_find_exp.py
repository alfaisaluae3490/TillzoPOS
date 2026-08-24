#!/usr/bin/env python3
"""Form closed (back to list) but AUDIT-EXP-1 not visible at top. Maybe sorted by date
(newest first) and our expense IS there but desc shows category name instead.
Scroll list & search for 45.75 amount. Also check logcat for save errors."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
print('45.75 in screen:', '45.75' in xml)

# check all texts containing Rent/Misc groupings
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:20])

# scroll list to find it
for _ in range(3):
    d.swipe(540, 1600, 540, 1000, duration=0.4); time.sleep(1.2)
    xml = dump(d)
    if '45.75' in xml or 'AUDIT' in xml:
        print('FOUND after scroll')
        break

xml = dump(d)
m = re.search(r'text="(Rent|Misc)[^"]*"', xml)
found45 = '45.75' in xml
print('45.75 found:', found45)
