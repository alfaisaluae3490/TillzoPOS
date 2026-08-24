#!/usr/bin/env python3
"""App icons (Menu/Inventory/Till) not in a11y tree — they're Compose-drawn.
Use known coordinates from earlier session: Menu=(1003,254), Inventory=(871,254).
Tap Menu directly and dump."""
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

# The scanner home screen — menu icon was at top-right area. Try tapping where 'More options' was
d.click(992, 171)
time.sleep(3)
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after tap:', texts[:12])

fs = find_bounds(xml, r'Force Sync')
print('Force Sync visible:', fs)
