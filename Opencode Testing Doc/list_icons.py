#!/usr/bin/env python3
"""App main screen (scanner view) loaded. Find the Advanced Menu icon on THIS screen.
Earlier it was at 1003,254 with desc 'Menu'. List all desc icons again."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="([^"]{1,40})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b and cd.group(1).strip():
        print(repr(cd.group(1)), b.groups())
