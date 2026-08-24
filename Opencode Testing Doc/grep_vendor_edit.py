#!/usr/bin/env python3
"""Tap on row didn't open edit. Check code: how does vendor edit work? Long-press or
edit icon? Inspect Vendors screen code."""
import subprocess
out = subprocess.run(['grep','-rn','editVendor|onVendorClick|VendorEdit|fun.*[Vv]endor',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/',
                      '--include=*.kt'], capture_output=True, text=True)
print(out.stdout[:1500])
