#!/usr/bin/env python3
"""MAP DECODED:
- 'Batch Number *' label -> EditText shows '30' — WRONG! 30 = expiryAlert default. So the
  field AFTER batch label is actually Expiry Alert?? No wait: pairs show nearest label ABOVE.
  Real layout: [batch EditText][mfg picker][expiry picker][expAlert=30].
  The '30' EditText sits below 'Batch Number*' label but ABOVE 'Expiry Alert' label => it IS expAlert.
  The batch field + expiry pickers are ABOVE 'Batch Number*' label? No—label is above its own field.

CONCLUSION from code: batch IS OutlinedTextField (EditText). The empty EditText under
'Batch & Expiry' section header (pair 1) = BATCH field! Fill it, then find mfg/expiry pickers
right of/below, set expiry via picker if empty, then save."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
nodes = re.findall(r'<node[^>]*>', xml)

# locate 'Batch &amp; Expiry' header and 'Batch Number *' label
hdr = lbl = None
for n in nodes:
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    m = re.search(r'text="([^"]{1,60})"', n)
    if not (b and m):
        continue
    t = m.group(1)
    bb = tuple(map(int, b.groups()))
    if t == 'Batch &amp; Expiry':
        hdr = bb
    elif t == 'Batch Number *':
        lbl = bb
print('header:', hdr, '| label:', lbl)

# batch EditText = between header bottom and label top? Actually label sits INSIDE the textfield
# as Material label at top-left of field bounds. The EditText with top near lbl[1] is batch.
els = d(className='android.widget.EditText')
batch_i = None
for i in range(els.count):
    info = els[i].info
    b = info.get('bounds')
    t = info.get('text')
    # candidate: empty, full-width-ish, vertically between header and +250px of label
    if hdr and lbl and b['top'] >= hdr[3] - 10 and b['top'] <= lbl[3] + 260 and t == '':
        print('batch cand:', i, repr(t), b)
        if batch_i is None or b['top'] < els[batch_i].info.get('bounds')['top']:
            batch_i = i

if batch_i is not None:
    els[batch_i].set_text('B-AUD-01')
    time.sleep(1)
    print('batch now:', repr(els[batch_i].info.get('text')))
else:
    print('no empty field found in batch zone')

# Save
btn = d(description='Save Product') if d(description='Save Product').exists else d(text='Save')
btn.click()
time.sleep(5)
xml = d.dump_hierarchy()
closed = 'Edit Product' not in xml
m = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('CLOSED:', closed, '| CARD STOCK:', m.group(1) if m else '?', '| errors:', errs)
