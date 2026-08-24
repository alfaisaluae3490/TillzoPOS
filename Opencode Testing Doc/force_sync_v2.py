#!/usr/bin/env python3
"""Force sync menu didn't open (maybe back-presses closed app to home). Re-navigate carefully."""
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

xml = dump()
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('current:', texts[:8])

# ensure in app main screen
if 'TillzoPOS' not in xml:
    d.shell('am start -n com.tillzo.pos/.ui.MainActivity')
    time.sleep(6)
    xml = dump()

# open advanced menu via icon top-right
d.click(1003, 254)
time.sleep(3)
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('menu:', texts[:10])

fs = find_bounds(xml, r'Force Sync')
if not fs:
    d.swipe(540, 1900, 540, 700, duration=0.5)
    time.sleep(1.5)
    fs = find_bounds(dump(), r'Force Sync')
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
