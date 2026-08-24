#!/usr/bin/env python3
"""IME disable worked (form alive, save visible). Click Save Expense now at exact bounds.
Check if click registered this time."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
print('form open:', 'Log New Expense' in xml)
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

xml = dump(d)
print('dialog gone:', 'Log New Expense' not in xml)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:12])
