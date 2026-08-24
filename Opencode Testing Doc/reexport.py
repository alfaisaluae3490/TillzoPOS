#!/usr/bin/env python3
"""'nothing to sync' = no pending rows locally. So recalculateTotalStock's pending flag
was consumed by an earlier sync that DID upload — meaning sheet SHOULD have 18.
But our export showed 19.0. Possibility: export was cached/stale OR the upload wrote to
a different row. Re-export fresh & recheck. Also check updated_at column."""
import subprocess, time, os
os.remove(r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx') if os.path.exists(r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx') else None
subprocess.run(['powershell','-NoProfile','-Command',
    "Start-Process -FilePath 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' -ArgumentList '--profile-directory=\"Profile 3\"','https://docs.google.com/spreadsheets/d/14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU/export?format=xlsx'"],capture_output=True)
time.sleep(25)
import openpyxl
wb = openpyxl.load_workbook(r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx', data_only=True)
ws = wb['Inventory']
for i, row in enumerate(ws.iter_rows(values_only=True),1):
    for cell in row:
        if cell and isinstance(cell,str) and 'HERMES-PROD-001' in cell:
            print('row',i,[str(c)[:18] for c in row[:14]])
            break
