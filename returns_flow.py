import subprocess, re, sys, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump(tag=""):
    for attempt in range(5):
        time.sleep(5)
        sh("adb shell uiautomator dump /sdcard/t.xml >/dev/null 2>&1")
        time.sleep(1.2)
        xml = sh("adb shell cat /sdcard/t.xml 2>/dev/null")
        if xml and len(xml) > 2000 and '<node' in xml:
            return xml
        time.sleep(2)
    return xml

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
        out.append((cls, txt, (x1+x2)//2, (y1+y2)//2, y1, (x1, y1, x2, y2)))
    return out

def find(xml, text, contains=False):
    for cls, txt, cx, cy, y1, bnd in nodes(xml):
        if (contains and text in txt) or (not contains and txt.strip() == text):
            return (cx, cy, bnd)
    return None

def tap(x, y, wait=2.5):
    sh(f"adb shell input tap {x} {y}")
    time.sleep(wait)

def hide_kb():
    sh("adb shell input keyevent KEYCODE_BACK")
    time.sleep(1.5)

# 1. launch clean
sh("adb shell am start -n com.tillzo.pos/.ui.MainActivity -f 0x10008000 >/dev/null 2>&1")
time.sleep(11)
xml = dump()
# PIN?
if find(xml, "Unlock"):
    eds = [n for n in nodes(xml) if n[0] == 'EditText']
    if eds:
        tap(eds[0][2], eds[0][3], 1.5)
        sh('adb shell input text "1234"')
        time.sleep(1.5)
        u = find(xml, "Unlock")
        if u: tap(u[0], u[1], 3)
        xml = dump()
# home screen scroll position restores scrolled-down — scroll to top
for i in range(3):
    if "TillzoPOS" in xml and "Search by name" in xml:
        break
    sh("adb shell input swipe 540 500 540 1900 300")
    time.sleep(2)
    xml = dump()
print("home:", "Search by name" in xml or "TillzoPOS" in xml)

# 2. open menu (top-right hamburger at ~1003,254)
tap(1003, 254, 3)
xml = dump()
r = find(xml, "Returns & Refunds")
if not r:
    # may be HTML-escaped
    r = find(xml, "Returns", contains=True)
if not r:
    print("MENU RETURNS NOT FOUND"); sys.exit(1)
print("returns row:", r)
tap(r[0], r[1], 4)
xml = dump()
if not (find(xml, "Invoice UUID") or "Invoice" in xml):
    print("RETURNS SCREEN NOT OPEN"); sys.exit(1)
print("returns screen open")

# 3. enter UUID
eds = [n for n in nodes(xml) if n[0] == 'EditText']
if not eds:
    print("no search field"); sys.exit(1)
tap(eds[0][2], eds[0][3], 2)
sh('adb shell input text "845ed833-ebb9-42ec-93fe-d10d7d4de944"')
time.sleep(3.5)
xml = dump()
print("invoice found:", "Invoice Found" in xml or "Total:" in xml)

# 4. hide keyboard, then tap Return to Inventory
hide_kb()
xml = dump()
btn = find(xml, "Return to Inventory")
if not btn:
    print("RETURN BTN NOT FOUND")
    for cls, txt, cx, cy, y1, bnd in nodes(xml):
        if txt.strip(): print(f"  y={y1} {txt[:50]}")
    sys.exit(1)
print("tapping Return to Inventory at", (btn[0], btn[1]))
tap(btn[0], btn[1], 5)
xml = dump()
status = "Refund Processed" in xml
print("REFUND PROCESSED:", status)
for cls, txt, cx, cy, y1, bnd in nodes(xml):
    if txt.strip() and y1 < 900:
        print(f"  y={y1} {txt[:55]}")
