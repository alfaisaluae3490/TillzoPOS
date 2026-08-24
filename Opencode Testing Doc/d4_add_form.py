#!/usr/bin/env python3
"""CRM screen open. Find add-customer FAB, add AUDIT-CUSTOMER-1, save, sync, verify."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# find FAB (Add customer icon)
fab = None
for n in re.findall(r'<node[^>]*>', dump(d)):
    cd = re.search(r'content-desc="([^"]*[Aa]dd[^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b:
        fab = (cd.group(1), tuple(map(int,b.groups())))
        print('FAB:', fab)
if not fab:
    # try known FAB position
    fab = ('guess', (926, 1862, 992, 1928))
    print('using guess pos')

import re as _re
b = fab[1]
d.click((b[0]+b[2])//2,(b[1]+b[3])//2)
time.sleep(3)

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('form:', texts[:14])
els = d(className='android.widget.EditText')
print('fields:', [(i, els[i].info.get('text')) for i in range(els.count)])
