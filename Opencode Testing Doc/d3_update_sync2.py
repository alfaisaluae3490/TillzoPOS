#!/usr/bin/env python3
"""fs found only icon (55,1697) — click the TEXT version. Dump menu items near that Y."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
# find Force Sync text node
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Force Sync"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        print('text fs at:', b.groups())
        x1,y1,x2,y2 = map(int,b.groups())
        d.click((x1+x2)//2,(y1+y2)//2)
        break
else:
    # maybe menu closed; reopen
    print('menu not open, reopening...')
    d.press('back'); time.sleep(1.5)
    d.click(1003, 254); time.sleep(3)
    for _ in range(4):
        found = False
        xml = dump(d)
        for n in re.findall(r'<node[^>]*>', xml):
            m = re.search(r'text="Force Sync"', n)
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if m and b:
                x1,y1,x2,y2 = map(int,b.groups())
                d.click((x1+x2)//2,(y1+y2)//2)
                found = True
                break
        if found: break
        d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)

time.sleep(3)
# confirm cooldown dialog
conf = find_bounds(dump(d), r'^Force Sync$')
print('confirm:', conf)
if conf:
    d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
time.sleep(12)
log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|VendorUpsert"')
s = log.output if hasattr(log,'output') else str(log)
print(s.strip()[-300:])
