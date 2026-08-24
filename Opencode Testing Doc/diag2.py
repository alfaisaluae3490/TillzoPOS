#!/usr/bin/env python3
"""home=False even after force-stop+relaunch. Something is on top (dialog?). Dump screen."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],capture_output=True,text=True).stdout
print('resumed:', out.strip()[:110])

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:14])
