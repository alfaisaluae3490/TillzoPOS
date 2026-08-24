#!/usr/bin/env python3
"""Advanced Options menu IS open (1003,254 worked). Scroll down inside it to Force Sync."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# scroll inside the menu sheet
for _ in range(3):
    d.swipe(540, 1900, 540, 700, duration=0.5)
    time.sleep(1)
    fs = find_bounds(dump(), r'Force Sync')
    if fs:
        break
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2)
    time.sleep(3)
    conf = find_bounds(dump(), r'^Force Sync$')
    print('confirm:', conf)
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
    time.sleep(12)
    log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|CategoryUpsertUseCase"')
    s = log.output if hasattr(log, 'output') else str(log)
    print(s.strip()[-350:])
