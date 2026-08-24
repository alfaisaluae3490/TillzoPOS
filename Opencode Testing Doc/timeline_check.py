#!/usr/bin/env python3
"""The delete flow looks correct. Check: was the delete done BEFORE the category synced
(add at 23:06, delete clicked ~23:31)? If deleted while sync_status was still 'pending',
deleteCategory sets is_deleted=1 + pending — getPendingSyncDeleted should catch it.
But sheet shows 'synced' status — maybe the ADD sync ran AFTER local delete, re-adding it?
Check logcat timeline around 23:27-23:46 for the category."""
import uiautomator2 as u2

d = u2.connect('emulator-5554')
log = d.shell('logcat -d | grep -E "467e6048|AUDIT-CAT-X" | tail -10')
s = log.output if hasattr(log,'output') else str(log)
print(s.strip() if s.strip() else 'no direct logs')
