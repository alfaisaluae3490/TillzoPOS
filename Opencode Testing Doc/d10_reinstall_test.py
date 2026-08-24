#!/usr/bin/env python3
"""D10: uninstall + reinstall + verify both D10 items restore from cloud."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

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

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        val = m.group(1) if m else ''
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

def click_center(d,b): d.click((b[0]+b[2])//2,(b[1]+b[3])//2)

# handle sign-in flow again
for step in range(10):
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    if 'Tap to activate scanner' in xml:
        print('MAIN APP reached at step', step); break
    allow = find_bounds(xml, r'^Allow$')
    if allow: click_center(d,allow); time.sleep(3); continue
    cont = find_bounds(xml, r'Continue with Google')
    if cont: click_center(d,cont); time.sleep(6); continue
    acc = find_bounds(xml, r'yourtutorial3490')
    if acc: click_center(d,acc); time.sleep(8); continue
    cons = find_bounds(xml, r'Accept & Sync|Accept &amp; Sync')
    if cons: click_center(d,cons); time.sleep(6); continue
    till = find_bounds(xml, r'Open Till')
    if till: click_center(d,till); time.sleep(4); continue
    reg = find_bounds(xml, r'Open Register / Start Shift')
    if reg:
        cash = find_bounds(xml, r'Opening Cash \(Amount\)')
        if cash:
            d.click((cash[0]+cash[2])//2,cash[3]+40); time.sleep(1)
            d.shell('input text 0'); time.sleep(1)
        click_center(d,reg); time.sleep(5); continue
    time.sleep(3)

# inventory -> search D10 items
d.click(871,254); time.sleep(4)
search = find_bounds(xml, r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text D10'); time.sleep(3)

xml = dump(d)
a = 'AUDIT-CUST-A' in xml
b = 'AUDIT-CUST-B' in xml
sa = re.search(r'Stock: ([\d.]+)', xml)
print('RESTORED A:', a, '| B:', b)
