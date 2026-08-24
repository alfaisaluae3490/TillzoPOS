#!/usr/bin/env python3
"""App not on home (step1 false). Force-stop + relaunch for clean state, then sync."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

subprocess.run([ADB,'-s','emulator-5554','shell','am force-stop com.tillzo.pos'],capture_output=True)
time.sleep(2)
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(10)

xml = dump(d)
print('home:', 'Tap to activate scanner' in xml)

d.click(1003,254); time.sleep(3)
xml = dump(d)
print('menu:', 'Advanced Options' in xml)

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
    print('confirm:', conf)
    if conf:
        d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(15)

out = d.shell('logcat -d | grep -E "InventoryUpsertUseCase" | tail -4')
s = out.output if hasattr(out,'output') else str(out)
print('LOGS:', s.strip()[-450:])
