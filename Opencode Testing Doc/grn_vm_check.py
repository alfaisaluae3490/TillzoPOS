#!/usr/bin/env python3
"""CreateGrnViewModel line 84 sets poItemId = poItem.poItemId — planned path OK.
So duplicates likely from MY repeated automation clicks each creating a fresh GRN
(each legitimately linked to poItemId, incrementing receivedQty... but then PO should
have hit RECEIVED and hidden button).

receivedQty increments: GRN#1 → received=1.0 = ordered 1.0 → RECEIVED. But PO stayed
SENT because later GRNs were created BEFORE status update propagated? Each new GRN
screen entry resets? Regardless: multiple GRNs happened due to my repeated taps.

The remaining REAL issues to fix in code:
A) PODetailScreen 'Receive Goods' button not disabled after first tap navigation.
B) Data repair: remove duplicate batches.

Given time budget, do data repair via UI edit of product stock to correct total,
and add a code guard: in CreateGrnViewModel, block creating a new GRN if PO already
fully received (check PO status)."""
import subprocess
out = subprocess.run(['grep','-n','-B5','-A15','poItemId = poItem.poItemId',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/viewmodel/CreateGrnViewModel.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1500])
