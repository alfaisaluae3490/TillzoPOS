#!/usr/bin/env python3
"""Menu didn't open. The Advanced menu icon in earlier session was at (1003,254) on the
SCANNER home. Try that exact coord; also try edge-swipe drawer fallback."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# try (1003,254)
d.click(1003, 254)
time.sleep(3)
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('try1:', texts[:10])

if 'Force Sync' not in xml:
    # edge-swipe drawer
    d.swipe(5, 1200, 700, 1200, duration=0.4)
    time.sleep(3)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('try2 (drawer):', texts[:12])

fs = find_bounds(xml, r'Force Sync')
print('FS:', fs)
