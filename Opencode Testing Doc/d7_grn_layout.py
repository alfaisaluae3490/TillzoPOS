#!/usr/bin/env python3
"""GRN screen: item HERMES-PROD-001 recv 1.0 PC. Field idx0 = delivery notes or qty.
Find the receive-quantity input, set it, then confirm GRN. Check current layout deeper."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
# find clickable nodes with 'Receive' text and any quantity fields
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Receive Goods|Confirm|Complete|1\.0)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    clk = re.search(r'clickable="true"', n)
    if m and b:
        print(m.group(1), b.groups(), 'clk='+(clk.group(1) if clk else '?'))
    cd = re.search(r'content-desc="([^"]{1,50})"', n)
    b2 = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b2:
        print('desc:', repr(cd.group(1)), b2.groups())
