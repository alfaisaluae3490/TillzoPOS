#!/usr/bin/env python3
"""Add vendor form open. Fill: Name=AUDIT-VENDOR-1, Phone=0509998877, then Save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)

# find EditTexts in order
els = d(className='android.widget.EditText')
count = els.count
print('fields:', count)
mapping = []
for i in range(count):
    info = els[i].info
    b = info.get('bounds')
    mapping.append((i, info.get('text'), b['left'], b['top']))
print(mapping)
