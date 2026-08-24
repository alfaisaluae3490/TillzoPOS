#!/usr/bin/env python3
"""Add Category button = IconButton with contentDescription 'Add Category'.
Type name, click that button (NOT enter)."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

# input focused already from debug script. Type the name:
d.shell('input text AUDIT-CAT-X')
time.sleep(1)
xml = dump()
print('typed visible:', 'AUDIT-CAT-X' in xml)

add_btn = d(description='Add Category')
print('Add Category btn:', add_btn.exists)
if add_btn.exists:
    add_btn.click()
    time.sleep(2)

xml = dump()
print('category in list now:', 'AUDIT-CAT-X' in xml)
