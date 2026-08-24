#!/usr/bin/env python3
"""Wastage module functional (dashboard + items list). D8 core flows verified.
Now D9: Settings audit — open menu -> Settings, enumerate all options & test key ones
(block screen capture toggle already verified). Quick enumeration pass."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.press('back'); time.sleep(2)
xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)

# Settings via menu (saw earlier at bottom) — scroll down in menu
d.click(1003, 254); time.sleep(3)
st = None
for _ in range(6):
    xml = dump(d)
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Settings)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            if y1 > 100: st = (x1,y1,x2,y2)
    if st: break
    d.swipe(540, 1900, 540, 1100, duration=0.4); time.sleep(1.2)

print('settings:', st)
if st:
    d.click((st[0]+st[2])//2,(st[1]+st[3])//2)
    time.sleep(4)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('settings screen:', texts[:20])
