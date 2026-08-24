#!/usr/bin/env python3
"""Confirm desc at (970-1036,221-287) = top-right icon. Click its center directly."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(1003, 254)
time.sleep(5)
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('after confirm:', texts[:18])
