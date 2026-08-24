#!/usr/bin/env python3
"""EditText count changed (keyboard covering?). Re-dump fresh each step."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

def get_fields():
    els = d(className='android.widget.EditText')
    return els, [(i, els[i].info.get('text')) for i in range(els.count)]

els, fields = get_fields()
print('fields:', fields)

# fix name field (the doubled one)
for i, t in fields:
    if t and 'AUDIT-VENDOR-1AUDIT' in t:
        e = els[i]
        e.click(); time.sleep(1)
        d.shell('input keyevent 123')
        for _ in range(40): d.shell('input keyevent KEYCODE_DEL')
        d.shell('input text AUDIT-VENDOR-1'); time.sleep(1)
        print('name fixed:', repr(els[i].info.get('text')))
        break

# fix phone (doubled)
els, fields = get_fields()
for i, t in fields:
    if t and '05099988770509998877' in t:
        e = els[i]
        e.click(); time.sleep(1)
        d.shell('input keyevent 123')
        for _ in range(25): d.shell('input keyevent KEYCODE_DEL')
        d.shell('input text 0509998877'); time.sleep(1)
        print('phone fixed:', repr(els[i].info.get('text')))
        break
