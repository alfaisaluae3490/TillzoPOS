#!/usr/bin/env python3
"""Search poItemId across GRN creation code paths."""
import subprocess
out = subprocess.run(['grep','-rn','poItemId',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/',
                      '--include=*.kt'], capture_output=True,text=True)
print(out.stdout[:1200] or 'NOT FOUND in module_c')
out2 = subprocess.run(['grep','-rn','poItemId =',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/'],
                      capture_output=True,text=True,shell=False)
print(out2.stdout[:800])
