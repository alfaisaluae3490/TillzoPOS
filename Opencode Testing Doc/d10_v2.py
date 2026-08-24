#!/usr/bin/env python3
"""D10 v2: fix dump() arg bug — use local dump() with no args."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

def dump():
    return d.dump_hierarchy()

def click_center(d,b): d.click((b[0]+b[2])//2,(b[1]+b[3])//2)

print('uninstalling...')
subprocess.run([ADB,'-s','emulator-5554','shell','uninstall com.tillzo.pos'],capture_output=True)
time.sleep(3)
print('reinstalling...')
r = subprocess.run([ADB,'-s','emulator-5554','install',
                    r'C:/Users/Faisal Khan/Desktop/Tillzo/app/build/outputs/apk/debug/app-debug.apk'],
                   capture_output=True,text=True,timeout=180)
print('install:', r.stdout.strip()[-15:])
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(9)

for step in range(12):
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    if 'Tap to activate scanner' in xml:
        print('MAIN APP at step', step); break
    allow = find_bounds(xml, r'^Allow$')
    if allow: d.click((allow[0]+allow[2])//2,(allow[1]+allow[3])//2); time.sleep(3); continue
    cont = find_bounds(xml, r'Continue with Google')
    if cont: d.click((cont[0]+cont[2])//2,(cont[1]+cont[3])//2); time.sleep(6); continue
    acc = find_bounds(xml, r'yourtutorial3490')
    if acc: d.click((acc[0]+acc[2])//2,(acc[1]+acc[3])//2); time.sleep(8); continue
    cons = find_bounds(xml, r'Accept & Sync|Accept &amp; Sync')
    if cons: d.click((cons[0]+cons[2])//2,(cons[1]+cons[3])//2); time.sleep(6); continue
    till = find_bounds(xml, r'Open Till')
    if till: d.click((till[0]+till[2])//2,(till[1]+till[3])//2); time.sleep(4); continue
    reg = find_bounds(xml, r'Open Register / Start Shift')
    if reg:
        cash = find_bounds(xml, r'Opening Cash \(Amount\)')
        if cash:
            d.click((cash[0]+cash[2])//2,cash[3]+40); time.sleep(1)
            d.shell('input text 0'); time.sleep(1)
        d.click((reg[0]+reg[2])//2,(reg[1]+reg[3])//2); time.sleep(5); continue
    time.sleep(3)

# inventory -> search D10
d.click(871,254); time.sleep(4)
search = find_bounds(dump(), r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text D10'); time.sleep(3)

xml = dump()
a = 'AUDIT-CUST-A' in xml
b = 'AUDIT-CUST-B' in xml
sa = re.search(r'Stock: ([\d.]+)', xml)
print('RESTORED A:', a, '| B:', b, '| first stock:', sa.group(1) if sa else '?')
