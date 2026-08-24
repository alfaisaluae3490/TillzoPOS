#!/usr/bin/env python3
"""On launcher. Full clean nav: launch -> inventory -> search -> card edit icon."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(9)
xml = dump(d)
print('home:', 'Tap to activate scanner' in xml)

d.click(871,254); time.sleep(4)

search = find_bounds(dump(d), r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)
    d.shell('input keyevent 111'); time.sleep(1)

xml = dump(d)
edit = find_bounds(xml, r'^Edit$')
print('Edit icon:', edit)
