#!/usr/bin/env python3
"""Check what's actually on screen now — full texts + classes."""
import uiautomator2 as u2, re

d = u2.connect('emulator-5554')
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:20])
import subprocess
print(subprocess.run(['C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe',
                      '-s','emulator-5554','shell','dumpsys activity activities | grep ResumedActivity'],
                     capture_output=True, text=True).stdout)
