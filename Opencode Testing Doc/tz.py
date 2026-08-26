#!/usr/bin/env python3
"""tz.py — Tillzo ULTRA protocol helper (uiautomator2 wrapper).
Works with whisper testenv (u2 installed there)."""
import subprocess, time, re

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

def connect():
    import uiautomator2 as u2
    return u2.connect('emulator-5554')

def dump(d):
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    """Find text node matching regex; return bounds tuple or None."""
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        val = m.group(1) if m else ''
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

def click_center(d, b):
    d.click((b[0] + b[2]) // 2, (b[1] + b[3]) // 2)

def click_text(d, xml, pattern, wait=2.5):
    b = find_bounds(xml, pattern)
    if b:
        click_center(d, b)
        time.sleep(wait)
        return True
    return False

def texts(xml, limit=20):
    out = [t for t in re.findall(r'text="([^"]{1,80})"', xml) if t.strip()]
    return out[:limit]

def adb(*args):
    return subprocess.run([ADB, '-s', 'emulator-5554'] + list(args), capture_output=True, text=True)

def force_sync(d, max_scroll=8):
    """Navigate: ensure scanner home -> menu(1003,254) -> scroll -> Force Sync -> confirm."""
    xml = dump(d)
    tries = 0
    while 'Tap to activate scanner' not in xml and tries < 3:
        d.press('back'); time.sleep(2)
        xml = dump(d)
        tries += 1
    d.click(1003, 254); time.sleep(3)
    fs = None
    for _ in range(max_scroll):
        xml = dump(d)
        for n in re.findall(r'<node[^>]*>', xml):
            m = re.search(r'text="(Force Sync)"', n)
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if m and b:
                x1, y1, x2, y2 = map(int, b.groups())
                if y1 > 100:
                    fs = (x1, y1, x2, y2); break
        if fs: break
        d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.2)
    if not fs:
        return False
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2); time.sleep(3)
    # cooldown confirm dialog button (lower on screen)
    conf = None
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1, y1, x2, y2 = map(int, b.groups())
            if y1 > 1000: conf = (x1, y1, x2, y2)
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
    time.sleep(14)
    log = d.shell('logcat -d -t 300 | grep -c "SyncWorker completed"').output.strip()
    return True

def sheet_export():
    """Download live sheet xlsx via Chrome profile & return local path."""
    xlsx = r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx'
    if os.path.exists(xlsx):
        try: os.remove(xlsx)
        except Exception: pass
    powershell = [
        'powershell', '-NoProfile', '-Command',
        "Start-Process -FilePath 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' "
        "-ArgumentList '--profile-directory=\"Profile 3\"',"
        "'https://docs.google.com/spreadsheets/d/17D6T0Mn2VOM-qB1br5fk-M8eMOMqE8ou3Lj8zX54Kag/export?format=xlsx'"
    ]
    subprocess.run(powershell, capture_output=True)
    import time as _t; _t.sleep(22)
    return xlsx if os.path.exists(xlsx) else None

import os
