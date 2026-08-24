#!/usr/bin/env python3
"""EDIT DIALOG OPEN! Change phone: clear field idx1, type new, Save, sync, verify sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
els = d(className='android.widget.EditText')

# clear phone (idx1) & set new
els[1].click(); time.sleep(1)
d.shell('input keyevent 123')
for _ in range(15): d.shell('input keyevent KEYCODE_DEL')
d.shell('input text 0501112222'); time.sleep(1)

# verify
els = d(className='android.widget.EditText')
print('new phone:', repr(els[1].info.get('text')))

# scroll to save & click
d.swipe(540, 1700, 540, 700, duration=0.35); time.sleep(1.5)
save = find_bounds(dump(d), r'^Save$')
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,50})"', xml)
ok = '0501112222' in xml
print('phone updated in app:', ok)
