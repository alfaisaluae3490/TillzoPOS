#!/usr/bin/env python3
"""Click save and grep ONLY for our tags: InventoryCrudVM / SyncWorker / saveItem."""
import uiautomator2 as u2, time

d = u2.connect('emulator-5554')
d.shell('logcat -c')

btn = d(description='Save Product')
btn.click()
time.sleep(5)

out = d.shell('logcat -d | grep -E "InventoryCrudVM|SyncWorker|saveItem|UpdateProduct|InventoryUpsert"')
s = out.output if hasattr(out, 'output') else str(out)
print(s.strip()[-1000:] if s.strip() else 'NO sync/save logs — onClick still not firing')

xml = d.dump_hierarchy()
print('form open:', 'Edit Product' in xml)
