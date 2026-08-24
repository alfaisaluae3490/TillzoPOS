#!/usr/bin/env python3
"""Debug current state + find Vendors entry point. Maybe it's under 'Inventory' submenu
or a different label like 'Vendor Management'."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:24])
