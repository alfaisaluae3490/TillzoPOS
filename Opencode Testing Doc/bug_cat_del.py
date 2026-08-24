#!/usr/bin/env python3
"""BUG #D2-1: Category delete not propagating to sheet (still 'synced' status in sheet row).
Check CategoryUpsertUseCase pending-deletion logic + local DB state."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')
log = d.shell('logcat -d | grep -iE "CategoryUpsert|pending deletion|Categories" | tail -12')
s = log.output if hasattr(log,'output') else str(log)
print(s.strip())
