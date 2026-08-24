#!/usr/bin/env python3
"""Only 1 EditText visible now (name). Scroll down in dialog for phone + save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# scroll dialog content down
d.swipe(540, 1500, 540, 900, duration=0.4)
time.sleep(1.5)

els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text')) for i in range(els.count)]
print('fields:', fields)

# fill phone if empty
for i, t in fields:
    if t == '' or t is None:
        els[i].click(); time.sleep(1)
        d.shell('input text 0509998877'); time.sleep(1)
        break

els = d(className='android.widget.EditText')
print('after phone:', [(i, els[i].info.get('text')) for i in range(els.count)])

# hide kb & save
d.press('back'); time.sleep(1.5)
save = find_bounds(dump(d), r'^Save$')
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2); time.sleep(4)
    xml = dump(d)
    print('vendor saved:', 'AUDIT-VENDOR-1' in xml)
