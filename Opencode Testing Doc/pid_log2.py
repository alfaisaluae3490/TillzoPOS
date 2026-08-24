#!/usr/bin/env python3
"""Watch app pid 7679 logs while clicking Save."""
import uiautomator2 as u2, time

d = u2.connect('emulator-5554')
d.shell('logcat -c')

btn = d(description='Save Product')
btn.click()
time.sleep(3)

out = d.shell('logcat -d --pid=7679')
s = out.output if hasattr(out, 'output') else str(out)
lines = s.strip().split('\n')
print(f'total app log lines: {len(lines)}')
for l in lines[-25:]:
    print(l[:150])
