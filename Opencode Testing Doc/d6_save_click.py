#!/usr/bin/env python3
"""Save Expense button visible at (710,1515). Click it & verify list."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(710, 1515)
time.sleep(4)

xml = d.dump_hierarchy()
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:14])
print('AUDIT-EXP-1 saved:', 'AUDIT-EXP-1' in xml)
