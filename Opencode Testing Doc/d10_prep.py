#!/usr/bin/env python3
"""Switch stays false — but is THIS the FLAG_SECURE switch (Block Screenshots)?
The label 'Block Negative Stock' is a DIFFERENT setting (blocks selling w/o stock).
Its switch being OFF is expected default. Not a bug!
The screenshot-block switch we tested earlier was separate & worked.
D9 conclusion: settings enumerated; toggles render correctly. Moving to D10:
add 2 more items via CSV, uninstall/reinstall, verify restore."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()
# write CSV with 2 new items
csv = """name,sku,barcode,category,cost_price,selling_price,stock_qty,unit
AUDIT-CUST-A,SKU-D10A,,Testing,3.00,7.00,9,pcs
AUDIT-CUST-B,SKU-D10B,,Testing,4.00,8.00,11,box
"""
with open(r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/d10_items.csv','w') as f:
    f.write(csv)

import subprocess
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
subprocess.run([ADB,'-s','emulator-5554','push',
                r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/d10_items.csv',
                '/sdcard/Download/d10_items.csv'],capture_output=True)
print('csv pushed')

# go inventory -> import csv
xml = dump(d)
if 'Search items' not in xml:
    d.press('back'); time.sleep(2)
d.click(1003,254) if False else None
# import icon at (1003,254)? earlier Import CSV icon was at (970-1036, 221-287) on inventory screen
ic = find_bounds(dump(d), r'Import CSV')
print('import icon:', ic)
if ic:
    d.click((ic[0]+ic[2])//2,(ic[1]+ic[3])//2); time.sleep(3)
