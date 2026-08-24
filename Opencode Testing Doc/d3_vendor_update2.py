#!/usr/bin/env python3
"""Edit Vendor dialog STILL open — Save at footer. Scroll & click Save (like vendor add)."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# scroll to bottom
for _ in range(3):
    d.swipe(540, 1800, 540, 600, duration=0.35)
    time.sleep(1)

xml = d.dump_hierarchy()
save = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int, b.groups()))
print('save bounds:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = d.dump_hierarchy()
print('dialog closed:', 'Edit Vendor' not in xml)
print('phone shown:', '0501112222' in xml)
