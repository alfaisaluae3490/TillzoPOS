#!/usr/bin/env python3
"""Expenses screen open. Find Add Expense FAB, create AUDIT-EXP-1 (25.50), save, sync, verify sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# find FAB
fab = None
for n in re.findall(r'<node[^>]*>', dump(d)):
    cd = re.search(r'content-desc="([^"]*[Aa]dd[^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b:
        fab = (cd.group(1), tuple(map(int,b.groups())))
        print('FAB:', fab)
if not fab:
    fab = ('guess', (926, 1862, 992, 1928))

b = fab[1]
d.click((b[0]+b[2])//2,(b[1]+b[3])//2)
time.sleep(3)

xml = d.dump_hierarchy()
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('add expense form:', texts[:14])
els = d(className='android.widget.EditText')
print('fields:', [(i, els[i].info.get('text')) for i in range(els.count)])
