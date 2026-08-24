#!/usr/bin/env python3
"""PO detail open: DRAFT, HERMES-PROD-001 ordered 1.0 PC received 0.0.
Flow: Mark as SENT -> then Receive (GRN) -> verify stock increase + sheet sync."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# scroll to find action buttons
btn = find_bounds(dump(d), r'Mark as SENT')
print('mark sent:', btn)
if btn:
    d.click((btn[0]+btn[2])//2,(btn[1]+btn[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('after sent:', texts[:20])
