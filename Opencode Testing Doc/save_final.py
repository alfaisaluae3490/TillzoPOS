#!/usr/bin/env python3
"""Expiry date picked via calendar! Now: verify stock=18 still, then SAVE. Then sync + sheet check."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

xml = dump()
# expiry value present?
m = re.search(r'text="(Aug 15, 2026|2026-08-15|15/08/2026|August 15, 2026)"', xml)
print('expiry shows:', m.group(1) if m else '?')

# stock check/set
els = d(className='android.widget.EditText')
vals = [(i, els[i].info.get('text')) for i in range(els.count)]
print('fields:', vals)
for i in range(els.count):
    if els[i].info.get('text') == '12.0':
        els[i].set_text('18')
        print('stock 12->18')
        break

btn = d(description='Save Product')
if not btn.exists:
    # scroll to bottom to expose footer? footer is fixed; maybe desc lost - use text
    btn = d(text='Save')
print('save exists:', btn.exists)
btn.click()
time.sleep(5)
xml = dump()
closed = 'Edit Product' not in xml
mm = re.search(r'text="Stock: ([\d.]+)"', xml)
errs = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if 'required' in t.lower()]
print('CLOSED:', closed, '| CARD STOCK:', mm.group(1) if mm else '?', '| errors:', errs)

if closed:
    log = d.shell('logcat -d -t 300 | grep -E "SyncWorker started|InventoryUpsertUseCase"')
    s = log.output if hasattr(log, 'output') else str(log)
    print('sync:', s.strip()[-250:])
