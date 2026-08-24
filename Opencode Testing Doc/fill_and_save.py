#!/usr/bin/env python3
"""Fill remaining fields (stock 18, expiry, batch-if-needed) then Save + verify."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def fields_now():
    els = d(className='android.widget.EditText')
    return [(i, els[i].info.get('text'), els[i].info.get('bounds')['left'], els[i].info.get('bounds')['top']) for i in range(els.count)]

# scroll down to pricing/stock/expiry area
d.swipe(540, 1700, 540, 1200, duration=0.3)
time.sleep(1.5)
print('fields:', [(i, t) for i, t, l, y in fields_now()])

els = d(className='android.widget.EditText')
for i in range(els.count):
    t = els[i].info.get('text')
    if t == '12.0':
        els[i].set_text('18')
        print('stock set 18')
        break
time.sleep(1)

# expiry: already '2027-12-31'? check; if empty field exists near batch section, fill
d.swipe(540, 1700, 540, 1400, duration=0.3)
time.sleep(1.5)
els = d(className='android.widget.EditText')
for i in range(els.count):
    t = els[i].info.get('text')
    if t == '2027-12-31':
        print('expiry present')
        break
else:
    for i in range(els.count):
        if els[i].info.get('text') == '':
            els[i].set_text('2027-12-31')
            print('expiry filled at', i)
            break
    time.sleep(1)

# NOW click Save via description
btn = d(description='Save Product')
if not btn.exists:
    # maybe scrolled past it — it's fixed footer though
    pass
btn.click()
time.sleep(4)
xml = d.dump_hierarchy()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?')
errs = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if 'required' in t.lower() or 'fill' in t.lower()]
print('errors:', errs)
