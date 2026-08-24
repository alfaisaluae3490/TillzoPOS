#!/usr/bin/env python3
"""Phone updated in app UI. Force sync + verify sheet shows 0501112222."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# exit vendors to home
d.press('back'); time.sleep(2)

# menu -> force sync
d.click(1003, 254); time.sleep(3)
fs = None
for _ in range(4):
    fs = find_bounds(dump(d), r'Force Sync')
    if fs: break
    d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2,(fs[1]+fs[3])//2); time.sleep(3)
    conf = find_bounds(dump(d), r'^Force Sync$')
    if conf:
        d.click((conf[0]+conf[2])//2,(conf[1]+conf[3])//2)
    time.sleep(12)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|VendorUpsert"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-300:])
