#!/usr/bin/env python3
"""Patch canReceive: add fully-received guard using items list.
items is State<List<POItemEntity>> collected at line 41, but canReceive at line 56 is
BEFORE items declared? Line 41 < 56 so items available. Patch:
val canReceive = order.status in listOf("SENT","PARTIALLY_RECEIVED") &&
    items.none { it.receivedQty >= it.orderedQty }"""
import re

path = r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos/ui/inventory/module_b/PODetailScreen.kt'
with open(path, encoding='utf-8') as f:
    src = f.read()

old = '    val canReceive = order.status in listOf("SENT", "PARTIALLY_RECEIVED")'
new = ('    // OVERNIGHT-AUDIT FIX (2026-08-24, D7-2): hide Receive when all items fully\n'
       '    // received — pehle status SENT par hi button zinda reh jata tha → duplicate GRNs.\n'
       '    val canReceive = order.status in listOf("SENT", "PARTIALLY_RECEIVED") &&\n'
       '        items.none { it.receivedQty >= it.orderedQty }')

if old in src and 'D7-2' not in src:
    src = src.replace(old, new)
    with open(path,'w',encoding='utf-8') as f:
        f.write(src)
    print('PATCHED')
else:
    print('SKIP: pattern missing or already patched')
