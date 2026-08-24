#!/usr/bin/env python3
"""D1: INVENTORY micro-field CRUD cycle test.
Test: add category via Category Manager, then use it; edit low-stock threshold (micro-field);
verify sheet sync for each op."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# open inventory module
xml = dump()
if 'Search items' not in xml:
    d.click(871, 254); time.sleep(3)

# open category manager via dropdown flow: tap Add Item -> category dropdown -> Manage Categories
add = find_bounds(dump(), r'Add Item')
print('Add Item:', add)
if add:
    d.click((add[0]+add[2])//2, (add[1]+add[3])//2)
    time.sleep(3)

xml = dump()
cat = find_bounds(xml, r'Select Main Category|Main Category')
print('category field:', cat)
