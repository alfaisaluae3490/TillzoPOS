#!/usr/bin/env python3
"""Debug: what does the More options menu contain?"""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
d.click(992, 171)
time.sleep(3)
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('menu texts:', texts[:20])
