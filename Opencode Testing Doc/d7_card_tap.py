#!/usr/bin/env python3
"""Search text still in field (kb hidden but search active). The card row for HERMES:
find its bounds, then tap card body → detail opens with Edit/Delete/Print icons
(we saw earlier: Print QR (761,1007), Delete (893,1007) pattern)."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
card = find_bounds(xml, r'HERMES-PROD-001')
print('card:', card)
if card:
    d.click((card[0]+card[2])//2,(card[1]+card[3])//2); time.sleep(3)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('detail:', texts[:16])
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print('desc:', repr(cd.group(1)), b.groups())
