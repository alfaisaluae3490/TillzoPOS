#!/usr/bin/env python3
"""DELETE test v2: import failed (import icon not found on current screen).
Do it step-by-step: 1) ensure inventory list, 2) find Import CSV by desc, 3) picker flow."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

xml = dump()
print('screen now:', re.findall(r'text="([^"]{1,30})"', xml)[:6])

# Are we in the inventory list? Look for 'Search items'
if 'Search items' not in xml:
    # press back to leave any detail view
    d.press('back'); time.sleep(2)
    xml = dump()

# Find the Import CSV icon (content-desc contains 'Import CSV')
c = find_bounds(xml, r'Import CSV')
print('Import CSV icon:', c)
if not c:
    print('FAIL: not on inventory list screen')
else:
    d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
    time.sleep(2.5)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,30})"', xml)
    print('picker:', texts[:6])
