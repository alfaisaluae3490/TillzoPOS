#!/usr/bin/env python3
"""BUG #D3-2: text DOUBLED (input text appended twice — keyboard back re-focused fields).
Fix form: clear both fields, retype once, then Save via footer coords."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
els = d(className='android.widget.EditText')

# clear + refill Name
els[0].click(); time.sleep(1)
d.shell('input keyevent 123')  # END
for _ in range(30): d.shell('input keyevent KEYCODE_DEL')
d.shell('input text AUDIT-VENDOR-1'); time.sleep(0.8)

# clear + refill Phone
els[1].click(); time.sleep(1)
d.shell('input keyevent 123')
for _ in range(20): d.shell('input keyevent KEYCODE_DEL')
d.shell('input text 0509998877'); time.sleep(0.8)

xml = dump(d)
els = d(className='android.widget.EditText')
print('name:', repr(els[0].info.get('text')), '| phone:', repr(els[1].info.get('text')))

# hide kb
d.press('back'); time.sleep(1.5)

# find Save (may need scroll)
save = find_bounds(dump(d), r'^Save$')
if not save:
    d.swipe(540, 1600, 540, 1100, duration=0.4); time.sleep(1.5)
    save = find_bounds(dump(d), r'^Save$')
print('save:', save)
if save:
    d.click((save[0]+save[2])//2, (save[1]+save[3])//2)
    time.sleep(4)

xml = dump(d)
print('vendor in list:', 'AUDIT-VENDOR-1' in xml and 'AUDIT-VENDOR-1AUDIT' not in xml)
