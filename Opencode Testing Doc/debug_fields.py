#!/usr/bin/env python3
"""Debug: after finding label, why didn't the EditText get filled? Show all fields + label pos."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

xml = d.dump_hierarchy()
label_pos = None
for n in re.findall(r'<node[^>]*>', xml):
    if re.search(r'text="Batch Number \*"', n):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        label_pos = tuple(map(int, b.groups()))
print('label:', label_pos)

els = d(className='android.widget.EditText')
for i in range(els.count):
    info = els[i].info
    b = info.get('bounds')
    print(i, repr(info.get('text')), b)
