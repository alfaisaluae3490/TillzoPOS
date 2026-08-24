#!/usr/bin/env python3
"""PO now SENT. 'Receive Goods' button visible — tap it to start GRN flow."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
rg = find_bounds(dump(d), r'Receive Goods')
print('receive goods:', rg)
if rg:
    d.click((rg[0]+rg[2])//2,(rg[1]+rg[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('GRN screen:', texts[:20])
els = d(className='android.widget.EditText')
print('fields:', [(i, els[i].info.get('text')) for i in range(min(els.count,8))])
