#!/usr/bin/env python3
"""Silent = script crashed (menu state). Print progress at each step."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
print('step1 home:', 'Tap to activate scanner' in xml)

d.click(1003,254); time.sleep(3)
xml = dump(d)
print('step2 menu:', 'Advanced Options' in xml)

fs = None
for _ in range(6):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 100:
                fs = (x1,y1,x2,y2)
    if fs: break
    d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.2)
print('step3 fs:', fs)

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
    print('step4 confirm:', conf)
    if conf:
        d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(15)

out = d.shell('logcat -d | grep -E "InventoryUpsertUseCase" | tail -4')
s = out.output if hasattr(out,'output') else str(out)
print('LOGS:', s.strip()[-400:])
