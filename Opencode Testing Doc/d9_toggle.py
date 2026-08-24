#!/usr/bin/env python3
"""D9: Settings enumerated. Test 'Block Negative Stock' toggle flip -> verify persists.
Then D10: multi-item reinstall sync integrity (add 2 more items, reinstall, check both)."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)

# find Block Negative Stock switch state
def get_switch_state(label):
    for n in re.findall(r'<node[^>]*>', dump(d)):
        m = re.search(r'text="([^"]*)"', n)
        if m and label in m.group(1):
            pass
    # switches: find sibling Switch of the label row — simpler: search checked state on Switch class near
    return None

before = None
for n in re.findall(r'<node[^>]*>', xml):
    cls = re.search(r'class="android.view.View"', n)
    chk = re.search(r'checked="(true|false)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if chk and b:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1500 < y1 < 2100:
            before = chk.group(1)
print('a switch state near block-negative:', before)

# toggle it via tapping its row area — find label bounds first
lbl = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Block Negative Stock"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        lbl = tuple(map(int,b.groups()))
print('label:', lbl)
if lbl:
    d.click(950, lbl[1]+20)  # switch is right side
    time.sleep(2)

xml = dump(d)
after = None
for n in re.findall(r'<node[^>]*>', xml):
    chk = re.search(r'checked="(true|false)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if chk and b:
        x1,y1,x2,y2 = map(int,b.groups())
        if 1500 < y1 < 2100:
            after = chk.group(1)
print('switch after tap:', after, '| changed:', before != after)

# toggle back to original
if before is not None and after is not None and before != after:
    d.click(950, lbl[1]+20) if lbl else None
    time.sleep(2)
    print('restored original')
