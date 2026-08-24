#!/usr/bin/env python3
"""Check VM errors and dialog state."""
import uiautomator2 as u2

d = u2.connect('emulator-5554')
out = d.shell('logcat -d | grep -iE "InventoryCrudVM|Failed to save"')
s = out.output if hasattr(out, 'output') else str(out)
print('VM ERRORS:', s.strip()[-500:] if s.strip() else 'none')

xml = d.dump_hierarchy()
print('form open:', 'Edit Product' in xml)
