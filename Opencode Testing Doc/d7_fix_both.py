#!/usr/bin/env python3
"""Tax=20.0 at idx5 (my earlier wrong edit persisted as 20.0!). Fix BOTH:
idx5 tax -> 10.0, idx6 stock 19->20. Then IME-disable + Save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
els = d(className='android.widget.EditText')

# fix tax
els[5].click(); time.sleep(1)
d.shell('input keyevent 123')
for _ in range(10): d.shell('input keyevent KEYCODE_DEL')
d.shell('input text 10'); time.sleep(0.8)

# fix stock
els[6].click(); time.sleep(1)
d.shell('input keyevent 123')
for _ in range(12): d.shell('input keyevent KEYCODE_DEL')
d.shell('input text 20'); time.sleep(0.8)

els = d(className='android.widget.EditText')
print('after:', [(i, els[i].info.get('text')) for i in range(els.count)])

# disable IME & save
d.shell('ime disable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
time.sleep(2)

save = None
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

d.shell('ime enable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
xml = dump(d)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('dialog gone:', 'Edit Product' not in xml, '| card stock:', m.group(1) if m else '?')
