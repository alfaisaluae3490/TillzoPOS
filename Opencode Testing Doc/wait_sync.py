#!/usr/bin/env python3
"""Force sync via menu then verify sheet."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

# wait for auto-sync (UpdateProductUseCase triggers manual sync automatically)
time.sleep(8)
log = d.shell('logcat -d -t 400 | grep -E "SyncWorker completed|InventoryUpsertUseCase|batchUpdate"')
s = log.output if hasattr(log, 'output') else str(log)
print(s.strip()[-600:] if s.strip() else 'no sync yet')
