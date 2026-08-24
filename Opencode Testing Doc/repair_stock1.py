#!/usr/bin/env python3
"""Sheet STILL shows stock 19.0 while app shows 18.0 and 'nothing to sync'.
This means the local row is marked 'synced' with value 18, but the last actual
upload wrote 19 (or never wrote 18). The pending upload got lost — likely consumed
by the delta-pull conflict OR the sync ran between recalc-pending and sheet write
with 'nothing to sync' because ANOTHER earlier sync already marked it synced after
uploading 19.

FIX APPROACH (code): in InventoryUpsertUseCase, when uploading updates, also compare
stock_qty; but real issue = lost wakeup. Pragmatic repair: force one row to pending
via a no-op edit through the app UI: open HERMES-PROD-001 edit, retype same stock 18,
save -> triggers UpdateProductUseCase(pending) -> sync uploads 18.
Then verify sheet."""
import sys, os, time, re, subprocess
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
d = connect()

# inventory list -> tap HERMES card -> edit form -> set stock 18 -> save
d.click(871,254); time.sleep(4)
xml = dump(d)
search = find_bounds(xml, r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)

xml = dump(d)
card = find_bounds(xml, r'HERMES-PROD-001')
print('card:', card)
if card:
    d.click((card[0]+card[2])//2,(card[1]+card[3])//2)
    time.sleep(3)

xml = dump(d)
edit = find_bounds(xml, r'^Edit$|content.*Edit')
print('detail texts:', [t for t in re.findall(r'text="([^"]{1,40})"',xml) if t.strip()][:14])
