#!/usr/bin/env python3
"""VendorCard has edit/delete IconButtons. They may only be visible when card expanded.
Tap the AUDIT-VENDOR-1 card first to expand, then find Edit icon."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
row = find_bounds(xml, r'AUDIT-VENDOR-1')
print('row:', row)
if row:
    # tap the card body (left side, on name area)
    d.click(row[0]+60, row[1]+20)
    time.sleep(2.5)
    xml = dump(d)
    # look for edit icons
    for n in re.findall(r'<node[^>]*>', xml):
        cd = re.search(r'content-desc="([^"]*[Ee]dit[^"]*)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if cd and b:
            print('EDIT ICON:', cd.group(1), b.groups())
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('texts:', texts[:14])
