#!/usr/bin/env python3
"""Find Batch Number label with progressive scroll; fill; save; verify."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def find_label():
    xml = d.dump_hierarchy()
    for n in re.findall(r'<node[^>]*>', xml):
        if re.search(r'text="Batch Number \*"', n):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                return tuple(map(int, b.groups()))
    return None

# progressive scroll until label found (max 4 swipes)
label_pos = find_label()
swipes = 0
while not label_pos and swipes < 4:
    d.swipe(540, 1700, 540, 1300, duration=0.3)
    time.sleep(1.2)
    label_pos = find_label()
    swipes += 1

print('after', swipes, 'extra swipes — label at:', label_pos)

if label_pos:
    els = d(className='android.widget.EditText')
    target = None
    for i in range(els.count):
        info = els[i].info
        b = info.get('bounds')
        # EditText overlapping or just below label
        if abs(b['top'] - label_pos[3]) < 120 and b['left'] < 500:
            print('candidate', i, repr(info.get('text')), b)
            target = i
            break
    if target is not None:
        els[target].set_text('B-AUD-01')
        time.sleep(1)
        print('batch:', repr(els[target].info.get('text')))

btn = d(description='Save Product')
print('save exists:', btn.exists)
btn.click()
time.sleep(4)
xml = d.dump_hierarchy()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?')
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('errors:', errs)
