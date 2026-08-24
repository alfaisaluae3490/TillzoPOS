#!/usr/bin/env python3
"""Menu opened but scrolled past Vendors. Scroll UP inside menu to find Vendors item.
ALSO: Force Sync is visible here — click it first for pending sync."""
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
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

# Force Sync visible in current view? Click it (cooldown confirm may appear)
fs = find_bounds(dump(), r'^Force Sync$')
print('fs:', fs)
if fs:
    d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2)
    time.sleep(3)
    conf = find_bounds(dump(), r'^Force Sync$')
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
    time.sleep(10)

# scroll up to find Vendors
ven = None
for _ in range(4):
    ven = find_bounds(dump(), r'^Vendors$')
    if ven: break
    d.swipe(540, 900, 540, 1700, duration=0.5); time.sleep(1)
print('vendors:', ven)
if ven:
    d.click((ven[0]+ven[2])//2, (ven[1]+ven[3])//2)
    time.sleep(4)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('vendors screen:', texts[:12])
