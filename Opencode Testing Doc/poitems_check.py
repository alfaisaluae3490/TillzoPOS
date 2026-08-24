#!/usr/bin/env python3
"""Patch canReceive to also require not-fully-received items.
Find PO items received state: check what data PODetail has — poItems list? Grep."""
import subprocess
out = subprocess.run(['grep','-n','poItems\\|items',
                      r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'],
                     capture_output=True,text=True)
print(out.stdout[:900])
