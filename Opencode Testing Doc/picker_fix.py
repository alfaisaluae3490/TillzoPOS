#!/usr/bin/env python3
"""Picker grid cells not in accessibility tree (Compose calendar). Use vision fallback:
take screenshot, but screencap blocked? FLAG_SECURE only blocks screenshots of SECURE windows.
Try u2 screenshot."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
img = d.screenshot()
img.save(r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/picker.png')
print('saved picker.png', img.size)

# Also try clicking by grid math: 'August 2026' header; day 1 was listed as a row earlier
# From earlier dump: rows listed dates as text! e.g. 'Saturday, August 1, 2026'. Find that node:
xml = d.dump_hierarchy()
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Saturday, August 15|Friday, August 15|August 15)[^"]*"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1, y1, x2, y2 = map(int, b.groups())
        print('day15 node:', m.group(1), (x1, y1, x2, y2))
        d.click((x1+x2)//2, (y1+y2)//2)
        break
else:
    # click center of visible grid area (day 15 = middle cell)
    d.click(540, 1300)
    print('clicked grid center blindly')
time.sleep(1.5)

# find OK
def fb(xml, pat):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pat, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                return tuple(map(int, b.groups()))
    return None

xml = d.dump_hierarchy()
ok = fb(xml, r'^OK$')
if ok:
    d.click((ok[0]+ok[2])//2, (ok[1]+ok[3])//2)
    print('pressed OK')
time.sleep(2)
xml = d.dump_hierarchy()
print('picker closed:', 'Select date' not in xml)
print('form open:', 'Edit Product' in xml)
