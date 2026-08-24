#!/usr/bin/env python3
"""OOPS: (166,1178) is the MFG DATE picker (calendar opened). Dismiss it.
Layout insight: 'Batch Number*' label y1031 -> the two Views at y1178 are MFG/EXPIRY date pickers.
So BATCH input must be an EditText ABOVE the label... but we saw none. OR batch field = the
EditText showing '30'?? No that's expAlert (label below it).
REALITY CHECK from code line 798: OutlinedTextField(batch) IS a normal EditText. It might be
the empty EditText at idx3 y823 seen earlier (before stock row). Let me dismiss calendar,
scroll UP a bit, and list fields with their LABELS by proximity."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

# dismiss date picker
d.click(920, 1800)  # likely outside/cancel zone? safer: press back
d.press('back')
time.sleep(2)
xml = d.dump_hierarchy()
print('picker closed:', 'Select date' not in xml)

# scroll up slightly to find batch EditText between GTINs and pricing
d.swipe(540, 1000, 540, 1400, duration=0.3)
time.sleep(1.5)

xml = d.dump_hierarchy()
nodes = re.findall(r'<node[^>]*>', xml)
# print label + following editable pairs
last_label = None
for n in nodes:
    m = re.search(r'text="([^"]{1,60})"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    cls = re.search(r'class="([^"]+)"', n)
    if not (m and b and cls):
        continue
    t = m.group(1).strip()
    x1, y1, x2, y2 = map(int, b.groups())
    is_edit = 'EditText' in cls.group(1)
    if t:
        last_label = t
        continue
    if is_edit:
        print('EDITTEXT at y=%d <- label above: %s' % (y1, last_label))
