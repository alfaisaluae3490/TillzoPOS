#!/usr/bin/env python3
"""Menu closed after sync. Reopen menu, scroll UP (Vendors is near top), click Vendors."""
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

# close cooldown dialog if still open
xml = dump()
if 'Force Sync' in xml and 'Cancel' in xml:
    cancel = find_bounds(xml, r'^Cancel$')
    if cancel: d.click((cancel[0]+cancel[2])//2,(cancel[1]+cancel[3])//2); time.sleep(2)

d.press('back'); time.sleep(2)  # close menu
d.click(1003, 254); time.sleep(3)

ven = None
for _ in range(5):
    ven = find_bounds(dump(), r'^Vendors$')
    if ven: break
    d.swipe(540, 1000, 540, 1700, duration=0.4); time.sleep(1)
print('vendors:', ven)
if ven:
    d.click((ven[0]+ven[2])//2, (ven[1]+ven[3])//2)
    time.sleep(4)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('vendors screen:', texts[:14])
