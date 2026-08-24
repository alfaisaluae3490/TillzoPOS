#!/usr/bin/env python3
"""Force Sync TEXT is visible in menu at current scroll! Find exact bounds and click."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
fs = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Force Sync"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        fs = tuple(map(int,b.groups()))
print('fs bounds:', fs)
if fs:
    d.click((fs[0]+fs[2])//2,(fs[1]+fs[3])//2)
    time.sleep(3)
    # confirm dialog (second Force Sync button, lower on screen)
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
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|CustomerUpsert"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-300:])
