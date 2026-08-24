#!/usr/bin/env python3
"""Tillzo u2 driver v2 — full UPDATE test with state checks."""
import uiautomator2 as u2, time, re, sys

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def nodes(xml):
    return re.findall(r'<node[^>]*>', xml)

def bcenter(n):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    x1, y1, x2, y2 = map(int, b.groups())
    return ((x1 + x2) // 2, (y1 + y2) // 2)

def kb_shown():
    out = d.shell('dumpsys input_method | grep mInputShown')
    s = out.output if hasattr(out, 'output') else str(out)
    return 'mInputShown=true' in s

def focused_val(xml):
    for n in nodes(xml):
        if 'focused="true"' in n and 'EditText' in n:
            m = re.search(r'text="([^"]*)"', n)
            return m.group(1) if m else '?'
    return None

def save_center(xml):
    for n in nodes(xml):
        if re.search(r'text="Save"', n):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            x1, y1, x2, y2 = map(int, b.groups())
            return ((x1 + x2) // 2, (y1 + y2) // 2)
    return None

xml = dump()
in_form = 'Edit Product' in xml

# Step 0: ensure form open
if not in_form:
    c = None
    for n in nodes(xml):
        if 'text="AUDIT-ITEM-ALPHA"' in n:
            c = bcenter(n); break
    if not c:
        print('FATAL: ALPHA not on screen'); sys.exit(1)
    d.click(*c); time.sleep(3)
    xml = dump()
print('S0 form open:', 'Edit Product' in xml)

# Step 1: scroll to stock field
d.swipe(540, 1500, 540, 1000, duration=0.4); time.sleep(2)
xml = dump()

# Step 2: tap stock field (12.0)
tapped = False
for n in nodes(xml):
    if 'EditText' in n and 'text="12.0"' in n:
        cx, cy = bcenter(n)
        d.click(cx, min(cy, 1850))
        tapped = True; break
print('S2 stock tapped:', tapped)
time.sleep(2)
print('S2b keyboard:', kb_shown(), '| focused:', focused_val(dump()))

# Step 3: type 18
d.shell('input keyevent 123')
for _ in range(6):
    d.shell('input keyevent KEYCODE_DEL')
d.shell('input text 18')
time.sleep(1)
xml = dump()
val = focused_val(xml)
print('S3 typed:', val)

# Step 4: hide keyboard via BACK only if shown; verify dialog alive
if kb_shown():
    d.press('back'); time.sleep(2)
xml = dump()
print('S4 dialog alive:', 'Edit Product' in xml, '| kb now:', kb_shown())

# Step 5: Save — try raw click at text center
save = save_center(xml)
print('S5 save at:', save)
if save:
    d.click(*save); time.sleep(4)
xml = dump()
closed = 'Edit Product' not in xml
print('S5 closed after click:', closed)

# Step 6: if still open, try swipe-tap
if not closed:
    save = save_center(dump())
    d.swipe(save[0], save[1], save[0] + 2, save[1] + 2, duration=0.12)
    time.sleep(4)
    xml = dump()
    closed = 'Edit Product' not in xml
    print('S6 closed after swipe-tap:', closed)

# Step 7: verify stock on card / or inside form fields
xml = dump()
m = re.search(r'text="Stock: ([\d.]+)"', xml)
if m:
    print('FINAL card stock:', m.group(1))
else:
    vals = [t for t in re.findall(r'text="([^"]+)"', xml) if re.match(r'^[\d.]+$', t) and len(t) < 7]
    print('FINAL form numeric fields:', vals[:8])
log = d.shell('logcat -d -t 60 | grep -E "SyncWorker started|InventoryUpsert"')
s = log.output if hasattr(log, 'output') else str(log)
print('sync logs:', s.strip()[:200] or 'NONE')
