#!/usr/bin/env python3
"""CONFIRMED ROOT CAUSE D7-2:
canReceive = status in [SENT, PARTIALLY_RECEIVED]. After full receive the status
stays SENT (never transitions to RECEIVED), so 'Receive Goods' remains available →
repeated GRNs → duplicate 1.0 batches.

CODE FIX: after GRN confirm, if all items fully received, set PO status=RECEIVED.
Check ConfirmGrnUseCase end for status update; add it."""
import subprocess
out = subprocess.run(['grep','-n','-B2','-A8','Mark GRN as CONFIRMED',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/domain/usecase/grn/ConfirmGrnUseCase.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1200])
