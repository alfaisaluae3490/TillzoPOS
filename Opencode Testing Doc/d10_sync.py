#!/usr/bin/env python3
"""BOTH D10 items imported! Clear search, force sync, then uninstall/reinstall test."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

# clear search
search = find_bounds(dump(d), r'D10')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1)
    d.shell('input keyevent 123')
    for _ in range(6): d.shell('input keyevent KEYCODE_DEL')
    d.press('back'); time.sleep(1.5)

# home -> menu -> force sync
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

d.click(1003, 254); time.sleep(3)
fs = None
for _ in range(6):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 100: fs = (x1,y1,x2,y2)
    if fs: break
    d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.2)

print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2,(fs[1]+fs[3])//2); time.sleep(3)
    conf = None
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 1000: conf = (x1,y1,x2,y2)
    if conf:
        d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(15)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-120:])
