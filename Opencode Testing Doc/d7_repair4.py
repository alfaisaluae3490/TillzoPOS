#!/usr/bin/env python3
"""Icon tap opened search-edit (name field 'HERMES-PROD-001') not product edit.
Escape and use the card's Edit icon properly: earlier vendor edit worked via icon at
right side. For inventory card, Edit/Delete/Print icons were at y~1007 area under card.
Search view: find content-desc Edit near card."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
# clear wrong state: clear the search-edit field & escape
d.press('back'); time.sleep(1.5)
d.press('back'); time.sleep(1.5)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,40})"', xml) if t.strip()]
print(texts[:10])

# re-search HERMES
search = find_bounds(xml, r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)
    d.shell('input keyevent 111'); time.sleep(1)

xml = dump(d)
# find Edit desc on card row
edit = None
for n in re.findall(r'<node[^>]*>', xml):
    cd = re.search(r'content-desc="(Edit)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if cd and b:
        x1,y1,x2,y2 = map(int,b.groups())
        edit = (x1,y1,x2,y2)
        print('Edit icon:', edit)
        break
