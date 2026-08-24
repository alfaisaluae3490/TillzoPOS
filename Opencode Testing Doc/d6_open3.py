#!/usr/bin/env python3
"""D6 open v3 — fixed regex bug."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(9)

print('home:', 'Tap to activate scanner' in dump(d))

d.click(1003, 254); time.sleep(3)

exp = None
for _ in range(6):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Expenses)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            exp = (x1,y1,x2,y2)
    if exp: break
    d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.2)

print('expenses item:', exp)
if exp:
    d.click((exp[0]+exp[2])//2,(exp[1]+exp[3])//2)
    time.sleep(4)
    xml = dump(d)
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print('expenses screen:', texts[:16])
