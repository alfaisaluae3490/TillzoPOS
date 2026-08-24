#!/usr/bin/env python3
"""Save button still below fold. Scroll MORE inside dialog (footer buttons are part of
dialog scroll? No — but this dialog seems scrollable incl. footer). Try bigger swipe."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# big swipe up inside form
d.swipe(540, 1700, 540, 700, duration=0.4)
time.sleep(1.5)

xml = dump(d)
save = find_bounds(xml, r'^Save$')
cancel = find_bounds(xml, r'^Cancel$')
print('save:', save, '| cancel:', cancel)

if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after:', texts[:10])
