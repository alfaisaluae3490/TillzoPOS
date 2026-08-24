#!/usr/bin/env python3
"""Locate Batch Number label by scrolling; fill it; save."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump_texts():
    xml = d.dump_hierarchy()
    return xml, re.findall(r'text="([^"]{1,60})"', xml)

# Scroll further down to reach Batch & Expiry section
d.swipe(540, 1700, 540, 1100, duration=0.3)
time.sleep(1.5)
xml, texts = dump_texts()
print('TEXTS:', texts[:20])

# find 'Batch Number *' label bounds
label_pos = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Batch Number \*"', n)
    if m:
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            label_pos = tuple(map(int, b.groups()))
print('label:', label_pos)

if label_pos:
    els = d(className='android.widget.EditText')
    target = None
    for i in range(els.count):
        info = els[i].info
        b = info.get('bounds')
        t = info.get('text')
        # EditText below the label (its top within/below label bottom), left aligned
        if b['top'] >= label_pos[3] - 40 and b['left'] < 500:
            print('candidate', i, repr(t), b)
            if t == '' or t == 'B-AUD-01':
                target = i
                break
    if target is not None:
        els[target].set_text('B-AUD-01')
        time.sleep(1)
        print('batch now:', repr(els[target].info.get('text')))

# Save
btn = d(description='Save Product')
btn.click()
time.sleep(4)
xml = d.dump_hierarchy()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?')
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('errors:', errs)
