#!/usr/bin/env python3
"""All fields filled, no errors, still open. Check what validation could fail:
maybe mfgDate or something in onSave lambda throwing. Watch logcat during click."""
import uiautomator2 as u2, time

d = u2.connect('emulator-5554')
d.shell('logcat -c')

# click save
btn = d(description='Save Product')
print('exists:', btn.exists)
btn.click()

for i in range(6):
    time.sleep(1)
    xml = d.dump_hierarchy()
    if 'Edit Product' not in xml:
        print(f'CLOSED after {i+1}s')
        break
else:
    print('NOT closed after 6s')

out = d.shell('logcat -d | grep -vE "chatty|OpenGLRenderer|libc|Compatibil|GraphicsEnv|AppLifeCycle|GameIntervention|NetworkSecurityConfig|zygote" | tail -25')
s = out.output if hasattr(out, 'output') else str(out)
print(s.strip()[-1200:])
