#!/usr/bin/env python3
"""App on launcher. The 1003,254 menu tap works ONLY from scanner home. Relaunch app first,
wait longer for camera init, THEN tap menu. Add state assertions at every step."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(10)

xml = dump(d)
on_home = 'Tap to activate scanner' in xml
print('on scanner home:', on_home)

if not on_home:
    # maybe permission or dialog — print texts
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print('texts:', texts[:8])

# tap menu
d.click(1003, 254)
time.sleep(3)
xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml)
print('after menu tap:', texts[:16])
