#!/usr/bin/env python3
"""App exited to launcher. Relaunch and complete sign-in step by step with state checks."""
import uiautomator2 as u2, time, re, subprocess

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

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

def click_center(b): d.click((b[0]+b[2])//2,(b[1]+b[3])//2)

subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(8)

for round_ in range(8):
    xml = dump()
    texts = re.findall(r'text="([^"]{1,60})"', xml)
    print(round_, texts[:6])

    # main app reached?
    if 'Tap to activate scanner' in xml:
        print('MAIN APP REACHED')
        break

    # permission
    allow = find_bounds(xml, r'^Allow$')
    if allow: click_center(allow); time.sleep(3); continue

    # sign-in
    cont = find_bounds(xml, r'Continue with Google')
    if cont: click_center(cont); time.sleep(6); continue

    acc = find_bounds(xml, r'yourtutorial3490')
    if acc: click_center(acc); time.sleep(7); continue

    cons = find_bounds(xml, r'Accept & Sync|Accept &amp; Sync')
    if cons: click_center(cons); time.sleep(6); continue

    till = find_bounds(xml, r'Open Till')
    if till: click_center(till); time.sleep(4); continue

    cash = find_bounds(xml, r'Opening Cash \(Amount\)')
    reg = find_bounds(xml, r'Open Register / Start Shift')
    if reg:
        if cash:
            d.click((cash[0]+cash[2])//2, cash[3]+40); time.sleep(1)
            d.shell('input text 0'); time.sleep(1)
        click_center(reg); time.sleep(5); continue

    time.sleep(3)

xml = dump()
print('FINAL:', re.findall(r'text="([^"]{1,40})"', xml)[:8])
