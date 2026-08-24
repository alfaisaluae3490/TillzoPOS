#!/usr/bin/env python3
"""Permission dialog persisting — click Allow with retries & different matching."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

# Try clicking Allow button by resource-id or text via u2 selector directly
for attempt in range(5):
    xml = dump()
    if 'Allow TillzoPOS' not in xml:
        print('permission dialog gone at attempt', attempt)
        break
    # try multiple selectors
    btn = d(text='Allow')
    if btn.exists:
        btn.click()
        print('clicked Allow (text)')
    else:
        btn = d(textContains='Allow')
        if btn.exists:
            btn.click()
            print('clicked Allow (contains)')
        else:
            d.click(540, 1500)  # fallback tap center-ish
            print('fallback tap')
    time.sleep(3)

xml = dump()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('now:', texts[:10])
