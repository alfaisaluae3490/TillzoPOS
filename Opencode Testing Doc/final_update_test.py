#!/usr/bin/env python3
"""onClick WORKS! So earlier failures = hidden state (category/batch empty in ViewModel).
Now fill fields IN ORDER using label->field mapping, then save & verify."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

# Form is open with error shown. Plan:
# 1. Select category via dropdown
els = d(className='android.widget.EditText')
for i in range(els.count):
    if els[i].info.get('text') == 'Select Main Category':
        els[i].click()
        break
time.sleep(2)
c = None
for n in re.findall(r'<node[^>]*>', dump()):
    m = re.search(r'text="HERMES-CAT-001"', n)
    if m:
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            x1, y1, x2, y2 = map(int, b.groups())
            c = ((x1+x2)//2, (y1+y2)//2)
if c:
    d.click(*c)
    time.sleep(2)
print('1) category selected')

# 2. scroll to stock & set 18; note batch/expiry fields on the way
found_batch = found_exp = False
stock_set = False
for round_ in range(4):
    d.swipe(540, 1700, 540, 1350, duration=0.3)
    time.sleep(1.2)
    xml = dump()
    els = d(className='android.widget.EditText')
    for i in range(els.count):
        info = els[i].info
        t, b = info.get('text'), info.get('bounds')
        # stock: currently '12.0' left column
        if t == '12.0' and b['left'] < 400 and not stock_set:
            els[i].set_text('18')
            stock_set = True
            print('   stock set at', i)
            break
    # check batch label visible -> fill field below it
    xml = dump()
    lbl = None
    for n in re.findall(r'<node[^>]*>', xml):
        if re.search(r'text="Batch Number \*"', n):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                lbl = tuple(map(int, b.groups()))
    if lbl and not found_batch:
        els = d(className='android.widget.EditText')
        for i in range(els.count):
            info = els[i].info
            b = info.get('bounds')
            if abs(b['top'] - lbl[3]) < 120 and info.get('text') == '':
                els[i].set_text('B-AUD-01')
                found_batch = True
                print('   batch filled at', i)
                break
    # expiry present?
    xml = dump()
    if '2027-12-31' in xml:
        found_exp = True
    if stock_set and found_batch and found_exp:
        break

print('2) stock:', stock_set, '| batch:', found_batch, '| exp seen:', found_exp)

# 3. Save!
btn = d(description='Save Product')
btn.click()
time.sleep(5)
xml = dump()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('3) CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?', '| errors:', errs)

# 4. If closed, trigger sync & verify sheet later
if closed:
    log = d.shell('logcat -d -t 200 | grep -E "SyncWorker started|InventoryUpsert"')
    s = log.output if hasattr(log, 'output') else str(log)
    print('4) sync logs:', s.strip()[:300])
