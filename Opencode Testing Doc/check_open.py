#!/usr/bin/env python3
"""The form seems to have closed (only status bar text). Check state & reopen if needed,
then do a careful label-anchored batch fill."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('current screen:', texts[:8])
print('form open:', 'Edit Product' in xml)
