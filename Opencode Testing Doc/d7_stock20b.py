#!/usr/bin/env python3
"""stock idx5 picked '10.0' (tax) not 19.0! Wrong field edited: tax now '20'.
Fix: set idx5(tax)=10, find 19.0 field (idx6), set to 20. Then save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text')) for i in range(els.count)]
print('before:', fields)

for i, t in fields:
    if t == '20':
        els[i].set_text('10'); print('tax fixed ->', i); break

# now stock: field with 19.0
for i, t in fields:
    if t == '19.0':
        els[i].click(); time.sleep(1)
        d.shell('input keyevent 123')
        for _ in range(12): d.shell('input keyevent KEYCODE_DEL')
        d.shell('input text 20'); time.sleep(1)
        print('stock fixed at', i)
        break

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
texts = [t for t in re.findall(r'text="([^"]{1,40})"', xml) if t.strip()]
print('dialog gone:', 'Edit Product' not in xml)
print('card:', texts[:12] if texts else texts)
