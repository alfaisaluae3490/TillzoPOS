#!/usr/bin/env python3
"""GRN SUCCESS! 'Goods Received Successfully' + 1 new batch. Return to PO list,
force sync, verify: HERMES-PROD-001 stock 19->20, GRN_Headers/Items sheet tabs."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
ret = find_bounds(dump(d), r'Return to PO List')
if ret:
    d.click((ret[0]+ret[2])//2,(ret[1]+ret[3])//2)
    time.sleep(4)

# home
d.press('back'); time.sleep(2)
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

# force sync
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
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|GRN"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-250:])
