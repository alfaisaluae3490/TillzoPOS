#!/usr/bin/env python3
"""Check state; navigate home; sync via menu with robust loop."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],capture_output=True,text=True).stdout
print('resumed:', out.strip()[:100])
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,40})"', xml) if t.strip()]
print('screen:', texts[:8])

# get back to scanner home
for _ in range(3):
    if 'Tap to activate scanner' in xml:
        break
    d.press('back'); time.sleep(2)
    xml = dump(d)

print('on home:', 'Tap to activate scanner' in xml)

# menu -> force sync
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
    time.sleep(12)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-120:])
