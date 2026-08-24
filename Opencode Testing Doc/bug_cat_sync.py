#!/usr/bin/env python3
"""BUG INVESTIGATION: AUDIT-CAT-X created locally but NOT synced to sheet.
Check sync_status of the new category + CategoryUpsertUseCase pending logic."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

# check logcat for CategoryUpsert details
log = d.shell('logcat -d | grep -iE "CategoryUpsert|Categories" | tail -10')
s = log.output if hasattr(log, 'output') else str(log)
print(s.strip())
