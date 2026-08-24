import subprocess, re, sys, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump():
    for attempt in range(5):
        time.sleep(6)
        sh("adb shell uiautomator dump /sdcard/r.xml >/dev/null 2>&1")
        time.sleep(1.5)
        xml = sh("adb shell cat /sdcard/r.xml 2>/dev/null")
        if xml and len(xml) > 2000 and '<node' in xml:
            return xml
        time.sleep(3)
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
        out.append((cls, txt, (x1+x2)//2, (y1+y2)//2, y1))
    return out

def find(xml, text, contains=False):
    for cls, txt, cx, cy, y1 in nodes(xml):
        if (contains and text in txt) or (not contains and txt.strip() == text):
            return (cx, cy)
    return None

# 1. open dialog (PAY NOW)
xml = dump()
pay = find(xml, "PAY NOW")
if not pay:
    print("PAY NOW not found"); sys.exit(1)
sh(f"adb shell input tap {pay[0]} {pay[1]}")
time.sleep(3)
xml = dump()
if not find(xml, "Payment"):
    print("Dialog did not open"); sys.exit(1)
print("Dialog open")

# 2. cash
eds = [n for n in nodes(xml) if n[0] == 'EditText']
if not eds:
    print("No cash field"); sys.exit(1)
cx, cy = eds[0][2], eds[0][3]
sh(f"adb shell input tap {cx} {cy}")
time.sleep(1.5)
sh('adb shell input text "110"')
time.sleep(1.5)
xml = dump()
first_ed = [n for n in nodes(xml) if n[0] == 'EditText']
print("Cash value:", first_ed[0][1] if first_ed else "?")

# 3. confirm — find ANY Confirm Payment node
conf = find(xml, "Confirm Payment")
if not conf:
    # maybe hidden below — dump may still include it
    print("Confirm not in dump, scrolling content via swipe INSIDE sheet (top area)")
    sh("adb shell input swipe 540 1400 540 900 300")
    time.sleep(2)
    xml = dump()
    conf = find(xml, "Confirm Payment")
if not conf:
    print("STILL no confirm")
    # dump all texts for debug
    for cls, txt, cx, cy, y1 in nodes(xml):
        if txt.strip(): print(f"  y={y1} {txt[:45]}")
    sys.exit(1)
print("Tapping Confirm at", conf)
sh(f"adb shell input tap {conf[0]} {conf[1]}")
time.sleep(5)
xml = dump()
# check result
if "receipt" in xml.lower() or "Thank you" in xml or "Invoice" in xml:
    print("RESULT: receipt screen")
elif find(xml, "Cart (0)") or (find(xml, "PAY NOW") is None and "Payment" not in xml):
    print("RESULT: back on home (cart cleared)")
else:
    print("RESULT: unknown state")
    for cls, txt, cx, cy, y1 in nodes(xml):
        if txt.strip() and y1 < 1300: print(f"  y={y1} {txt[:50]}")
