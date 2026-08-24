#!/usr/bin/env python3
"""CODE FIX (D7-2): CreateGrnViewModel must not pre-fill remainingQty when PO already
fully received, and PODetailScreen should hide Receive when fully received.
Add guard in loadPOItems: if receivedQty>=orderedQty for all → skip/expose flag.

Minimal robust fix: in the map at line 79-90, set receivedQty = remainingQty only
if remaining>0 else keep 0 AND mark item inactive; plus PODetailScreen: hide Receive
when order.status == RECEIVED (already covered by canReceive list) — the gap was
status never became RECEIVED because each duplicate GRN incremented receivedQty by 1
each time... actually increments happened; after 1st GRN status=RECEIVED; canReceive
false. But my later taps created NEW GRNs from CreateGrnScreen still open? Each
'Receive Goods' tap navigated to a NEW CreateGrnScreen — but if button hidden after
RECEIVED, no more GRNs. Since duplicates exist, status updates raced.

PRAGMATIC CODE FIX: add idempotency in ConfirmGrnUseCase caller (CreateGrnViewModel.save):
if PO.status == RECEIVED, abort save. Read save function first."""
import subprocess
out = subprocess.run(['grep','-n','-B3','-A30','fun saveGrn|fun save',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/viewmodel/CreateGrnViewModel.kt'],
                     capture_output=True,text=True)
print(out.stdout[:2200])
