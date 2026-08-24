#!/usr/bin/env python3
"""BUG #D7-1 CONFIRMED ROOT CAUSE:
GRN flow: ConfirmGrnUseCase → recalculateTotalStock → sets current_stock=18,
sync_status='pending'. But THEN the DeltaSync PULL ran (remote=19 from sheet) and
OVERWROTE local 18 back to 19, clearing pending! Classic pull-overwrites-push race.

Evidence: app showed 18.0 after restart (fresh read = 18?)... wait no, app shows 18
but sheet shows 19. If pull overwrote, app would show 19 too.

Actually app=18, sheet=19 means: recalc set 18+pending, but NO sync uploaded it
(all syncs say 'nothing to sync' — pending was cleared!). The delta-pull at 01:03-01:52
matched remote/local timestamps and something marked it synced WITHOUT uploading.

Look at InventoryUpsertUseCase: after successful upsert it calls markSynced.
But 'nothing to sync' means getPendingItems() returned EMPTY — so something else
cleared the pending flag. Suspect: DeltaSyncManager pull writes row with synced status.

FIX NEEDED: In DeltaSyncManager pull path, don't overwrite rows that have
sync_status='pending' (local changes win). Check code."""
import subprocess
out = subprocess.run(['grep','-n','-B3','-A15','pending',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/data/sync/options/delta/DeltaSyncManager.kt'],
                     capture_output=True,text=True)
print(out.stdout[:2000])
