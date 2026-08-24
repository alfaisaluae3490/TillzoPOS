#!/usr/bin/env python3
"""D7 results: GRN_Headers synced (CONFIRMED) but:
1. GRN header sync_status='pending' in sheet (should be 'synced'?)
2. HERMES-PROD-001 stock still 19.0 — GRN added batch but product totalStock NOT updated!
Check Product_Batches tab for new batch."""
import openpyxl
wb = openpyxl.load_workbook(r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx', data_only=True)
ws = wb['Product_Batches']
rows = list(ws.iter_rows(values_only=True))
print('Product_Batches rows:')
for r in rows[-4:]:
    print([str(c)[:16] if c is not None else '' for c in r[:12]])
