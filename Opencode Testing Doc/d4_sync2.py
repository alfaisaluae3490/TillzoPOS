#!/usr/bin/env python3
"""Check state + robust force sync: try multiple menu icon positions."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('state:', texts[:8])

# if CRM screen, exit
if 'Search Customers' in xml:
    d.press('back'); time.sleep(2)

# ensure scanner home
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.shell('am start -n com.tillzo.pos/.ui.MainActivity'); time.sleep(6)

# open menu & scroll to Force Sync
d.click(1003, 254); time.sleep(3)
fs = None
for _ in range(5):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Force Sync"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            fs = (x1,y1,x2,y2)
            break
    if fs: break
    d.swipe(540, 1900, 540, 800, duration=0.4); time.sleep(1.2)

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
            # confirm dialog button is lower on screen (~1256-1308 y from earlier)
            if y1 > 1000: conf = (x1,y1,x2,y2)
    if conf:
        print('confirm at:', conf)
        d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(12)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-150:])
