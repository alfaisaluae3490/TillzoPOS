#!/usr/bin/env python3
"""Card tap only expanded action row. Earlier pattern: Edit/Delete icons appear BELOW
card after expansion (Print QR/Delete seen at y1007). Find 'Edit' text/desc now after
expansion — or icons at right of expanded row."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    m = re.search(r'text="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if b and ((cd and cd.group(1).strip()) or (m and m.group(1).strip())):
        val = (cd.group(1) if cd else '') or (m.group(1) if m else '')
        x1,y1,x2,y2 = map(int,b.groups())
        if 500 < y1 < 1300:
            print(repr(val), (x1,y1,x2,y2))
