#!/usr/bin/env python3
"""DELETE worked instantly (no confirm dialog — direct delete with undo?). Verify:
1) sync fires (delete marker upload)
2) sheet no longer has DELETE-ME-TEST after sync."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
time.sleep(10)
log = d.shell('logcat -d -t 500 | grep -E "SyncWorker completed|delete marker|batchUpdate|InventoryUpsert"')
s = log.output if hasattr(log, 'output') else str(log)
print(s.strip()[-800:] if s.strip() else 'no sync logs')

xml = d.dump_hierarchy()
print('still gone from app:', 'DELETE-ME-TEST' not in xml)
# check ALPHA & BETA intact
print('ALPHA safe:', 'AUDIT-ITEM-ALPHA' in xml)
print('BETA safe:', 'AUDIT-ITEM-BETA' in xml)
