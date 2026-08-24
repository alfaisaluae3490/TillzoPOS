#!/usr/bin/env python3
"""Find canReceive definition."""
import subprocess
out = subprocess.run(['grep','-n','canReceive =\\|val canReceive',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout)
