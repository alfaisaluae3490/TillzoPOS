"""PROD-002 v4 — FULL AUTONOMOUS: IME-toggle keyboard hide + focus-verify + dates + save.
Solves: BACK key closes dialog when keyboard closed; tap drift when keyboard shown.
Strategy: NEVER use BACK. Keyboard hide = ime disable+enable (proven safe).
"""
import subprocess, re, time

IME = 'com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME'

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

def hide_kb():
    """IME toggle — hides keyboard, NEVER closes dialog. Proven."""
    sh(f'adb shell ime disable {IME}')
    time.sleep(0.4)
    sh(f'adb shell ime enable {IME}')
    time.sleep(1.0)

def kb_shown():
    out = sh('adb shell dumpsys input_method 2>/dev/null | grep mInputShown | head -1')
    return 'mInputShown=true' in out

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

def focused_field(xml):
    fs = fields(xml)
    foc = [f for f in fs if f['focused']]
    return foc[0] if foc else None

def tap_type(field, text, label, force_hide=True):
    """tap → focus-verify → clear → type → IME-hide. Returns fresh xml."""
    tap(field['cx'], field['cy'])
    xml = dump()
    f = focused_field(xml)
    if not f:
        print(f'  !! {label}: no focus after tap')
        return xml
    if abs(f['cy'] - field['cy']) > 90:
        print(f'  ⚠️ {label}: drift tap@{field["cy"]}→focus@{f["cy"]} — using actual')
        field = f
    # clear
    sh('adb shell input keyevent 123')
    cur = f['val']
    for _ in range(len(cur) + 3):
        sh('adb shell input keyevent 67')
    time.sleep(0.3)
    type_text(text)
    xml = dump()
    vals = [x['val'] for x in fields(xml)]
    ok = text in vals
    print(f'  {"✅" if ok else "❌"} {label} = {text}')
    if force_hide:
        hide_kb()
    return dump()

def scroll_down():
    sh('adb shell input swipe 540 1750 540 1100 400')
    time.sleep(2)
    return dump()

