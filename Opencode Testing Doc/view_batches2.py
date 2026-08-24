#!/usr/bin/env python3
"""Open View Batches bottom sheet to inspect batch rows for HERMES-PROD-001."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
vb = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(View Batches[^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        vb = (m.group(1), (x1,y1,x2,y2))
print('view batches:', vb)
if vb:
    b = vb[1]
    d.click((b[0]+b[2])//2,(b[1]+b[3])//2)
    time.sleep(3)
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
    print('batch sheet:', texts[:24])
