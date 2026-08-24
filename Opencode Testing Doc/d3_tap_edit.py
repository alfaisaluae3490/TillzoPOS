#!/usr/bin/env python3
"""Clickable 132px squares at (739-871, 639-771) & (871-1003, 639-771) = edit+delete icons
for AUDIT-VENDOR-1 card (its row text at y660-708 sits inside). Tap first = EDIT."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(805, 705)   # center of first icon (edit)
time.sleep(3)
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:14])
els = d(className='android.widget.EditText')
print('fields:', [(i, els[i].info.get('text')) for i in range(els.count)])
