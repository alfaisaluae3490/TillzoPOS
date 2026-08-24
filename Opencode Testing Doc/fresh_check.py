#!/usr/bin/env python3
"""DB is SQLCipher-encrypted (DEF-84). Can't inspect directly.
Use the app UI instead: 'View Batches (19.0)' — open batch list to see if new batch shows.
If batches show 19+1=20 but product says 19 => recalc didn't run/persist.
Actually earlier UI said 'View Batches (19.0)' AFTER GRN — meaning sum(batches)=19?
But sheet Product_Batches has rows: BATCH-INITIAL 12.0? No wait that was for AUDIT items.
For HERMES-PROD-001: batch ffc10eb4 qty=1.0. Where's the original 19? hasBatches was false
=> stock 19 was direct current_stock, no batch. After GRN: batch=1, recalc sets stock=1!
But UI showed 19.0... and sheet shows 19.0. So recalc result (1.0) never wrote?

OR: View Batches(19.0) means Flow showed OLD cached value. Test: reopen inventory screen
fresh and check HERMES stock now."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()
subprocess.run([ADB,'-s','emulator-5554','shell','am force-stop com.tillzo.pos'],capture_output=True)
time.sleep(2)
subprocess.run([ADB,'-s','emulator-5554','shell','am start -n com.tillzo.pos/.ui.MainActivity'],capture_output=True)
time.sleep(10)
d.click(871,254); time.sleep(4)
xml = dump(d)
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('HERMES stock after fresh app restart:', m.group(1) if m else '?')
