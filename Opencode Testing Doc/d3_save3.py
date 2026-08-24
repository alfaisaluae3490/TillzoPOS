#!/usr/bin/env python3
"""Save/Cancel not found — they're at the very bottom of scrollable form. Scroll to END
with multiple swipes then dump ALL texts to find them."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
for _ in range(4):
    d.swipe(540, 1800, 540, 600, duration=0.35)
    time.sleep(1)

xml = dump(d)
save = find_bounds(xml, r'^Save$')
print('save:', save)
if save:
    # ensure fully visible (y2 <= 2100)
    d.click((save[0]+save[2])//2, min((save[1]+save[3])//2, 2000))
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after:', texts[:10])
