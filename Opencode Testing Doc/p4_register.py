#!/usr/bin/env python3
"""Open Register form visible: enter opening cash (0), confirm, then inventory restore check."""
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

# tap opening cash field & enter 0
f = find_bounds(dump(), r'Opening Cash \(Amount\)')
if f:
    d.click((f[0]+f[2])//2, f[3] + 40)
    time.sleep(1.5)
    d.shell('input text 0')
    time.sleep(1)

btn = find_bounds(dump(), r'Open Register / Start Shift|Open Register &amp; Start Selling')
print('register btn:', btn)
if btn:
    d.click((btn[0]+btn[2])//2, (btn[1]+btn[3])//2)
    print('clicked open register')
time.sleep(6)

# go to inventory
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('now:', texts[:8])
d.click(871, 254)
time.sleep(4)

# wait for pull sync restore
for i in range(14):
    time.sleep(5)
    xml = dump()
    if 'AUDIT-ITEM-ALPHA' in xml:
        print(f'RESTORED after ~{(i+1)*5}s')
        break
else:
    # force sync via menu
    print('forcing sync via menu...')
    d.click(1003, 254)  # menu icon
    time.sleep(3)
    for _ in range(3):
        d.swipe(540, 1900, 540, 700, duration=0.4)
        time.sleep(0.8)
    fs = find_bounds(dump(), r'Force Sync')
    if fs:
        d.click((fs[0]+fs[2])//2, (fs[1]+fs[3])//2)
        print('force sync clicked')

xml = dump()
print('ALPHA:', 'AUDIT-ITEM-ALPHA' in xml)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('stock:', m.group(1) if m else '?')
