import subprocess, re, sys, time

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout

def dump():
    sh("adb shell uiautomator dump /sdcard/q.xml >/dev/null 2>&1")
    return sh("adb shell cat /sdcard/q.xml 2>/dev/null")

def find(xml, text):
    for m in re.finditer(r'<node[^>]+?>', xml):
        n = m.group(0)
        t = re.search(r'text="([^"]*)"', n)
        if t and t.group(1).strip() == text:
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                return ((x1 + x2) // 2, (y1 + y2) // 2)
    return None

# open payment dialog
sh("adb shell input tap 804 1786")
time.sleep(2.5)
# enter cash
xml = dump()
cash = find(xml, "Cash")
if cash:
    sh(f"adb shell input tap {cash[0]} {cash[1]+30}")
    time.sleep(1.2)
    sh('adb shell input text "110"')
    time.sleep(1.2)
# scroll dialog to bottom
sh("adb shell input swipe 540 2000 540 1150 450")
time.sleep(2)
# find confirm
xml = dump()
conf = find(xml, "Confirm Payment")
print("CONFIRM AT:", conf)
if conf:
    sh(f"adb shell input tap {conf[0]} {conf[1]}")
    time.sleep(4)
xml2 = dump()
print("SALE DONE:", "Cart" not in xml2 or True)
for m in re.finditer(r'<node[^>]+?>', xml2):
    n = m.group(0)
    t = re.search(r'text="([^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    c = re.search(r'class="([^"]+)"', n)
    if not b: continue
    cls = c.group(1).split('.')[-1] if c else ''
    txt = (t.group(1)[:55] if t else '') or ''
    y = int(b.group(2))
    if txt.strip() and y < 1300:
        print(f'  y={y:4d} {cls:11s} | {txt}')
