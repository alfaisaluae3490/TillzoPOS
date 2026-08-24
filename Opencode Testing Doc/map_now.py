#!/usr/bin/env python3
"""Fields after picker: idx0=''(batch?), 3=''(?), 7=''(expiry text?). Batch may have been reset
by dialog reopen? NO — dialog stayed open; the batch field we filled shows '' at idx0?? 
Scroll position changed. Get label-anchored map NOW."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def get_pairs():
    xml = d.dump_hierarchy()
    nodes = re.findall(r'<node[^>]*>', xml)
    labels, edits = [], []
    for n in nodes:
        m = re.search(r'text="([^"]{0,60})"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        cls = re.search(r'class="([^"]+)"', n)
        if not (m and b and cls):
            continue
        t = m.group(1).strip()
        x1, y1, x2, y2 = map(int, b.groups())
        if 'EditText' in cls.group(1):
            edits.append((y1, x1, t))
        elif t:
            labels.append((y1, t))
    pairs = []
    for ey, ex, et in edits:
        cand = [(ly, lt) for ly, lt in labels if ly < ey]
        lbl = max(cand)[1] if cand else '?'
        pairs.append((lbl, et))
    return sorted(pairs)

for p in get_pairs():
    print(p)
