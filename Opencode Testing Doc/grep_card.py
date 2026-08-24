#!/usr/bin/env python3
"""No edit icon in a11y. Check VendorCard code lines 190-210 to see how edit icon is
rendered (icon only visible on expanded card?)."""
import subprocess
out = subprocess.run(['grep','-n','-A6','IconButton(onClick = onEdit)',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/VendorManagementScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:800])
