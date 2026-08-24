#!/usr/bin/env python3
"""Form was already closed (fields list showed only 1 = search box). Stock still 19.
Clean retry with careful state checks. This time: open edit form, verify >3 fields,
then act. If form not open, reopen via card tap."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

def open_edit():
    # card name text center
    d.click(288, 962); time.sleep(3)
    xml = dump(d)
    ok = 'Edit Product' in xml
    if not ok:
        d.click(288, 962); time.sleep(3)
        xml = dump(d)
        ok = 'Edit Product' in xml
    return ok

opened = open_edit()
print('edit opened:', opened)

# scroll to stock area
d.swipe(540, 1700, 540, 900, duration=0.35); time.sleep(1.5)
els = d(className='android.widget.EditText')
n = els.count
fields = []
for i in range(n):
    b = els[i].info.get('bounds')
    fields.append((i, els[i].info.get('text'), b['left'], b['top']))
print('form fields:', fields)

# identify: cost=10.5 left, sell=25/100 right pair; stock=19.0 left below them
stock_i = None
for i,t,l,y in fields:
    if t in ('19.0','18.0','22.0') and l < 500 and y > 1000:
        stock_i = i; break
print('stock idx:', stock_i)
