#!/usr/bin/env python3
"""Save button missing from dump entirely. Check if it's BELOW screen (y>2280) or the
dialog has a fixed footer rendered off-view. Dump ALL nodes w/ text incl. off-screen."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Save|Cancel|Active Status|Credit Limit)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        print(m.group(1), b.groups())
