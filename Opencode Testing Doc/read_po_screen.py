#!/usr/bin/env python3
"""CODE FIX for D7-2: PO Receive double-execution.
In PODetailScreen the 'Receive Goods'/'Confirm' buttons must be disabled while a GRN
is in flight, and ConfirmGrnUseCase must no-op if PO already RECEIVED.
Read relevant PODetailScreen code first."""
import subprocess
out = subprocess.run(['grep','-n','-B4','-A12','Receive Goods',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:2500])
