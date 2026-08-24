#!/usr/bin/env python3
"""Cancel button not found by exact text? Find its bounds from dump and click via coords."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

xml = dump()
# print all clickable nodes with text
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b and m.group(1).strip():
        print(repr(m.group(1)), b.groups())
