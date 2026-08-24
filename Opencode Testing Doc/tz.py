#!/usr/bin/env python3
"""App exited again (back-press closed it). Robust helper module for Tillzo navigation.
This will be imported by subsequent test scripts."""
import uiautomator2 as u2, time, re

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

def connect():
    return u2.connect('emulator-5554')

def dump(d):
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

def click_center(d, b):
    d.click((b[0]+b[2])//2, (b[1]+b[3])//2)

def ensure_app(d):
    xml = dump(d)
    if 'TillzoPOS' not in xml:
        d.shell('am start -n com.tillzo.pos/.ui.MainActivity')
        time.sleep(6)
    return dump(d)

def open_menu(d):
    """Open Advanced Options menu from scanner home."""
    d.click(1003, 254)
    time.sleep(3)
    return dump(d)

def menu_scroll_to(d, label):
    """Scroll within open menu until label found. Returns bounds or None."""
    for _ in range(6):
        b = find_bounds(dump(d), '^' + label + '$')
        if b: return b
        # alternate scroll direction based on position attempt
        d.swipe(540, 1500, 540, 800, duration=0.4)
        time.sleep(1)
    return None
