#!/usr/bin/env python3
"""App not on home. Diagnose: print resumed activity + screen texts, then handle."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],capture_output=True,text=True).stdout
print('resumed:', out.strip()[:120])

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print('texts:', texts[:12])
