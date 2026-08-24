#!/usr/bin/env python3
"""Clean state: scanner home. Now open Advanced Menu -> Force Sync -> verify sheet."""
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

d.click(1003, 254); time.sleep(3)

fs = None
for _ in range(4):
    fs = find_bounds(dump(), r'Force Sync')
    if fs: break
    d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2); time.sleep(3)
    conf = find_bounds(dump(), r'^Force Sync$')
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
        print('sync confirmed')
    time.sleep(12)
    log = d.shell('logcat -d -t 300 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-150:])
