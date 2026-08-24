#!/usr/bin/env python3
"""Scroll to batch section, fill REAL batch field, save, verify."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

d.swipe(540, 1700, 540, 1200, duration=0.3)
time.sleep(1.5)
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('TEXTS:', texts[:22])

els = d(className='android.widget.EditText')
fields = [(i, els[i].info.get('text'), els[i].info.get('bounds')['left'], els[i].info.get('bounds')['top']) for i in range(els.count)]
print('FIELDS:')
for f in fields:
    print(' ', f)

# Find the empty field directly below/after 'Batch Number *' label position.
# Locate label bounds:
label_pos = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Batch Number \*"', n)
    if m:
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            label_pos = (int(b.group(1)), int(b.group(2)), int(b.group(3)), int(b.group(4)))
print('batch label at:', label_pos)

if label_pos:
    # find EditText whose top is just below label bottom
    target = None
    for i, t, l, y in fields:
        b = els[i].info.get('bounds')
        if b['top'] >= label_pos[3] - 10 and b['left'] < 400 and t == '':
            target = i
            break
    print('target batch idx:', target)
    if target is not None:
        els[target].set_text('B-AUD-01')
        time.sleep(1)
        print('batch filled:', repr(els[target].info.get('text')))

# Click Save
btn = d(description='Save Product')
btn.click()
time.sleep(4)
xml = d.dump_hierarchy()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?')
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('errors:', errs)
