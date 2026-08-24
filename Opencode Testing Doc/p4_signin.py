#!/usr/bin/env python3
"""Sign-in: Continue with Google -> pick yourtutorial3490 account -> wait for main app."""
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

# STEP 1: Continue with Google
c = find_bounds(dump(), r'Continue with Google')
print('continue btn:', c)
if c:
    d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
    time.sleep(6)

# STEP 2: account picker
xml = dump()
acc = find_bounds(xml, r'yourtutorial3490')
print('account:', acc)
if acc:
    d.click((acc[0]+acc[2])//2, (acc[1]+acc[3])//2)
    time.sleep(8)

# STEP 3: any consent/continue screens
for i in range(4):
    xml = dump()
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print(f'step {i}:', texts[:8])
    nxt = find_bounds(xml, r'^(Continue|Allow|Got it|OK|Done)$')
    if nxt:
        d.click((nxt[0]+nxt[2])//2, (nxt[1]+nxt[3])//2)
        time.sleep(4)
    elif 'Edit Product' in xml or 'Search items' in xml or 'LOW STOCK' in xml:
        print('MAIN APP REACHED')
        break
