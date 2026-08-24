#!/usr/bin/env python3
"""Batch Number label visible with NO EditText below it?! The batch field is the
'30' node? No — 30 = expAlert. Wait: 'Batch Number *' label at some Y, then '30'
(Expiry Alert default) — where's batch input and expiry input?? They may be
read-only/picker fields rendered as TextViews, not EditTexts!
Check nodes near Batch Number label."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
nodes = re.findall(r'<node[^>]*>', xml)

batch_lbl = None
for n in nodes:
    if re.search(r'text="Batch Number \*"', n):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        batch_lbl = tuple(map(int, b.groups()))
print('label:', batch_lbl)

# show ALL nodes (any class) between label top-50 and label bottom+400
for n in nodes:
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    m = re.search(r'text="([^"]{0,60})"', n)
    c = re.search(r'class="([^"]+)"', n)
    clk = re.search(r'clickable="(\w+)"', n)
    ed = re.search(r'editable="(\w+)"', n)
    if b and batch_lbl:
        x1, y1, x2, y2 = map(int, b.groups())
        if y1 >= batch_lbl[1] - 20 and y1 <= batch_lbl[3] + 400 and x2 > x1:
            print((x1, y1, x2, y2), repr(m.group(1) if m else ''), (c.group(1).split('.')[-1] if c else ''), 'clk=' + (clk.group(1) if clk else '?'), 'edit=' + (ed.group(1) if ed else '?'))
