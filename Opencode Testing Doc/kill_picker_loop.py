#!/usr/bin/env python3
"""The documentsui PICKER is resumed (it's in Tillzo's task). Kill picker task properly:
finish its task via 'am kill' won't work; use input BACK until gone, THEN force-stop app
and relaunch. Loop back presses up to 6 times checking resumed activity."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

def resumed():
    out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],capture_output=True,text=True).stdout
    return out

for i in range(8):
    r = resumed()
    if 'documentsui' not in r:
        print('picker gone at press', i)
        break
    d.press('back'); time.sleep(1.5)
else:
    subprocess.run([ADB,'-s','emulator-5554','shell','am force-stop com.google.android.documentsui'],capture_output=True)
    time.sleep(2)

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(9)
xml = dump(d)
print('home:', 'Tap to activate scanner' in xml)
