#!/usr/bin/env python3
"""Second code fix: PODetailScreen — hide Receive Goods when fully received.
canReceive currently: status in [SENT, PARTIALLY_RECEIVED]. Add receivedQty>=orderedQty check.
Read lines 50-60 first."""
import subprocess
out = subprocess.run(['grep','-n','-B6','val canReceive',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:900])
