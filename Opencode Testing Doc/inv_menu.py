#!/usr/bin/env python3
"""Inventory list screen: menu icon here is 'More options' at (992,171) per earlier dump.
Click THAT (not 1003,254). Then look for sync/refresh options."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

d.click(992, 171)
time.sleep(3)
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('inventory menu:', texts[:12])
