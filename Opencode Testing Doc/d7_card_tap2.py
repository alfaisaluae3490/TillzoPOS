#!/usr/bin/env python3
"""v2 with find_bounds import."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
card = find_bounds(xml, r'HERMES-PROD-001')
print('card:', card)
if card:
    d.click((card[0]+card[2])//2,(card[1]+card[3])//2); time.sleep(3)
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
    print('detail:', texts[:16])
