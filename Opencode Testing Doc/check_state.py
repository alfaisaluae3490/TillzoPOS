#!/usr/bin/env python3
"""Check logcat + current state after save attempt."""
import uiautomator2 as u2, re

d = u2.connect('emulator-5554')

out = d.shell('logcat -d -t 400 | grep -iE "FATAL|AndroidRuntime|Exception"')
s = out.output if hasattr(out, 'output') else str(out)
print('EXCEPTIONS:', s.strip()[-600:] if s.strip() else 'none')

xml = d.dump_hierarchy()
print('form open:', 'Edit Product' in xml)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print(texts[:10])

# Is stock now 18 on card (maybe dialog DID save but stayed open)?
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('stock visible:', m.group(1) if m else '?')