def main():
    print('=== PROD-002 v4 (full auto) ===')
    xml = dump()
    if 'Add Product' not in xml:
        tap(959, 1895, 4)  # Add Item FAB
        xml = dump()
    if 'Add Product' not in xml:
        print('!! form not open'); return
    fs = fields(xml)
    if len(fs) < 2:
        print('!! fields missing'); return
    # 1. Name + SKU (top of form)
    xml = tap_type(fs[0], 'HERMES-PROD-002', 'Name')
    fs = fields(xml)
    xml = tap_type(fs[1], 'QA-SKU-002', 'SKU')
    # 2. Category
    xml = dump()
    fs = fields(xml)
    cat = [f for f in fs if 'Select' in f['val']]
    if cat:
        tap(cat[0]['cx'], cat[0]['cy'], 3)
        xml = dump()
        m = re.search(r'text="HERMES-CAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 1.5)
            print('  ✅ Category = HERMES-CAT-001')
    # 3. Subcategory
    xml = dump()
    m = re.search(r'text="Select Subcategory"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2, 2.5)
        xml = dump()
        m2 = re.search(r'text="HERMES-SUBCAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m2:
            tap((int(m2.group(1))+int(m2.group(3)))//2, (int(m2.group(2))+int(m2.group(4)))//2, 1.5)
            print('  ✅ Subcategory = HERMES-SUBCAT-001')
    # 4. Scroll to pricing
    xml = scroll_down()
    # 5. Cost/Selling/Tax via labels
    def field_below(label, side=None):
        m = re.search(r'text="' + label + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if not m: return None
        ly = int(m.group(4))
        for f in fields(xml):
            if ly+10 <= f['cy'] <= ly+340:
                if side == 'left' and f['cx'] > 540: continue
                if side == 'right' and f['cx'] < 540: continue
                return f
        return None
    for lbl, val, side in [('Cost Price', '150', 'left'), ('Selling Price', '250', 'right'), ('Tax %', '0', None)]:
        f = field_below(lbl, side)
        if f:
            xml = tap_type(f, val, lbl, force_hide=True)
        else:
            print(f'  ❌ {lbl} field not found')
    # 6. Scroll to stock/unit
    xml = scroll_down()
    for lbl, val, side in [('Current Stock', '5.5', 'left'), ('Low Alert', '1', 'right')]:
        f = field_below(lbl, side)
        if f:
            xml = tap_type(f, val, lbl, force_hide=True)
        else:
            print(f'  ❌ {lbl} field not found')
    # 7. Unit = KG via dropdown (tap field, select from popup)
    xml = dump()
    m = re.search(r'text="Unit \(KG/ML/PC\)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        # field is at same y as label
        fy = int(m.group(4))
        tap(540, fy + 30, 2.5)
        xml = dump()
        m2 = re.search(r'text="Kilogram \(KG\)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m2:
            tap((int(m2.group(1))+int(m2.group(3)))//2, (int(m2.group(2))+int(m2.group(4)))//2, 1.5)
            print('  ✅ Unit = KG')
        else:
            print('  ⚠️ KG option not found in popup')
    # 8. Scroll to batch
    xml = scroll_down()
    m = re.search(r'text="Batch Number \*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        ly = int(m.group(4))
        cands = [f for f in fields(xml) if ly+10 <= f['cy'] <= ly+340]
        if cands:
            xml = tap_type(cands[0], 'HERMES-BATCH-002', 'BatchNumber', force_hide=True)
    # 9. Mfg date + Exp date (pickers)
    xml = dump()
    # date picker views: clickable empty views under 'Batch & Expiry' section
    # find them: views after batch field before expiry-alert field
    clickable = re.findall(r'<node[^>]*clickable="true"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    # simpler: picker = empty clickable View pairs between batch & expiry alert
    fs = fields(xml)
    batch_y = None
    for f in fs:
        if 'HERMES-BATCH' in f['val']:
            batch_y = f['y']
    if batch_y:
        # find clickable view pairs below batch field
        pairs = []
        for m3 in re.finditer(r'<node[^>]*class="android.view.View"[^>]*clickable="true"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
            x1, y1, x2, y2 = map(int, m3.groups())
            if y1 > batch_y + 60:
                pairs.append((x1, y1, x2, y2))
        pairs.sort()
        if len(pairs) >= 2:
            # Mfg (left) & Exp (right) — same y
            left = [p for p in pairs if p[0] < 540]
            right = [p for p in pairs if p[0] > 540]
            if left:
                tap((left[0][0]+left[0][2])//2, (left[0][1]+left[0][3])//2, 3)
                xml = dump()
                # pick today: 'Monday, August 17, 2026'
                m4 = re.search(r'text="Monday, August 17, 2026"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
                if m4:
                    tap((int(m4.group(1))+int(m4.group(3)))//2, (int(m4.group(2))+int(m4.group(4)))//2, 1.5)
                    m5 = re.search(r'text="OK"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
                    if m5:
                        tap((int(m5.group(1))+int(m5.group(3)))//2, (int(m5.group(2))+int(m5.group(4)))//2, 1.5)
                        print('  ✅ Mfg date = Aug 17')
                xml = dump()
            if right:
                tap((right[0][0]+right[0][2])//2, (right[0][1]+right[0][3])//2, 3)
                xml = dump()
                m6 = re.search(r'text="Tuesday, August 18, 2026"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
                if m6:
                    tap((int(m6.group(1))+int(m6.group(3)))//2, (int(m6.group(2))+int(m6.group(4)))//2, 1.5)
                    m7 = re.search(r'text="OK"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
                    if m7:
                        tap((int(m7.group(1))+int(m7.group(3)))//2, (int(m7.group(2))+int(m7.group(4)))//2, 1.5)
                        print('  ✅ Exp date = Aug 18')
                xml = dump()
    # 10. Final verify + Save
    xml = dump()
    vals = [f['val'] for f in fields(xml)]
    need = ['HERMES-PROD-002', 'QA-SKU-002', '150', '250', '5.5', '1']
    missing = [v for v in need if v not in vals]
    print(f'FINAL VALS: {vals}')
    print(f'VERIFY: {"PASS ✅" if not missing else "MISSING: " + str(missing)}')
    # scroll to bottom for Save
    sh('adb shell input swipe 540 1100 540 1900 500')
    time.sleep(2)
    xml = dump()
    m = re.search(r'text="Save"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        sx = (int(m.group(1))+int(m.group(3)))//2
        sy = (int(m.group(2))+int(m.group(4)))//2
        # tap clickable parent (Save text is child; parent clickable below)
        tap(sx, sy, 1)
        tap(sx, sy, 4)
        xml = dump()
        print('FORM OPEN after save:', 'Add Product' in xml)
        if 'Add Product' not in xml:
            print('  ✅ SAVED!')
        else:
            print('  ❌ Save did not close form — validation fail?')
    else:
        print('  ❌ Save button not found')

if __name__ == '__main__':
    main()
