#!/usr/bin/env python3
"""Fill customer form: Name=AUDIT-CUSTOMER-1, Phone=0505554433, Save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
els = d(className='android.widget.EditText')

els[0].click(); time.sleep(1)
d.shell('input text AUDIT-CUSTOMER-1'); time.sleep(0.8)
els[1].click(); time.sleep(1)
d.shell('input text 0505554433'); time.sleep(0.8)

# scroll dialog to reveal Save
for _ in range(3):
    d.swipe(540, 1800, 540, 600, duration=0.35); time.sleep(0.8)

xml = d.dump_hierarchy()
save = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = d.dump_hierarchy()
print('customer saved:', 'AUDIT-CUSTOMER-1' in xml)
