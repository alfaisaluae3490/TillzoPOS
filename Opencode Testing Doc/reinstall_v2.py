#!/usr/bin/env python3
"""Clean up orphaned AUDIT-CAT-X row in sheet (delete marker was lost). Use Sheets API via
gspread-style REST with OAuth? Simpler: re-add category locally, let sync run, then delete
again — new code defers deletion on failure and properly deletes on success."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
import subprocess

subprocess.run([ADB,'-s','emulator-5554','install','-r',
                r'C:/Users/Faisal Khan/Desktop/Tillzo/app/build/outputs/apk/debug/app-debug.apk'],
               capture_output=True,text=True)
print('installed')

d.shell('am start -n com.tillzo.pos/.ui.MainActivity')
time.sleep(8)

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        cd = re.search(r'content-desc="([^"]*)"', n)
        val = (m.group(1) if m else '') + '|' + (cd.group(1) if cd else '')
        if re.search(pattern, val):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

# handle permissions if fresh install cleared them
for _ in range(3):
    xml = dump()
    allow = find_bounds(xml, r'^Allow$')
    if not allow: break
    d.click((allow[0]+allow[2])//2,(allow[1]+allow[3])//2); time.sleep(2)

# sign-in screen?
xml = dump()
if 'Continue with Google' in xml:
    c = find_bounds(xml, r'Continue with Google')
    if c:
        d.click((c[0]+c[2])//2,(c[1]+c[3])//2); time.sleep(6)
    acc = find_bounds(dump(), r'yourtutorial3490')
    if acc:
        d.click((acc[0]+acc[2])//2,(acc[1]+acc[3])//2); time.sleep(8)
    # consent
    cons = find_bounds(dump(), r'Accept & Sync|Accept &amp; Sync')
    if cons:
        d.click((cons[0]+cons[2])//2,(cons[1]+cons[3])//2); time.sleep(6)
    till = find_bounds(dump(), r'Open Till')
    if till:
        d.click((till[0]+till[2])//2,(till[1]+till[3])//2); time.sleep(4)
        cash = find_bounds(dump(), r'Opening Cash \(Amount\)')
        if cash:
            d.click((cash[0]+cash[2])//2, cash[3]+40); time.sleep(1)
            d.shell('input text 0'); time.sleep(1)
        reg = find_bounds(dump(), r'Open Register / Start Shift')
        if reg:
            d.click((reg[0]+reg[2])//2,(reg[1]+reg[3])//2); time.sleep(5)

print('signed in & till opened:', 'Tap to activate scanner' in dump() or 'Search items' in dump())
