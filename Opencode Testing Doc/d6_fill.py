#!/usr/bin/env python3
"""Expense form: select 'Misc' category chip, amount=45.75, desc=AUDIT-EXP-1, Save Expense."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# tap Misc category chip
misc = find_bounds(dump(d), r'^Misc$')
print('misc:', misc)
if misc:
    d.click((misc[0]+misc[2])//2,(misc[1]+misc[3])//2)
    time.sleep(1.5)

# fill amount + description (fields idx0/idx1 from earlier dump)
els = d(className='android.widget.EditText')
if els.count >= 2:
    els[0].click(); time.sleep(1)
    d.shell('input text 45.75'); time.sleep(0.8)
    els[1].click(); time.sleep(1)
    d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)

# scroll dialog to reveal Save Expense
for _ in range(3):
    d.swipe(540, 1800, 540, 700, duration=0.35); time.sleep(0.9)

save = find_bounds(dump(d), r'Save Expense')
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print('after:', texts[:10])
