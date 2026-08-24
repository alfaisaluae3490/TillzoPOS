#!/usr/bin/env python3
"""BUG #D7-1 CONFIRMED: GRN received 1.0 PC into a batch (ffc10eb4) but
HERMES-PROD-001 totalStock/current_stock still 19.0 in sheet — recalculateTotalStock
not triggered OR not synced. Check app UI: what does inventory show for stock?
Earlier app showed 'Stock: 19.0'. After GRN it should be 20.0.
Check app now."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(8)
xml = dump(d)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print('app stocks:', [t for t in texts if 'Stock' in t])
print('HERMES in view:', any('HERMES' in t for t in texts))
