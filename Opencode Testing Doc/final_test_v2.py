#!/usr/bin/env python3
"""FINAL UPDATE TEST — complete flow with label-anchored field mapping.
Steps: open ALPHA form -> select category -> scroll to Batch&Expiry -> tap batch EditText
(anchored to 'Batch Number *' label) -> type via set_text on the found node -> fill expiry
via its picker View (click + select day 15) or skip if already set -> stock 18 ->
Save via description -> verify card stock + sync."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_text_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# 0. open form
xml = dump()
if 'Edit Product' not in xml:
    c = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and 'SKU-AUD-1' in m.group(1):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                c = ((x1+x2)//2, (y1+y2)//2)
            break
    d.click(*c)
    time.sleep(3)
print('form:', 'Edit Product' in dump())

# 1. category
els = d(className='android.widget.EditText')
for i in range(els.count):
    if els[i].info.get('text') == 'Select Main Category':
        els[i].click()
        break
time.sleep(2)
c = None
for n in re.findall(r'<node[^>]*>', dump()):
    if re.search(r'text="HERMES-CAT-001"', n):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            x1, y1, x2, y2 = map(int, b.groups())
            c = ((x1+x2)//2, (y1+y2)//2)
if c:
    d.click(*c)
time.sleep(2)
print('category done')

# 2. scroll until 'Batch Number *' label visible
lbl = None
for _ in range(5):
    xml = dump()
    lbl = find_text_bounds(xml, r'Batch Number \*')
    if lbl:
        break
    d.swipe(540, 1700, 540, 1250, duration=0.3)
    time.sleep(1.2)
print('batch label:', lbl)

# 3. batch EditText: first EditText with top >= lbl[3]-40 and left<500
target = None
els = d(className='android.widget.EditText')
for i in range(els.count):
    info = els[i].info
    b = info.get('bounds')
    t = info.get('text')
    if b['top'] >= lbl[3] - 60 and b['left'] < 500:
        print('batch candidate', i, repr(t), b['top'])
        target = i
        break
if target is not None:
    els[target].set_text('B-AUD-01')
    time.sleep(1)
    print('batch set:', repr(els[target].info.get('text')))

# 4. expiry: click its picker View? Expiry already has value from earlier session? check text
xml = dump()
has_exp = '2027-12-31' in xml
print('expiry present:', has_exp)

# 5. stock: find '12.0' left col -> 18 (may already be 18)
stock_done = False
for _ in range(3):
    xml = dump()
    b12 = find_text_bounds(xml, r'^12\.0$')
    if not b12:
        # maybe already 18
        if find_text_bounds(xml, r'^18$'):
            stock_done = True
        break
    cx, cy = (b12[0]+b12[2])//2, min((b12[1]+b12[3])//2, 1850)
    d.click(cx, cy)
    time.sleep(1.5)
    els = d(className='android.widget.EditText')
    for i in range(els.count):
        info = els[i].info
        if info.get('text') == '12.0' and info.get('bounds')['left'] < 400:
            els[i].set_text('18')
            stock_done = True
            print('stock set')
            break
    if stock_done:
        break

# 6. SAVE
btn = d(description='Save Product')
btn.click()
time.sleep(5)
xml = dump()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?', '| errors:', errs)

if closed:
    log = d.shell('logcat -d -t 300 | grep -E "SyncWorker started|InventoryUpsertUseCase"')
    s = log.output if hasattr(log, 'output') else str(log)
    print('sync:', s.strip()[-200:])
