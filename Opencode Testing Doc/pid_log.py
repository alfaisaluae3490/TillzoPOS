#!/usr/bin/env python3
"""Check if the Save click actually reaches the app process: watch tillzo logcat tags only."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
d.shell('logcat -c')

btn = d(description='Save Product')
btn.click()
time.sleep(3)

out = d.shell('logcat -d 9503:*:0')  # pid filtered? Actually use --pid
out2 = d.shell(f'logcat -d --pid={9503} | tail -30')
s = out2.output if hasattr(out2, 'output') else str(out2)
print(s.strip()[-1500:] if s.strip() else 'no app logs at all')

# Also check current focused window — maybe clicking desc node opened something invisible
xml = d.dump_hierarchy()
print('form open:', 'Edit Product' in xml)
