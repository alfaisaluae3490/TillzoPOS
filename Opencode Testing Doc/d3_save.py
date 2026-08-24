#!/usr/bin/env python3
"""Name + Phone filled. Scroll dialog to reveal Save footer, tap it WITHOUT back-press."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# scroll INSIDE dialog (swipe up small) to bring Save into view — but avoid dismissing:
d.swipe(540, 1500, 540, 1000, duration=0.3)
time.sleep(1.5)

xml = dump(d)
save = find_bounds(xml, r'^Save$')
print('save:', save)
if save and save[1] < 2100:
    d.click((save[0]+save[2])//2, (save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after save:', texts[:12])
print('vendor in list:', 'AUDIT-VENDOR-1' in xml and 'No vendors' not in xml)
