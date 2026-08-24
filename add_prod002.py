"""TillzoPOS Add Product — deterministic, keyboard-safe fill (PROD-002 KG)"""
import subprocess, re, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump():
    sh('adb shell uiautomator dump /sdcard/ui.xml')
    return subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui.xml'], capture_output=True, text=True).stdout

def tap(x, y, wait=1.2):
    sh(f'adb shell input tap {x} {y}')
    time.sleep(wait)

def type_text(t):
    sh(f'adb shell input text "{t}"')
    time.sleep(0.8)

def hide_kb():
    """Hide keyboard: BACK key hides keyboard ONLY when open (never closes dialog).
    FIX: ESC (111) closes the dialog — never use it."""
    sh('adb shell input keyevent 4')  # BACK = keyboard hide when keyboard is up
    time.sleep(1.2)

def fields(xml):
    out = []
    for m in re.finditer(r'<node[^>]*class="android.widget.EditText"[^>]*>', xml):
        n = m.group(0)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        t = re.search(r'text="([^"]*)"', n)
        f = re.search(r'focused="true"', n)
        if b:
            out.append({'y': int(b.group(2)), 'val': t.group(1) if t else '',
                        'cx': (int(b.group(1))+int(b.group(3)))//2,
                        'cy': (int(b.group(2))+int(b.group(4)))//2,
                        'focused': bool(f)})
    return sorted(out, key=lambda x: x['y'])

def main():
    print('=== PROD-002 (KG) fill ===')
    xml = dump()
    if 'Add Product' not in xml:
        # tap Add Item FAB
        m = re.search(r'content-desc="Add Item"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 4)
            xml = dump()
    # fill name, sku
    fs = fields(xml)
    tap(fs[0]['cx'], fs[0]['cy'])
    type_text('HERMES-PROD-002')
    hide_kb()
    xml = dump()
    fs = fields(xml)
    tap(fs[1]['cx'], fs[1]['cy'])
    type_text('QA-SKU-002')
    hide_kb()
    # category dropdown (field with val 'Select Main Category')
    xml = dump()
    fs = fields(xml)
    cat = [f for f in fs if 'Select' in f['val'] or f['val'] == 'HERMES-CAT-001']
    if not cat:
        print('!! category field not found'); return
    tap(cat[0]['cx'], cat[0]['cy'], 3)
    xml = dump()
    m = re.search(r'text="HERMES-CAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 1.5)
    # subcategory
    xml = dump()
    m = re.search(r'text="Select Subcategory"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 2.5)
        xml = dump()
        m2 = re.search(r'text="HERMES-SUBCAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m2:
            tap((int(m2.group(1))+int(m2.group(3)))//2, (int(m2.group(2))+int(m2.group(4)))//2, 1.5)
    # scroll down
    sh('adb shell input swipe 540 1800 540 1200 400')
    time.sleep(2)
    xml = dump()
    # find cost/selling by labels
    for lbl, val in [('Cost Price', '150'), ('Selling Price', '250')]:
        m = re.search(r'text="' + lbl + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            ly = int(m.group(4))
            fs = fields(xml)
            cand = [f for f in fs if ly+20 <= f['cy'] <= ly+320 and f['cx'] < 800]
            if cand:
                tap(cand[0]['cx'], cand[0]['cy'])
                type_text(val)
                hide_kb()
                xml = dump()
    # tax (single full-width below cost/selling)
    xml = dump()
    fs = fields(xml)
    # tax = the full-width empty field after the 2-col row
    rows = {}
    for f in fs:
        rows.setdefault(f['y'], []).append(f)
    two_col_ys = [y for y, r in rows.items() if len(r) == 2]
    if two_col_ys:
        y_below = max(two_col_ys)
        fulls = [f for f in fs if f['y'] > y_below + 100 and f['y'] < y_below + 400]
        if fulls:
            tap(fulls[0]['cx'], fulls[0]['cy'])
            type_text('0')
            hide_kb()
            xml = dump()
    # scroll more for stock
    sh('adb shell input swipe 540 1800 540 1200 400')
    time.sleep(2)
    xml = dump()
    fs = fields(xml)
    for f in fs:
        print(f"y={f['y']} val={f['val']!r} cx={f['cx']} cy={f['cy']} focused={f['focused']}")
    # stock/lowalert: find 'Current Stock' & 'Low Alert' labels
    m = re.search(r'text="Current Stock"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    m2 = re.search(r'text="Low Alert"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m and m2:
        ly = int(m.group(4)); ly2 = int(m2.group(4))
        fs = fields(xml)
        stk = [f for f in fs if ly+20 <= f['cy'] <= ly+320 and f['cx'] < 800]
        low = [f for f in fs if ly2+20 <= f['cy'] <= ly2+320 and f['cx'] > 500]
        if stk:
            tap(stk[0]['cx'], stk[0]['cy'])
            type_text('5.5')
            hide_kb()
        if low:
            tap(low[0]['cx'], low[0]['cy'])
            type_text('1')
            hide_kb()
    xml = dump()
    fs = fields(xml)
    print('=== FINAL ===')
    for f in fs:
        print(f"y={f['y']} val={f['val']!r}")
    # verify all values
    vals = [f['val'] for f in fs]
    ok = all(v in vals for v in ['HERMES-PROD-002', 'QA-SKU-002', '150', '250', '5.5', '1'])
    print(f'VERIFY: {"PASS ✅" if ok else "FAIL ❌"} (tax 0 + others: {vals})')

if __name__ == '__main__':
    main()
