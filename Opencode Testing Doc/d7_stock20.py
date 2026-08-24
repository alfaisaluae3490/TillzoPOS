#!/usr/bin/env python3
"""Edit form open. Set stock=20, save, sync, verify sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# scroll to stock
d.swipe(540, 1700, 540, 900, duration=0.35); time.sleep(1.5)
xml = d.dump_hierarchy()
els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text')) for i in range(els.count)]
print('fields:', fields)

# stock = the 19.0 field (or 18.0/22.0); pick numeric non-cost/sell
stock_i = None
for i, t in fields:
    if t and t.replace('.','').isdigit() and '.' in t and t not in ('10.5','25.0','50.0','100.0'):
        stock_i = i; break
print('stock idx:', stock_i)

if stock_i is not None:
    els[stock_i].click(); time.sleep(1.5)
    d.shell('input keyevent 123')
    for _ in range(12): d.shell('input keyevent KEYCODE_DEL')
    d.shell('input text 20'); time.sleep(1)
    els = d(className='android.widget.EditText')
    print('stock set:', repr(els[stock_i].info.get('text')))

# disable IME then save
d.shell('ime disable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
time.sleep(2)

save = None
for n in re.findall(r'<node[^>]*>', dump(d)):
    m = re.search(r'text="Save"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

# re-enable IME
d.shell('ime enable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')

xml = d.dump(d) if hasattr(d,'dump') else dump(d)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('card stock now:', m.group(1) if m else '?')
