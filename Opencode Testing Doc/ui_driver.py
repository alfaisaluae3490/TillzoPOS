#!/usr/bin/env python3
"""Tillzo UI Driver - one-shot automation to kill tool-call round-trips."""
import subprocess, re, sys, time

ADB = r"C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe"
SERIAL = "emulator-5554"

def sh(cmd, timeout=30):
    return subprocess.run([ADB, "-s", SERIAL, "shell", cmd],
                          capture_output=True, text=True, timeout=timeout).stdout

def dump():
    sh("uiautomator dump /data/local/tmp/ui.xml")
    return sh("cat /data/local/tmp/ui.xml")

def texts(xml):
    return [t for t in re.findall(r'text="([^"]{1,80})"', xml) if t.strip()]

def find_bounds(xml, pattern):
    """Return center (x,y) of first node whose text matches regex pattern."""
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return ((x1+x2)//2, (y1+y2)//2)
    return None

def tap(x, y):
    sh(f"input tap {x} {y}")

# ---- Flow: open AUDIT-ITEM-ALPHA form, set stock, save, verify ----
def run(new_val="18"):
    # 0. if stuck on camera/OCR screen, back out first
    xml = dump()
    if "Point camera" in xml:
        sh("input keyevent 4"); time.sleep(2); xml = dump()
    # 1. If already in inventory list (has Import CSV icon), skip nav.
    #    Otherwise tap inventory icon; if OCR opens instead, back + we're in list.
    if "Import CSV" not in xml:
        sh("input tap 871 254"); time.sleep(3)
        xml = dump()
        if "Point camera" in xml:
            sh("input keyevent 4"); time.sleep(2)
            xml = dump()
    # 1c. if Downloads picker open, close it
    if "FILES ON DOWNLOADS" in xml:
        sh("input keyevent 4"); time.sleep(2); xml = dump()
    # 2. find ALPHA card in the LIST view and tap it
    c = find_bounds(xml, r"AUDIT-ITEM-ALPHA")
    if not c:
        return f"FAIL: ALPHA card not found | screen={texts(xml)[:6]}"
    tap(*c); time.sleep(3)
    # 3. scroll down to stock area
    sh("input swipe 540 1500 540 1000 400"); time.sleep(2)
    # 4. find stock field (value currently 12.0) and tap it
    xml = dump()
    stock_c = None
    for n in re.findall(r'<node[^>]*>', xml):
        if 'EditText' in n and 'text="12.0"' in n:
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                stock_c = ((int(b.group(1))+int(b.group(3)))//2,
                           (int(b.group(2))+int(b.group(4)))//2)
            break
    if not stock_c:
        return "FAIL: stock field (12.0) not visible after scroll"
    # clamp tap to safe area (field may extend past screen)
    tap(stock_c[0], min(stock_c[1], 1900)); time.sleep(2)
    # 5. select-all, delete, type new value
    sh("input keyevent 123")  # MOVE_END
    for _ in range(6):
        sh("input keyevent KEYCODE_DEL")
    sh(f"input text {new_val}"); time.sleep(1)
    # 6. close IME so it can't eat the Save tap
    sh("input keyevent 111")  # ESC
    time.sleep(2)
    # 7. fresh dump -> find Save -> tap
    xml = dump()
    save_c = find_bounds(xml, r"^Save$")
    if not save_c:
        return f"FAIL: Save button not found after edit (typed {new_val})"
    tap(*save_c); time.sleep(3)
    # 8. form should be closed; check SyncWorker triggered
    log = sh("logcat -d -t 60", timeout=15)
    synced = bool(re.search(r'SyncWorker started', log))
    # 9. verify card now shows new stock
    time.sleep(2)
    sh("input keyevent 4"); time.sleep(1); sh("input keyevent 4"); time.sleep(2)
    xml = dump()
    out = []
    ts = texts(xml)
    for i, t in enumerate(ts):
        if "AUDIT-ITEM-ALPHA" in t:
            out = ts[i:i+4]; break
    return f"RESULT: sync_triggered={synced} | CARD={out}"

if __name__ == "__main__":
    val = sys.argv[1] if len(sys.argv) > 1 else "18"
    print(run(val))
