#!/usr/bin/env python3
"""Check current screen state after reinstall flow."""
import uiautomator2 as u2, re
d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:14])
