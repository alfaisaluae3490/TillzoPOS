#!/usr/bin/env python3
"""Picker already showing delete_me.csv. Tap it, verify import, then delete via detail view."""
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

# tap the csv file entry
f = find_bounds(dump(), r'delete_me\.csv')
if f:
    d.click((f[0]+f[2])//2, (f[1]+f[3])//2)
    time.sleep(4)

xml = dump()
imported = 'DELETE-ME-TEST' in xml
print('imported & visible:', imported)

if imported:
    # find card + its Delete icon (desc=Delete near card). The list shows Delete icons per row!
    c = find_bounds(xml, r'DELETE-ME-TEST')
    print('card text at:', c)
    # find all desc=Delete buttons, pick the one vertically closest to card
    dels = []
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'content-desc="(Delete)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1, y1, x2, y2 = map(int, b.groups())
            dels.append((y1, (x1, y1, x2, y2)))
    dels.sort()
    if c and dels:
        # choose first Delete below card top
        chosen = None
        for y1, bb in dels:
            if y1 >= c[3] - 50:
                chosen = bb
                break
        print('chosen delete:', chosen)
        if chosen:
            d.click((chosen[0]+chosen[2])//2, (chosen[1]+chosen[3])//2)
            time.sleep(3)
            xml = dump()
            texts = re.findall(r'text="([^"]{1,60})"', xml)
            print('after delete click:', texts[:12])
            # confirmation dialog: find Delete/Yes/OK button
            conf = find_bounds(xml, r'^(Delete|Yes|OK|Confirm)$')
            if conf:
                d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
                time.sleep(4)
    xml = dump()
    gone = 'DELETE-ME-TEST' not in xml
    print('GONE FROM APP:', gone)
