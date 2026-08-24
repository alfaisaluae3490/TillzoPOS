#!/usr/bin/env python3
"""Fill vendor form: idx0=Name, idx1=Phone. Then Save. Verify list, sync, sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
els = d(className='android.widget.EditText')

# Name (label 'Name *' at y~469 above field 0 y469)
els[0].click(); time.sleep(1)
d.shell('input text AUDIT-VENDOR-1'); time.sleep(0.8)
# Phone
els[1].click(); time.sleep(1)
d.shell('input text 0509998877'); time.sleep(0.8)

# dismiss keyboard
d.press('back'); time.sleep(1.5)

# Save button at footer
xml = dump(d)
save = find_bounds(xml, r'^Save$')
print('save:', save)
if save:
    d.click((save[0]+save[2])//2, (save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
created = 'AUDIT-VENDOR-1' in xml
print('vendor created & visible:', created)
