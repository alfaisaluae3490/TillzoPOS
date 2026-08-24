#!/usr/bin/env python3
"""Switch still false — this 'Block Negative Stock' switch might genuinely be OFF by
user setting and taps not registering (same Compose a11y issue). BUT earlier session
verified FLAG_SECURE toggle works. This settings screen may need row-tap instead of
switch-tap. Try tapping the LABEL ROW left side."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(400, 1287); time.sleep(2)
xml = dump(d)
for n in re.findall(r'<node[^>]*>', xml):
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    chk = re.search(r'checked="(true|false)"', n)
    if b and chk:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1200 < y1 < 1400 and x1 > 800:
            print('switch:', chk.group(1))
            break
