#!/usr/bin/env python3
"""Form fresh. Chips visible: Rent(255,835), Internet missing from dump? Misc at (613,967).
Plan: click Rent chip -> verify selected state changes -> fill fields -> disable IME ->
click Save Expense -> verify list."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# click Rent chip
d.click(255, 835); time.sleep(1.5)

# verify selection
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Rent)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    sel = re.search(r'selected="(\w+)"', n)
    if m and b:
        print('Rent after tap:', b.groups(), 'sel=', sel.group(1) if sel else '?')
        break

# fill amount & desc
els = d(className='android.widget.EditText')
els[0].click(); time.sleep(1)
d.shell('input text 45.75'); time.sleep(0.8)
els[1].click(); time.sleep(1)
d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)

# disable IME (form survives, kb closes)
d.shell('ime disable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
time.sleep(2)

# find & click Save Expense
save = None
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
print('saved:', 'AUDIT-EXP-1' in xml or '45.75' in xml)

# RE-ENABLE IME
d.shell('ime enable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
