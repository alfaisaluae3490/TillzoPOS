#!/usr/bin/env python3
"""Post-reinstall: grant permissions, complete sign-in, wait for sync, verify data restore."""
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

# STEP 1: permissions dialog(s) — Allow
for i in range(4):
    xml = dump()
    allow = find_bounds(xml, r'^(Allow|While using the app)$')
    if allow:
        d.click((allow[0]+allow[2])//2, (allow[1]+allow[3])//2)
        print('granted permission', i+1)
        time.sleep(2.5)

xml = dump()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('after perms:', texts[:10])

# STEP 2: sign-in flow — look for Google sign-in button
c = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'(?:text|content-desc)="([^"]*)"', n)
    if m and re.search(r'Sign [Ii]n|Continue|Get Started|Google', m.group(1)):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            x1,y1,x2,y2 = map(int,b.groups())
            c = ((x1+x2)//2,(y1+y2)//2)
            print('signin element:', m.group(1), c)
            break
if c:
    d.click(*c)
    time.sleep(5)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print('next step:', texts[:12])
    # account picker: choose yourtutorial3490
    acc = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and 'yourtutorial3490' in m.group(1):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                acc = ((x1+x2)//2,(y1+y2)//2)
    if acc:
        d.click(*acc)
        time.sleep(6)
        print('account selected')
