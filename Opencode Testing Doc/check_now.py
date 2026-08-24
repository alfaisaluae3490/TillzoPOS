#!/usr/bin/env python3
"""Save-by-desc NOT found => dialog closed already (date picker OK press may have saved?) or scrolled.
Check current state."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:12])
print('form open:', 'Edit Product' in xml)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('card stock:', m.group(1) if m else '?')
