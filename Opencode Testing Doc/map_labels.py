#!/usr/bin/env python3
"""Map ALL EditTexts to their nearest label above. Then fill batch correctly."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def map_fields():
    xml = d.dump_hierarchy()
    nodes = re.findall(r'<node[^>]*>', xml)
    items = []  # (y, kind, text, x1,y1,x2,y2)
    for n in nodes:
        m = re.search(r'text="([^"]{0,60})"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        cls = re.search(r'class="([^"]+)"', n)
        if not (m and b and cls):
            continue
        t = m.group(1).strip()
        x1, y1, x2, y2 = map(int, b.groups())
        kind = 'EDIT' if 'EditText' in cls.group(1) else ('TXT' if t else None)
        if kind:
            items.append((y1, x1, kind, t))
    # sort by y
    items.sort()
    result = []
    last_label = '?'
    for y, x, kind, t in items:
        if kind == 'TXT' and t:
            last_label = t
        elif kind == 'EDIT':
            result.append((last_label, t, (x, y)))
    return result

# scroll to middle of form
d.swipe(540, 1400, 540, 900, duration=0.3)
time.sleep(1.5)
for row in map_fields():
    print(row)
