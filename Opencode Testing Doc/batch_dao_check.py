#!/usr/bin/env python3
"""BUG #D7-1 ANALYSIS COMPLETE:
GRN item inventoryAction defaults to 'PENDING'. ConfirmGrnUseCase's when() handles
NEW_PRODUCT/NEW_ITEM, ADD_BATCH, UPDATE_BATCH, PENDING. PENDING IS handled (fallback
creates batch + recalcs). The batch WAS created (Product_Batches row exists with 1.0).
recalculateTotalStock WAS called... but stock still 19.

WAIT — recalc sets current_stock = sum(batches). Product had NO batches before
(hasBatches=false, current=19 from manual entry). GRN adds batch qty=1.0.
New total = 0 + 1 = 1?? But sheet shows product stock 19.0 unchanged...

Actually the batch row shows productId '8cea4687' = HERMES-PROD-001 ✓ qty=1.0 ✓
So local DB should now be 19+1=20 via recalculate? Unless recalc only counts
isActive && !isDeleted batches and the sum REPLACED 19 with 1!
Sheet shows 19.0 though — meaning recalc result never synced OR recalc happened
before batch insert committed.

KEY: the app UI showed 'View Batches (19.0)' — so locally it thinks batches total 19.
The new batch (qty 1) isn't being counted! Maybe isActive flag or productId mismatch.
The batch row col 'productId'=8cea4687 matches product. Hmm — but View Batches shows 19.0...

This needs code-level fix verification. Check recalculateTotalStock query conditions."""
import subprocess
out = subprocess.run(['grep','-n','-B2','-A8','fun getActiveBatchesForProduct|getActiveBatches',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/data/local/dao/ProductBatchDao.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1200])
