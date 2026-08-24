#!/usr/bin/env python3
"""BUG #D1-1 CONFIRMED: AUDIT-CAT-X vanished — ENTER submit didn't commit.
Root cause hypothesis: 'input keyevent 66' (ENTER) on Compose TextField doesn't trigger
onValueChange commit / save action. Need explicit ADD/save button OR IME action.
Test: type text then look for add/submit button; use u2 clear+type+commit."""
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

# manager is open. Tap New Category input, type, then look for a button
f = find_bounds(dump(), r'New Category')
if f:
    d.click((f[0]+f[2])//2, (f[1]+f[3])//2)
    time.sleep(1.5)

# use uiautomator2's send_keys which uses AdbKeyboard or commits properly
el = d(className='android.widget.EditText')
target = None
for i in range(el.count):
    info = el[i].info
    if not info.get('text'):
        target = i
        break
print('empty input idx:', target)

xml = dump()
btns_after = []
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Add|Save|Create|Submit|\+)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        btns_after.append((m.group(1), tuple(map(int, b.groups()))))
print('submit buttons:', btns_after)
