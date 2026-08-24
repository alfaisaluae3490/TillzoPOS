#!/usr/bin/env python3
"""Picker activity is STILL resumed despite force-stop. Kill ALL app tasks via am, then
relaunch TillzoPOS fresh (its task includes the picker since startActivityForResult)."""
import uiautomator2 as u2, time, re, subprocess

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

# finish the picker task
subprocess.run([ADB,'-s','emulator-5554','shell','am task lock nothing'], capture_output=True)
subprocess.run([ADB,'-s','emulator-5554','shell','input keyevent KEYCODE_BACK'],capture_output=True)
time.sleep(1)
subprocess.run([ADB,'-s','emulator-5554','shell','input keyevent KEYCODE_BACK'],capture_output=True)
time.sleep(1)
subprocess.run([ADB,'-s','emulator-5554','shell','input keyevent KEYCODE_HOME'],capture_output=True)
time.sleep(2)

out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],
                     capture_output=True,text=True).stdout
print('resumed:', out.strip())

# now launch tillzo
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(6)

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,30})"', xml)
print('screen:', texts[:6])
