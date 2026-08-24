"""PROD-002 fill v3 — FOCUS-VERIFY before every type. No BACK when keyboard may be closed.
Approach: after each tap, dump → confirm focused field bounds → only then type.
Hide keyboard: tap a NON-EDITABLE area? No — use IME action Done via keyevent 66 (ENTER) on single-line fields (safe, no dialog close).
"""
import subprocess, re, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump():
    sh('adb shell uiautomator dump /sdcard/ui.xml')
    return subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui.xml'], capture_output=True, text=True).stdout

def tap(x, y, wait=1.3):
    sh(f'adb shell input tap {x} {y}')
    time.sleep(wait)

def type_text(t):
    sh(f'adb shell input text "{t}"')
    time.sleep(0.8)

def fields(xml):
    out = []
    for m in re.finditer(r'<node[^>]*class="android.widget.EditText"[^>]*>', xml):
        n = m.group(0)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        t = re.search(r'text="([^"]*)"', n)
        f = re.search(r'focused="true"', n)
        if b:
            out.append({'y': int(b.group(2)), 'val': t.group(1) if t else '',
                        'x1': int(b.group(1)), 'y1': int(b.group(2)),
                        'x2': int(b.group(3)), 'y2': int(b.group(4)),
                        'cx': (int(b.group(1))+int(b.group(3)))//2,
                        'cy': (int(b.group(2))+int(b.group(4)))//2,
                        'focused': bool(f)})
    return sorted(out, key=lambda x: x['y'])

def focused(xml):
    fs = fields(xml)
    foc = [f for f in fs if f['focused']]
    return foc[0] if foc else None

def tap_type_verify(field, text, label):
    """tap field center → verify focus landed on a field → type → return fresh xml"""
    tap(field['cx'], field['cy'])
    xml = dump()
    f = focused(xml)
    if not f:
        print(f'  !! {label}: no focused field after tap')
        return xml
    if abs(f['cy'] - field['cy']) > 80:
        print(f'  !! {label}: focus drift tap@y={field["cy"]} → focus@y={f["cy"]} (val={f["val"]!r})')
        field = f  # use actual focused field
    # clear
    sh('adb shell input keyevent 123')
    cur = f['val']
    for _ in range(len(cur) + 3):
        sh('adb shell input keyevent 67')
    time.sleep(0.3)
    type_text(text)
    xml = dump()
    vals = [x['val'] for x in fields(xml)]
    print(f'  {"✅" if text in vals else "⚠️"} {label} = {text} (vals={vals})')
    return xml

def main():
    print('=== PROD-002 v3 ===')
    xml = dump()
    if 'Add Product' not in xml:
        tap(959, 1895, 4)
        xml = dump()
    if 'Add Product' not in xml:
        print('!! form not open'); return
    # Name
    fs = fields(xml)
    if not fs:
        print('!! no fields'); return
    xml = tap_type_verify(fs[0], 'HERMES-PROD-002', 'Name')
    # SKU — re-dump fresh
    fs = fields(xml)
    xml = tap_type_verify(fs[1], 'QA-SKU-002', 'SKU')
    # hide keyboard SAFELY: tap on title area (non-edit) — actually ENTER (IME Done) on SKU:
    sh('adb shell input keyevent 66')
    time.sleep(1.2)
    # Category
    xml = dump()
    fs = fields(xml)
    cat = [f for f in fs if 'Select' in f['val']]
    if cat:
        tap(cat[0]['cx'], cat[0]['cy'], 3)
        xml = dump()
        m = re.search(r'text="HERMES-CAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 1.5)
    # Subcategory
    xml = dump()
    m = re.search(r'text="Select Subcategory"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 2.5)
        xml = dump()
        m2 = re.search(r'text="HERMES-SUBCAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m2:
            tap((int(m2.group(1))+int(m2.group(3)))//2, (int(m2.group(2))+int(m2.group(4)))//2, 1.5)
    # scroll
    sh('adb shell input swipe 540 1800 540 1200 400')
    time.sleep(2)
    xml = dump()
    # Cost/Selling/Tax by labels
    def field_below(label, x_side=None):
        m = re.search(r'text="' + label + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if not m: return None
        ly = int(m.group(4))
        fs = fields(xml)
        for f in fs:
            if ly+10 <= f['cy'] <= ly+340:
                if x_side == 'left' and f['cx'] > 540: continue
                if x_side == 'right' and f['cx'] < 540: continue
                return f
        return None
    cost = field_below('Cost Price', 'left')
    sell = field_below('Selling Price', 'right')
    if cost: xml = tap_type_verify(cost, '150', 'Cost')
    else: print('  !! Cost field not found')
    xml = dump()
    sell = field_below('Selling Price', 'right')
    if sell: xml = tap_type_verify(sell, '250', 'Selling')
    else: print('  !! Selling field not found')
    xml = dump()
    tax = field_below('Tax %')
    if tax: xml = tap_type_verify(tax, '0', 'Tax')
    else: print('  !! Tax field not found')
    # scroll for stock
    sh('adb shell input swipe 540 1800 540 1200 400')
    time.sleep(2)
    xml = dump()
    stk = field_below('Current Stock', 'left')
    low = field_below('Low Alert', 'right')
    if stk: xml = tap_type_verify(stk, '5.5', 'Stock')
    else: print('  !! Stock field not found')
    xml = dump()
    low = field_below('Low Alert', 'right')
    if low: xml = tap_type_verify(low, '1', 'LowAlert')
    else: print('  !! LowAlert field not found')
    # final verify
    xml = dump()
    vals = [f['val'] for f in fields(xml)]
    print(f'FINAL VALS: {vals}')
    need = ['HERMES-PROD-002', 'QA-SKU-002', '150', '250', '5.5', '1']
    ok = all(v in vals for v in need)
    print(f'VERIFY: {"PASS ✅" if ok else "FAIL ❌ — " + str([v for v in need if v not in vals])}')

if __name__ == '__main__':
    main()
