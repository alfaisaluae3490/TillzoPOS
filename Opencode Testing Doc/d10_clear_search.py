#!/usr/bin/env python3
"""Items not visible — search field filter still has 'HERMES-PROD-001'! Clear it."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
search = find_bounds(dump(d), r'Search items|Search by')
print('search:', search)
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input keyevent 123')
    for _ in range(20): d.shell('input keyevent KEYCODE_DEL')
    d.press('back'); time.sleep(1.5)

xml = dump(d)
a = 'AUDIT-CUST-A' in xml
bb = 'AUDIT-CUST-B' in xml
print('A visible:', a, '| B visible:', bb)
