#!/usr/bin/env python3
"""Check screen state, then navigate to scanner home -> menu(1003,254) -> scroll -> Force Sync."""
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

xml = dump()
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('state:', texts[:8])

# close any dialogs
for pat in [r'^Close$', r'^Cancel$', r'^OK$']:
    c = find_bounds(xml, pat)
    if c:
        d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
        time.sleep(2)
        xml = dump()

# if not on home, go home via back
if 'Tap to activate scanner' not in xml and 'Search items' not in xml:
    d.press('back'); time.sleep(2)

# now menu
d.click(1003, 254); time.sleep(3)
fs = None
for _ in range(3):
    fs = find_bounds(dump(), r'Force Sync')
    if fs: break
    d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2); time.sleep(3)
    conf = find_bounds(dump(), r'^Force Sync$')
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
    time.sleep(12)
    log = d.shell('logcat -d -t 300 | grep -E "SyncWorker completed"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-200:])
