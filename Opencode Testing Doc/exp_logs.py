#!/usr/bin/env python3
"""Expense not in list. Check logcat: did Save Expense click register? Any validation?
Also check DB via sheet sync later. First inspect logcat."""
import uiautomator2 as u2
d = u2.connect('emulator-5554')
log = d.shell('logcat -d -t 600 | grep -iE "expense|ExpenseUpsert|InventoryCrudVM|Failed"')
s = log.output if hasattr(log,'output') else str(log)
print(s.strip()[-800:] if s.strip() else 'no expense logs')
