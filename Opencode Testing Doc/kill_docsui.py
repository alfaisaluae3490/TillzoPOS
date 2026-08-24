#!/usr/bin/env python3
"""That's still the system picker menu?! Check resumed activity - maybe picker activity is
still on top. Use am to bring main activity forward, then verify which screen."""
import uiautomator2 as u2, time, re, subprocess

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

out = subprocess.run([ADB,'-s','emulator-5554','shell','dumpsys activity activities | grep -i ResumedActivity'],
                     capture_output=True,text=True).stdout
print(out.strip())

# force stop the picker (documentsui)
r = subprocess.run([ADB,'-s','emulator-5554','shell','am force-stop com.google.android.documentsui'],
                   capture_output=True,text=True)
print('picker killed')

# bring app forward
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(6)

xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,30})"', xml)
print('screen:', texts[:6])
