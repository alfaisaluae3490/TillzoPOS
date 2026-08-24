#!/usr/bin/env python3
"""BUG #D7-1 FINAL ANALYSIS:
- App local: HERMES stock = 18.0 (correct: 17 batch + 1 GRN)
- Sheet: still 19.0 (stale) — the recalc's pending sync didn't upload the new value.
Why? recalculateTotalStock sets current_stock=18, sync_status=pending.
SyncWorker ran → InventoryUpsertUseCase said 'nothing to sync' at some point?
OR the sheet row got REVERTED by a delta-pull overwriting local (pull conflict).

Check logcat for InventoryUpsert pending count in last syncs."""
import uiautomator2 as u2
d = u2.connect('emulator-5554')
log = d.shell('logcat -d | grep -E "InventoryUpsert|pendingItems|Inventory: " | tail -12')
s = log.output if hasattr(log,'output') else str(log)
print(s.strip())
