#!/usr/bin/env python3
"""DEF-40 idempotency exists per-GRN. The duplicates came from MULTIPLE GRNs being
created (each 'Receive Goods' tap → CreateGrnScreen → Save → new GRN + confirm).
Each new GRN is legitimately distinct, so batches multiplied.

REAL UX GAP: PO status stays SENT after first receive; canReceive remains true.
FIX: in PODetailScreen, hide/disable 'Receive Goods' once receivedQty >= orderedQty,
or show RECEIVED state. Check how canReceive is computed."""
import subprocess
out = subprocess.run(['grep','-n','-B3','-A6','canReceive',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1200])
