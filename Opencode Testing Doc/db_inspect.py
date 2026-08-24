#!/usr/bin/env python3
"""Check ProductBatchEntity defaults: isActive default? The GRN-created batch shows
isActive=1 in sheet (col 10 = '1.0'). Hmm sheet col 10='1.0' might be isActive.
Actually cols: [id, productId, barcode, batchNum?, mfg?, expiry?, qty=1.0, cost=50, sell=0, 1.0(isActive?), 0.0]
So batch IS active with qty 1.0. Then View Batches(19.0) means the app's Flow didn't refresh,
OR recalculate ran but current_stock=19 was re-written by a later sync pull (delta from sheet
had old 19). Sheet still shows 19 because product row wasn't marked pending after recalc?
recalculateTotalStock sets sync_status=pending ✓ and we force-synced...

Deeper check: query local DB directly via app debug? run-as works on emulator.
Use adb to dump the inventory table via sqlite3 through debug apk."""
import subprocess
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
# create DB copy readable
r1 = subprocess.run([ADB,'-s','emulator-5554','shell',
    'run-as com.tillzo.pos cat /data/data/com.tillzo.pos/databases/tillzo_pos_db > /data/local/tmp/db.db'],
    capture_output=True,text=True)
r2 = subprocess.run([ADB,'-s','emulator-5554','pull','/data/local/tmp/db.db',
    r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/db_now.db'],capture_output=True,text=True)
print(r2.stdout.strip()[-80:])
import sqlite3
con = sqlite3.connect(r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/db_now.db')
cur = con.cursor()
try:
    cur.execute("SELECT item_name, current_stock, totalStock, hasBatches FROM Inventory WHERE item_name LIKE 'HERMES-PROD-001'")
    print('PRODUCT:', cur.fetchall())
    cur.execute("SELECT batchNumber, stockQty, isActive, isDeleted FROM product_batches WHERE productId=(SELECT system_row_id FROM Inventory WHERE item_name='HERMES-PROD-001')")
    print('BATCHES:', cur.fetchall())
except Exception as e:
    print('ERR:', e)
