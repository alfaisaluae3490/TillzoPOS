#!/usr/bin/env python3
"""P4: REINSTALL DATA-RETENTION TEST
1. Capture pre-state (inventory count, sales count from sheet + app)
2. Force full sync first so everything is on cloud
3. UNINSTALL app completely
4. REINSTALL same APK
5. Login/sign-in, trigger sync
6. Verify data restored (ALPHA stock 18, BETA 30, HERMES items)
NOTE: app uses sheet as source of truth; local DB is rebuilt from cloud pull."""
import uiautomator2 as u2, time, re, subprocess

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

def dump():
    return d.dump_hierarchy()

def sh(*args):
    return subprocess.run([ADB, '-s', 'emulator-5554'] + list(args), capture_output=True, text=True).stdout

# STEP 1: force-sync first via menu to ensure latest data on sheet
# (we just synced after delete; skip extra sync)

# STEP 2: capture pre-install state
xml = dump()
pre_alpha = 'AUDIT-ITEM-ALPHA' in xml
print('PRE: ALPHA in app:', pre_alpha)

# STEP 3: uninstall
print('uninstalling...')
r = sh('uninstall', 'com.tillzo.pos')
print('uninstall:', r.strip())
time.sleep(3)

# STEP 4: reinstall
print('reinstalling...')
r = subprocess.run([ADB, '-s', 'emulator-5554', 'install',
                    r'C:/Users/Faisal Khan/Desktop/Tillzo/app/build/outputs/apk/debug/app-debug.apk'],
                   capture_output=True, text=True, timeout=180)
print('install:', r.stdout.strip()[-20:])

# STEP 5: launch
sh('shell', 'am', 'start', '-n', 'com.tillzo.pos/.ui.MainActivity')
time.sleep(10)
xml = dump()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('post-reinstall screen:', texts[:8])

# check login state — if sign-in needed, we use stored google account picker
if any('Sign' in t or 'sign' in t for t in texts):
    print('SIGN-IN REQUIRED — attempting Google sign-in flow...')
    # tap sign-in button if visible
    c = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(r'Sign [Ii]n|Continue with Google|Get Started', m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                c = ((x1+x2)//2,(y1+y2)//2)
                print('signin btn:', m.group(1), c)
                break
    if c:
        d.click(*c)
        time.sleep(5)
        xml = dump()
        texts = re.findall(r'text="([^"]{1,60})"', xml)
        print('after signin tap:', texts[:10])
