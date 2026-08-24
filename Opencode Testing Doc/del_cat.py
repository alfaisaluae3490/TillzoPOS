#!/usr/bin/env python3
"""Delete AUDIT-CAT-X via its row delete icon (815,950-881,1016), confirm if asked,
sync, verify sheet removal."""
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

# click delete icon on AUDIT-CAT-X row
d.click(848, 983)
time.sleep(3)

xml = dump()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('after delete:', texts[:10])

# confirmation dialog?
conf = find_bounds(xml, r'^(Delete|Yes|OK|Confirm)$')
if conf:
    d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
    time.sleep(3)

xml = dump()
print('gone locally:', 'AUDIT-CAT-X' not in xml)

# close manager + dialogs, force sync
close = find_bounds(xml, r'^Close$')
if close:
    d.click((close[0]+close[2])//2, (close[1]+close[3])//2); time.sleep(2)
cancel = find_bounds(dump(), r'^Cancel$')
if cancel:
    d.click((cancel[0]+cancel[2])//2, (cancel[1]+cancel[3])//2); time.sleep(2)
d.press('back'); time.sleep(1.5)

# menu -> force sync
d.click(1003, 254); time.sleep(3)
fs = None
for _ in range(3):
    fs = find_bounds(dump(), r'Force Sync')
    if fs: break
    d.swipe(540, 1900, 540, 700, duration=0.5); time.sleep(1)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2); time.sleep(3)
    conf2 = find_bounds(dump(), r'^Force Sync$')
    if conf2:
        d.click((conf2[0]+conf2[2])//2, (conf2[1]+conf2[3])//2)
    time.sleep(12)
    log = d.shell('logcat -d -t 300 | grep -E "SyncWorker completed|delete marker"')
    s = log.output if hasattr(log,'output') else str(log)
    print(s.strip()[-250:])
