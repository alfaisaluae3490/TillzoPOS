#!/usr/bin/env python3
"""Save found at (893,1875)! Click it now."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(893, 1875)
time.sleep(4)
xml = dump(d)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after save:', texts[:12])
print('vendor saved:', 'AUDIT-VENDOR-1' in xml and 'No vendors yet' not in xml)
