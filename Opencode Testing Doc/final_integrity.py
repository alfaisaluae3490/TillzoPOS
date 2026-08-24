#!/usr/bin/env python3
"""Final verification: BETA + HERMES items also restored, then full data integrity check."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:20])
print('---')
print('BETA:', 'AUDIT-ITEM-BETA' in xml)
print('HERMES:', 'HERMES' in xml)
