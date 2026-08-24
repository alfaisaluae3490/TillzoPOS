import subprocess, re, sys, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump(tag=""):
    time.sleep(1.2)
    sh("adb shell uiautomator dump /sdcard/q.xml >/dev/null 2>&1")
    time.sleep(0.8)
    return sh("adb shell cat /sdcard/q.xml 2>/dev/null")

def nodes(xml):
    out = []
    for m in re.finditer(r'<node[^>]+?>', xml):
        n = m.group(0)
        t = re.search(r'text="([^"]*)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        c = re.search(r'class="([^"]+)"', n)
        if not b: continue
        cls = c.group(1).split('.')[-1] if c else ''
        txt = (t.group(1) if t else '') or ''
        x1, y1, x2, y2 = map(int, b.groups())
        out.append((cls, txt, (x1 + x2) // 2, (y1 + y2) // 2, y1, (x1, y1, x2, y2)))
    return out

def find(xml, text, contains=False):
    for cls, txt, cx, cy, y1, bnd in nodes(xml):
        if (contains and text in txt) or (not contains and txt.strip() == text):
            return (cx, cy, bnd, txt)
    return None

def find_edittexts(xml):
    return [n for n in nodes(xml) if n[0] == 'EditText']

def tap(x, y):
    sh(f"adb shell input tap {x} {y}")
    time.sleep(2)

def show(tag, xml, maxy=1400):
    print(f"--- {tag} ---")
    for cls, txt, cx, cy, y1, bnd in nodes(xml):
        if txt.strip() and y1 < maxy:
            print(f"  y={y1:4d} {cls:11s} | {txt[:55]}")

# 1. launch
sh("adb shell am force-stop com.tillzo.pos")
time.sleep(1.5)
sh("adb shell am start -n com.tillzo.pos/.ui.MainActivity")
time.sleep(10)
xml = dump("after-launch")

# 2. PIN gate?
pin = find(xml, "Enter 4-Digit PIN", contains=True) or find(xml, "Unlock")
if pin or "PIN" in xml:
    print("PIN gate detected")
    eds = find_edittexts(xml)
    if eds:
        tap(eds[0][2], eds[0][3])
        sh('adb shell input text "1234"')
        time.sleep(1.5)
        unl = find(xml, "Unlock")
        if unl: tap(unl[0], unl[1])
        time.sleep(3)
        xml = dump("after-pin")

# 3. search product
eds = find_edittexts(xml)
if eds:
    tap(eds[0][2], eds[0][3])
    sh('adb shell input text "HERMES-PROD-001"')
    time.sleep(3)
    xml = dump("after-search")
    prod = find(xml, "HERMES-PROD-001")
    if prod:
        tap(prod[0], prod[1])
        time.sleep(2.5)
        xml = dump("after-add")
        show("state", xml, 2200)
    else:
        print("PRODUCT NOT FOUND")
        sys.exit(1)
else:
    print("NO SEARCH FIELD")
    sys.exit(1)

# 4. till gate?
gate = find(xml, "Open Till")
if gate:
    print("Till gate -> opening till")
    tap(gate[0], gate[1])
    time.sleep(2.5)
    xml = dump("till-dialog")
    eds = find_edittexts(xml)
    if eds:
        tap(eds[0][2], eds[0][3])
        sh('adb shell input text "1000"')
        time.sleep(1.5)
    # find confirm/open button
    xml = dump("till-dialog2")
    btn = find(xml, "Open Till") or find(xml, "Start Shift") or find(xml, "Open Register") or find(xml, "Confirm")
    if btn:
        tap(btn[0], btn[1])
        time.sleep(3)
    xml = dump("after-till")
    # re-add product
    eds = find_edittexts(xml)
    if eds:
        tap(eds[0][2], eds[0][3])
        sh('adb shell input text "HERMES-PROD-001"')
        time.sleep(3)
        xml = dump("search2")
        prod = find(xml, "HERMES-PROD-001")
        if prod:
            tap(prod[0], prod[1])
            time.sleep(2.5)
            xml = dump("after-add2")
show("final-cart", xml, 2200)
