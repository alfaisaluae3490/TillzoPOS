#!/usr/bin/env python3
"""Expense didn't save (form dismissed by my big swipe = gesture dismiss).
RETRY: open form, tap category chip FIRST via scroll-into-view, fill fields,
then tap 'Save Expense' WITHOUT any large swipes. Save Expense was at y~1489 last time
after small scroll — use SMALL scrolls only."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()

# open Add Expense
fab = None
for n in re.findall(r'<node[^>]*>', dump(d)):
    cd = re.search(r'content-desc="Add Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b:
        fab = tuple(map(int,b.groups()))
if not fab:
    fab = (926, 1862, 992, 1928)
d.click((fab[0]+fab[2])//2,(fab[1]+fab[3])//2)
time.sleep(3)

xml = dump(d)
print('form open:', 'Log New Expense' in xml)

# select category: Rent chip
rent = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Rent)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        rent = tuple(map(int,b.groups()))
print('rent chip:', rent)
if rent:
    d.click((rent[0]+rent[2])//2,(rent[1]+rent[3])//2)
    time.sleep(1)

# amount + desc
els = d(className='android.widget.EditText')
if els.count >= 2:
    els[0].click(); time.sleep(1)
    d.shell('input text 45.75'); time.sleep(0.8)
    els[1].click(); time.sleep(1)
    d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)
    print('fields filled')

# small scroll to expose Save Expense button
d.swipe(540, 1500, 540, 1150, duration=0.25); time.sleep(1)
save = None
for n in re.findall(r'<node[^>]*>', dump(d)):
    m = re.search(r'text="Save Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
