#!/usr/bin/env python3
"""We're in PO screen (Draft PO-202608-0004 exists!). Menu overlay is open on top.
Scroll DOWN in menu to reach Force Sync, click it."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

fs = None
for _ in range(6):
    xml = dump(d)
    fs = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 200:  # visible
                fs = (x1,y1,x2,y2)
    if fs: break
    d.swipe(540, 1900, 540, 900, duration=0.4); time.sleep(1.2)

print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2,(fs[1]+fs[3])//2)
    time.sleep(3)
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
    time.sleep(12)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-150:])
