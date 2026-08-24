#!/usr/bin/env python3
"""We're on launcher (app exited). Relaunch, go to vendors, tap row precisely on NAME text."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(8)

# menu -> vendors
d.click(1003, 254); time.sleep(3)
ven = None
for _ in range(4):
    ven = find_bounds(dump(d), r'^Vendors$')
    if ven: break
    d.swipe(540, 1500, 540, 1000, duration=0.4); time.sleep(1)
if ven:
    d.click((ven[0]+ven[2])//2,(ven[1]+ven[3])//2); time.sleep(4)

xml = dump(d)
row = find_bounds(xml, r'AUDIT-VENDOR-1')
print('row:', row)
if row:
    # tap directly on the name text center
    d.click((row[0]+row[2])//2, (row[1]+row[3])//2)
    time.sleep(3)
    xml = dump(d)
    els = d(className='android.widget.EditText')
    fields = [(i, els[i].info.get('text')) for i in range(els.count)]
    print('edit fields:', fields[:8])
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('screen:', texts[:10])
