#!/usr/bin/env python3
"""Full D10 sequence: launch -> inventory -> import d10_items.csv -> verify ->
force sync -> uninstall/reinstall -> sign-in -> verify both items restored."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(9)

# inventory
d.click(871,254); time.sleep(4)

# import icon
ic = find_bounds(dump(d), r'Import CSV')
print('icon:', ic)
if ic:
    d.click((ic[0]+ic[2])//2,(ic[1]+ic[3])//2); time.sleep(3)

# picker: Downloads -> d10_items.csv
dl = find_bounds(dump(d), r'^Downloads$')
if dl:
    d.click((dl[0]+dl[2])//2,(dl[1]+dl[3])//2); time.sleep(3)
f = find_bounds(dump(d), r'd10_items\.csv')
print('file:', f)
if f:
    d.click((f[0]+f[2])//2,(f[1]+f[3])//2); time.sleep(5)

xml = dump(d)
a = 'AUDIT-CUST-A' in xml
b = 'AUDIT-CUST-B' in xml
print('imported A:', a, '| B:', b)
