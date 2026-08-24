#!/usr/bin/env python3
"""Robust label->field map: dump raw hierarchy pairs (label TextView y + EditText below)."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def get_pairs():
    xml = d.dump_hierarchy()
    nodes = re.findall(r'<node[^>]*>', xml)
    labels = []   # (y1, text)
    edits = []    # (y1, x1, text, node_bounds)
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
        # nearest label ABOVE this edit field
        cand = [(ly, lt) for ly, lt in labels if ly < ey]
        lbl = max(cand)[1] if cand else '?'
        pairs.append((lbl, et, ex, ey))
    return sorted(pairs, key=lambda p: p[3])

# scroll to the zone between stock row and batch section
for scroll_y in [(540, 1500, 540, 1000)]:
    d.swipe(*scroll_y, duration=0.3)
    time.sleep(1.5)

for p in get_pairs():
    print(p)
