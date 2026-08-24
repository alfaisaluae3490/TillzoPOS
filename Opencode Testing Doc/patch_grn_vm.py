#!/usr/bin/env python3
"""_isLoading guard EXISTS (line 166) for double-tap within same VM. Duplicates came
from repeated navigation creating fresh GRN screens (each new VM, loading=false).
CODE FIX: add PO-status guard in saveAndConfirmGRN: abort if po.status == RECEIVED."""
import re

path = r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_c/viewmodel/CreateGrnViewModel.kt'
with open(path, encoding='utf-8') as f:
    src = f.read()

old = """    fun saveAndConfirmGRN(notes: String) {
        val po = _selectedPO.value ?: return
        if (_isLoading.value) return"""

new = """    fun saveAndConfirmGRN(notes: String) {
        val po = _selectedPO.value ?: return
        if (_isLoading.value) return
        // OVERNIGHT-AUDIT FIX (2026-08-24, D7-2): PO already fully received → block new GRN.
        // Pehle har 'Receive Goods' navigation naya GRN bana deta tha, duplicate batches
        // ban jaate the jab user button ko baar-baar dabata tha.
        if (po.status.equals("RECEIVED", ignoreCase = true)) return"""

if old in src:
    src = src.replace(old, new)
    with open(path,'w',encoding='utf-8') as f:
        f.write(src)
    print('PATCHED')
else:
    print('PATTERN NOT FOUND')
