#!/usr/bin/env python3
"""Google account chooser finally loaded — pick yourtutorial3490@gmail.com."""
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

acc = find_bounds(dump(), r'yourtutorial3490')
print('account at:', acc)
if acc:
    d.click((acc[0]+acc[2])//2, (acc[1]+acc[3])//2)
    print('clicked account')
time.sleep(8)

# handle consent / permissions again if they appear
for i in range(6):
    xml = dump()
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print(i, texts[:6])
    allow = find_bounds(xml, r'^(Allow|Accept & Sync|Continue|Got it|OK)$')
    if allow:
        d.click((allow[0]+allow[2])//2, (allow[1]+allow[3])//2)
        time.sleep(4)
    elif 'Search items' in texts or 'LOW STOCK' in ' '.join(texts):
        print('MAIN APP!')
        break
