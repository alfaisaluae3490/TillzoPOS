#!/usr/bin/env python3
"""No expense logs = Save Expense click may have missed. The button was at (710,1515).
But wait — after filling fields the keyboard was UP, covering the button. The click
at 710,1515 hit the KEYBOARD instead. Solution: hide kb first (small back), THEN click.
Retry with keyboard handling."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# open Add Expense
fab = (926, 1862, 992, 1928)
d.click((fab[0]+fab[2])//2,(fab[1]+fab[3])//2)
time.sleep(3)
xml = dump(d)
print('form:', 'Log New Expense' in xml)

# category Rent
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Rent)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        d.click((x1+x2)//2,(y1+y2)//2); break
time.sleep(1)

# fill fields
els = d(className='android.widget.EditText')
if els.count >= 2:
    els[0].click(); time.sleep(1)
    d.shell('input text 45.75'); time.sleep(0.8)
    els[0].click(); time.sleep(0.5)  # refocus for desc? no - desc is idx1
# scroll small to bring desc + save into view WITHOUT closing kb issue:
d.swipe(540, 1500, 540, 1250, duration=0.25); time.sleep(1)

els = d(className='android.widget.EditText')
print('fields now:', [(i, els[i].info.get('text')) for i in range(els.count)])
if els.count >= 2:
    # description field might be idx1
    if not els[1].info.get('text'):
        els[1].click(); time.sleep(1)
        d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)

# hide keyboard properly: BACK press hides IME without dismissing dialog (kb is up)
d.press('back'); time.sleep(1.5)
xml = dump(d)
print('form still open after kb-hide:', 'Log New Expense' in xml)

save = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
