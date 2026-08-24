#!/usr/bin/env python3
"""BUG #D7-1 CONFIRMED: GRN received 1.0 PC but product stock still 19.0 (not 20).
'View Batches (19.0)' — batch total didn't update either.
ROOT CAUSE: GRN flow creates batch but doesn't call recalculateTotalStock OR
the batch was created with wrong productId linkage.
Check GRN code path: where does PO receive create batches?"""
import subprocess
out = subprocess.run(['grep','-rn','-l','Receive Goods|receiveGoods|GRN',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/',
                      '--include=*.kt'], capture_output=True,text=True)
print(out.stdout)
