#!/usr/bin/env python3
"""Menu icon click not opening menu. Check current screen + find right menu entry point."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,40})"', xml)
print('screen:', texts[:10])

# all content-desc elements (icons)
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print('desc:', repr(cd.group(1)), b.groups())
