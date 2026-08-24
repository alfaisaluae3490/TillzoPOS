#!/usr/bin/env python3
"""INTERESTING STATE: 'AUDIT-VENDOR-1' text + 'No vendors yet' — the name text is the
SEARCH FIELD content! The vendor was never saved; dialog got dismissed by back-press
(which I used to hide keyboard). BUG UX: back dismisses whole form losing data.
Retry cleanly: FAB -> fill -> DON'T press back -> scroll via dialog swipe -> Save."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds, click_center

d = connect()

# clear search field first
xml = dump(d)
search = find_bounds(xml, r'Search vendors')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2)
    time.sleep(1)
    # clear it
    d.shell('input keyevent 123')
    for _ in range(20): d.shell('input keyevent KEYCODE_DEL')
    d.press('back'); time.sleep(1.5)

# open Add Vendor FAB
fab = find_bounds(dump(d), r'Add Vendor')
print('fab:', fab)
if fab:
    click_center(d, fab)
    time.sleep(3)

# fill name
els = d(className='android.widget.EditText')
if els.count >= 1:
    els[0].click(); time.sleep(1)
    d.shell('input text AUDIT-VENDOR-1'); time.sleep(1)
els = d(className='android.widget.EditText')
print('name:', repr(els[0].info.get('text')) if els.count else 'no fields')

# phone: find next EditText BELOW name (scroll inside dialog with small swipe)
d.swipe(540, 1300, 540, 1100, duration=0.3); time.sleep(1.2)
els = d(className='android.widget.EditText')
for i in range(els.count):
    t = els[i].info.get('text')
    if not t and els[i].info.get('bounds')['top'] > 400:
        els[i].click(); time.sleep(1)
        d.shell('input text 0509998877'); time.sleep(0.8)
        break
els = d(className='android.widget.EditText')
print('fields now:', [(i, els[i].info.get('text')) for i in range(els.count)])
