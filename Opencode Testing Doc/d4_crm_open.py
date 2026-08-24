#!/usr/bin/env python3
"""D4: CUSTOMERS module test — same CRUD+sync cycle as vendors."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.press('back'); time.sleep(2)
    xml = dump(d)
if 'Tap to activate scanner' not in xml:
    d.shell('am start -n com.tillzo.pos/.ui.MainActivity'); time.sleep(6)

# open menu -> find CRM / Accounts (customers live there) or direct Customers item
d.click(1003, 254); time.sleep(3)

crm = None
for _ in range(5):
    xml = dump(d)
    crm = find_bounds(xml, r'CRM / Accounts')
    if crm: break
    d.swipe(540, 1900, 540, 1200, duration=0.4); time.sleep(1)
print('CRM:', crm)
if crm:
    d.click((crm[0]+crm[2])//2,(crm[1]+crm[3])//2)
    time.sleep(4)

xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('CRM screen:', texts[:16])
