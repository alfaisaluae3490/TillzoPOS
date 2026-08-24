#!/usr/bin/env python3
"""DEF-44 logic exists: increments receivedQty per GRN item & sets RECEIVED when fully
received. Our duplicate batches mean MULTIPLE GRNs each with poItemId blank (unplanned
GRN path) → receivedQty never incremented → PO stuck SENT → Receive button stayed live.

Check CreateGrnScreen line ~251 area: how poItemId is set for existing-product receive.
If blank, that's the bug: planned-PO receive must link poItemId."""
import subprocess
out = subprocess.run(['grep','-n','-B6','-A4','poItemId',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/CreateGrnScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1800])
