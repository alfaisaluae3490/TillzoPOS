#!/usr/bin/env python3
"""Vendors screen reached! HERMES-VENDOR-001 appears TWICE — possible duplicate bug!
First investigate: check full list + find Add button. Then add AUDIT-VENDOR-1."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('ALL texts:', texts[:30])

# find FAB / add vendor
fab = find_bounds(xml, r'Add Vendor|FloatingAction')
print('add:', fab)

# any clickable with desc containing add
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]*[Aa]dd[^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b:
        print('desc add:', cd.group(1), b.groups())
