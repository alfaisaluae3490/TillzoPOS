#!/usr/bin/env python3
"""Date picker still open, day 15 not visible (calendar shows Aug 2026 grid but '15' text
may be inside day cells with different structure). Scroll/inspect the picker grid."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
# find all numeric texts in picker
nums = []
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(\d{1,2})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1, y1, x2, y2 = map(int, b.groups())
        nums.append((int(m.group(1)), x1, y1))
print('numeric cells:', sorted(set(nums))[:40])
