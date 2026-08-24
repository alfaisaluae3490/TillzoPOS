#!/usr/bin/env python3
"""GRN screen: 'Confirm' button at top-right (970-1036, 221-287). The delivery notes
field idx0 is optional. Just click Confirm to receive the full order."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
conf = find_bounds(dump(d), r'^Confirm$')
print('confirm:', conf)
if conf:
    d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(5)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('after confirm:', texts[:16])
