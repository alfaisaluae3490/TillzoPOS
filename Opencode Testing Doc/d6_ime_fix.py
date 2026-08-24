#!/usr/bin/env python3
"""BACK closed the form again (dialog dismissible). New approach: DON'T hide kb.
Instead scroll the DIALOG slightly so Save Expense rises ABOVE keyboard, then tap.
Keyboard top ~y1250 when up. Save was at y1572 (covered). After small dialog scroll
it should appear at y~1100-1300 area... but kb covers 1250+.
BEST: use ESC? No — closes. Use ACTION_CLOSE_IME via adb: 'input keyevent 111' closes
whole dialog too. Hmm — earlier inventory edit form survived back! Different dialogs.

ALTERNATIVE: tap save THROUGH the keyboard? Not possible.
REAL FIX: fill desc BEFORE amount so focus ends on desc; then use IME action/scroll.
Or: set fields, then use u2 to click save coordinates that are computed AFTER kb hides
via d.shell('cmd input_method disable')... ime disable closes IME but keeps dialog!
We used 'ime disable com.google.android.inputmethod.latin' earlier successfully."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# open form again
fab = (926, 1862, 992, 1928)
d.click((fab[0]+fab[2])//2,(fab[1]+fab[3])//2); time.sleep(3)
xml = dump(d)
print('form:', 'Log New Expense' in xml)

# select Rent chip
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Rent)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        d.click((x1+x2)//2,(y1+y2)//2); break
time.sleep(1)

# fill both fields
els = d(className='android.widget.EditText')
els[0].click(); time.sleep(1)
d.shell('input text 45.75'); time.sleep(0.8)
els[1].click(); time.sleep(1)
d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)

# DISABLE IME entirely (closes kb without back-press)
d.shell('ime disable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')
time.sleep(2)
xml = dump(d)
print('form alive after ime-disable:', 'Log New Expense' in xml)

save = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    d.click((save[0]+save[2])//2,(save[1]+save[3])//2)
    time.sleep(4)

# RE-ENABLE IME for future tests
d.shell('ime enable com.google.android.inputmethod.latin/.com.android.inputmethod.latin.LatinIME')

xml = dump(d)
print('expense saved:', 'AUDIT-EXP-1' in xml or '45.75' in xml)
