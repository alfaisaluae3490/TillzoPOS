#!/usr/bin/env python3
"""updateLinkedPOStatus exists — check its logic (maybe it sets PARTIALLY_RECEIVED/RECEIVED)."""
import subprocess
out = subprocess.run(['grep','-n','-A25','fun updateLinkedPOStatus',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/domain/usecase/grn/ConfirmGrnUseCase.kt'],
                     capture_output=True,text=True)
print(out.stdout[:1600])
