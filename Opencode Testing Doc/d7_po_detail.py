#!/usr/bin/env python3
"""PO list open: PO-202608-0004 (Draft) exists. Open it -> check Receive flow (GRN).
Tap on the PO card."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
po = find_bounds(xml, r'PO-202608-0004')
print('po card:', po)
if po:
    d.click((po[0]+po[2])//2,(po[1]+po[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('PO detail:', texts[:20])
