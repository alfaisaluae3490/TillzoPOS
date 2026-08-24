#!/usr/bin/env python3
"""We're in the file picker still. Relaunch app activity directly."""
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

d.press('back'); time.sleep(1)
d.press('back'); time.sleep(1)
d.shell('am start -n com.tillzo.pos/.ui.MainActivity')
time.sleep(6)

xml = dump()
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('screen:', texts[:8])

# Till tab bottom-left? find 'Till'
till = find_bounds(xml, r'^Till$')
print('Till:', till)

# home screen icons
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print('desc:', repr(cd.group(1)), b.groups()[:2])
