#!/usr/bin/env python3
"""Only 1 field = this is the EDIT dialog (name only?) or form state odd. Dump full screen."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:20])
# all clickables
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Save|Cancel|Update|Delete)"', n)
    b = re.search(r'bounds="(\[[^]]+\]\[[^]]+\])"', n)
    if m and b: print(m.group(1), b.group(1))
