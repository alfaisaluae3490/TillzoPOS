#!/usr/bin/env python3
"""Expense STILL not saved. The Save Expense click at (710,1515) with IME disabled —
did it register? Check logcat around that time for ANY tillzo activity. Also check if
the form validation requires category selection (Rent chip may not have been selected —
chips might need exact tap). Deep debug: reopen form, select chip, VERIFY selection state."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# open Add Expense
fab = (926, 1862, 992, 1928)
d.click((fab[0]+fab[2])//2,(fab[1]+fab[3])//2); time.sleep(3)

xml = dump(d)

# find ALL category chips and their bounds + selected state
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Rent|Internet|Wages|Maintenance|Misc|Stationery)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    sel = re.search(r'selected="(\w+)"', n)
    chk = re.search(r'checkable="(\w+)"[^>]*checked="(\w+)"', n)
    if m and b:
        print('chip:', m.group(1), b.groups(), 'sel='+(sel.group(1) if sel else '?'), 'chk='+str(chk.groups() if chk else '?'))

# amount/desc fields
els = d(className='android.widget.EditText')
print('fields:', [(i, els[i].info.get('text')) for i in range(els.count)])
