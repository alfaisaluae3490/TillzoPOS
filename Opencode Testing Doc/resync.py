#!/usr/bin/env python3
"""STOCK NOW 18.0?! Was 19 before GRN. GRN added batch qty 1.0 → recalc replaced
current_stock with SUM(batches). If HERMES had a pre-existing BATCH-INITIAL with qty 17,
then 17+1=18 ✓. That's consistent: product had hasBatches=true w/ hidden batch 17.
So stock 18 is CORRECT behavior (19 was stale sheet value from earlier manual entry).

Sheet shows 19.0 because the recalc marked pending and synced BEFORE? No — force sync
ran after. Sheet may lag one cycle. Verify: force sync again & re-export."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

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
print('sync done')
