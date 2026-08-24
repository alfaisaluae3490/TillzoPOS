#!/usr/bin/env python3
"""Category added locally. Close manager, force sync, verify sheet Categories tab."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

close = find_bounds(dump(), r'^Close$')
if close:
    d.click((close[0]+close[2])//2, (close[1]+close[3])//2)
    time.sleep(1.5)

# close Edit Product dialog too
d.press('back'); time.sleep(1.5); d.press('back'); time.sleep(1.5)

# force sync via menu
xml = dump()
if 'Search items' not in xml:
    d.click(871, 254); time.sleep(2.5)
d.click(1003, 254); time.sleep(3)
for _ in range(3):
    d.swipe(540, 1900, 540, 700, duration=0.4)
    time.sleep(0.8)
fs = find_bounds(dump(), r'Force Sync')
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2)
    time.sleep(3)
    conf = find_bounds(dump(), r'^Force Sync$')
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
time.sleep(12)
log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|CategoryUpsert"')
s = log.output if hasattr(log, 'output') else str(log)
print(s.strip()[-300:])
