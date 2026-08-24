#!/usr/bin/env python3
"""Debug why batch label never found: dump ALL texts with bounds after scrolling to bottom."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

# scroll hard to bottom
for _ in range(2):
    d.swipe(540, 1700, 540, 1100, duration=0.3)
    time.sleep(1)

xml = d.dump_hierarchy()
nodes = re.findall(r'<node[^>]*>', xml)
for n in nodes:
    m = re.search(r'text="([^"]{1,60})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b and m.group(1).strip():
        print(repr(m.group(1)), (b.group(1)))
