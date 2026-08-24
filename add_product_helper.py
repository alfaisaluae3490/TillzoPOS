"""TillzoPOS Add Product helper — tap→verify-focus→type→verify-value discipline"""
import subprocess, re, time, sys

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def sh_list(cmd):
    return subprocess.run(cmd, capture_output=True, text=True).stdout

def dump():
    sh('adb shell uiautomator dump /sdcard/ui.xml')  # output line = sync barrier
    return sh_list(['adb', 'exec-out', 'cat', '/sdcard/ui.xml'])

def get_fields(xml):
    out = []
    for m in re.finditer(r'<node[^>]*class="android.widget.EditText"[^>]*>', xml):
        n = m.group(0)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        t = re.search(r'text="([^"]*)"', n)
        f = re.search(r'focused="(true|false)"', n)
        if b:
            out.append({'y': int(b.group(2)), 'val': t.group(1) if t else '',
                        'bounds': f'[{b.group(1)},{b.group(2)}][{b.group(3)},{b.group(4)}]',
                        'cx': (int(b.group(1)) + int(b.group(3))) // 2,
                        'cy': (int(b.group(2)) + int(b.group(4))) // 2,
                        'focused': f.group(1) == 'true' if f else False})
    return sorted(out, key=lambda x: x['y'])

def tap(x, y):
    sh(f'adb shell input tap {x} {y}')
    time.sleep(1.2)

def type_text(t):
    sh(f'adb shell input text "{t}"')
    time.sleep(0.8)

def hide_kb():
    sh('adb shell input keyevent 4')
    time.sleep(1)

def find_field(xml, val=None, idx=None):
    fs = get_fields(xml)
    if idx is not None and idx < len(fs):
        return fs[idx]
    for f in fs:
        if f['val'] == val:
            return f
    return None

def fill_by_verify(xml, idx, text, label_desc):
    """Tap field by index, verify it got focus, type, verify value"""
    fs = get_fields(xml)
    if idx >= len(fs):
        print(f'  !! field idx {idx} missing (have {len(fs)})')
        return None
    f = fs[idx]
    tap(f['cx'], f['cy'])
    xml2 = dump()
    fs2 = get_fields(xml2)
    focused = [x for x in fs2 if x['focused']]
    if focused and abs(focused[0]['cy'] - f['cy']) > 60:
        # focus went elsewhere — use the focused field instead
        print(f'  !! focus drift: wanted y={f["cy"]} got y={focused[0]["cy"]} ({label_desc})')
        f = focused[0]
    # clear existing
    sh('adb shell input keyevent 123')  # END
    cur = f['val']
    for _ in range(len(cur) + 2):
        sh('adb shell input keyevent 67')
    time.sleep(0.3)
    type_text(text)
    xml3 = dump()
    fs3 = get_fields(xml3)
    # verify value present
    vals = [x['val'] for x in fs3]
    if text in vals:
        print(f'  ✅ {label_desc} = {text}')
        return xml3
    print(f'  ⚠️ {label_desc}: typed {text} but values={vals}')
    return xml3

def scroll_up():
    sh('adb shell input swipe 540 1700 540 1000 400')
    time.sleep(1.5)

def main():
    print('=== Add Product: HERMES-PROD-001 ===')
    # ensure on Inventory Management, open Add Product
    xml = dump()
    if 'Add Product' not in xml:
        # find Add Item FAB
        m = re.search(r'content-desc="Add Item"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2)
        else:
            print('!! Add Item FAB not found'); return
        time.sleep(3.5)  # tree settle
        xml = dump()
    print('Form open ✅')
    # fields after settle
    fs = get_fields(xml)
    for i, f in enumerate(fs):
        print(f'  [{i}] y={f["y"]} val={f["val"]!r} {f["bounds"]}')
    # 1. Name (idx 0)
    xml = fill_by_verify(xml, 0, 'HERMES-PROD-001', 'Name')
    # 2. SKU (idx 1)
    xml = fill_by_verify(xml, 1, 'QA-SKU-001', 'SKU')
    hide_kb()
    # 3. Main Category dropdown (idx 2)
    xml = dump()
    fs = get_fields(xml)
    f = fs[2]
    tap(f['cx'], f['cy'])
    time.sleep(2.5)
    xml = dump()
    m = re.search(r'text="HERMES-CAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2)
        time.sleep(1.5)
        print('  ✅ Main Category = HERMES-CAT-001')
    else:
        print('  !! category option not found')
    # 4. Subcategory
    xml = dump()
    m = re.search(r'text="Select Subcategory"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if m:
        tap((int(m.group(1))+int(m.group(3)))//2, (int(m.group(2))+int(m.group(4)))//2)
        time.sleep(2.5)
        xml = dump()
        m2 = re.search(r'text="HERMES-SUBCAT-001"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m2:
            tap((int(m2.group(1))+int(m2.group(3)))//2, (int(m2.group(2))+int(m2.group(4)))//2)
            time.sleep(1.5)
            print('  ✅ Subcategory = HERMES-SUBCAT-001')
        else:
            print('  !! subcat option not found')
    # scroll to pricing
    scroll_up()
    xml = dump()
    fs = get_fields(xml)
    # find Cost (left of 2-col) & Selling (right) by position: 2 fields same y
    rows = {}
    for f in fs:
        rows.setdefault(f['y'], []).append(f)
    for y, r in sorted(rows.items()):
        if len(r) == 2:
            print(f'  row y={y}: left={r[0]["val"]!r} right={r[1]["val"]!r}')
    # Cost/Selling: the 2-col row where left is empty (GTIN row is left-half too... careful)
    # fill by labels: find 'Cost Price'/'Selling Price' labels
    for lbl, val in [('Cost Price', '50'), ('Selling Price', '100')]:
        m = re.search(r'text="' + lbl + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
        if m:
            # field is below label
            ly = int(m.group(4))
            # find field whose cy is within ly+40..ly+260 and x overlaps
            fx = (int(m.group(1)) + int(m.group(3))) // 2
            cands = [f for f in fs if ly + 30 <= f['cy'] <= ly + 300 and abs(f['cx'] - fx) < 200]
            if cands:
                f = cands[0]
                tap(f['cx'], f['cy'])
                time.sleep(1)
                xml2 = dump()
                # clear + type
                sh('adb shell input keyevent 123')
                for _ in range(8): sh('adb shell input keyevent 67')
                time.sleep(0.3)
                type_text(val)
                xml2 = dump()
                print(f'  ✅ {lbl} = {val}' if val in [x["val"] for x in get_fields(xml2)] else f'  ⚠️ {lbl} set check')
                xml = xml2
    print('=== phase 1 done ===')

if __name__ == '__main__':
    main()
