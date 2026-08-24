#!/usr/bin/env python3
"""No Edit desc. Inventory cards use Print QR/Delete/Edit icons w/o desc? Earlier dump
showed content-desc 'Print QR Code' and 'Delete' on cards. Find those + infer Edit pos.
Dump all descs with bounds now (search filtered to 1 card)."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print(repr(cd.group(1)), b.groups())
