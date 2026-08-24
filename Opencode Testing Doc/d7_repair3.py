#!/usr/bin/env python3
"""Icon found (996,705-1080,837) = Edit on card. Click it, set stock 20 in edit form,
save, sync, verify sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(1038, 771)
time.sleep(3)

xml = dump(d)
print('edit form:', 'Edit Product' in xml)

# scroll to stock field
for _ in range(2):
    d.swipe(540, 1700, 540, 900, duration=0.35); time.sleep(1)

xml = dump(d)
els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text')) for i in range(els.count)]
print('fields:', fields)

# find stock field (numeric like 19.0/18.0/22.0 left col) and Current Stock label above
stock_i = None
for i, t in fields:
    if t and t.replace('.','').isdigit() and '.' in t:
        # candidate; confirm via nearby 'Current Stock' label? accept first decimal field
        # after cost/sell pair — take the one whose value != 10.5 and != 25.0
        if t not in ('10.5','25.0','50.0','100.0'):
            stock_i = i
            break
print('stock idx:', stock_i)
if stock_i is None:
    # fallback: any numeric
    for i, t in fields:
        if t and t.replace('.','').isdigit():
            stock_i = i; break

if stock_i is not None:
    els[stock_i].click(); time.sleep(1)
    d.shell('input keyevent 123')
    for _ in range(12): d.shell('input keyevent KEYCODE_DEL')
    d.shell('input text 20'); time.sleep(1)
    els = d(className='android.widget.EditText')
    print('stock now:', repr(els[stock_i].info.get('text')))
