#!/usr/bin/env python3
"""DeltaSync pull DOES respect pending (line 302). So pending wasn't cleared by pull.
Then why 'nothing to sync'? getPendingItems WHERE sync_status='pending'.
recalculateTotalStock sets pending... but did it run BEFORE our force sync?
Timeline: GRN confirm ~01:38 → recalc → sync_status=pending.
Force sync 01:47 → should upload. Log said completed. But sheet still 19!

Maybe the upload DID happen but wrote OLD values? InventoryUpsertUseCase builds values
from item.toSyncMap() — check toSyncMap for current_stock field name mismatch."""
import subprocess
out = subprocess.run(['grep','-n','-A25','fun toSyncMap',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/data/local/entity/InventoryEntity.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1800])
