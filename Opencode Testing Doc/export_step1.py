#!/usr/bin/env python3
"""Export + verify in separate steps (openpyxl only in system python)."""
import subprocess, time, os

# step 1: export via chrome (powershell)
subprocess.run(['powershell','-NoProfile','-Command',
    "Start-Process -FilePath 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' -ArgumentList '--profile-directory=\"Profile 3\"','https://docs.google.com/spreadsheets/d/14K8Sw4HeASCL7wsU6HFe_orhB5EYd7XAUE_OLT7BBzU/export?format=xlsx'"],capture_output=True)
time.sleep(25)

xlsx = r'C:/Users/Faisal Khan/Downloads/Faisal Mart — TillzoPOS.xlsx'
print('downloaded:', os.path.exists(xlsx), os.path.getsize(xlsx) if os.path.exists(xlsx) else 0)
