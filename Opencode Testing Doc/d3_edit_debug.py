#!/usr/bin/env python3
"""Tapping vendor row didn't open edit (only 1 empty EditText = search?). Check screen
and look for edit affordance: maybe tap opens detail sheet or need edit icon per row."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:16])
# clickable descs
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,50})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print('desc:', repr(cd.group(1)), b.groups())
