#!/usr/bin/env python3
"""FOUND IT: Batch input = clickable View (166,1178,529,1354), Expiry = (551,1178,892,1354).
These are custom click-target overlays over read-only fields (per code: matchParentSize Box
with date picker + batch uses same pattern?). Fill via set_text on EditText '30'? No —
'30' is expAlert. The batch/expiry VALUES may live in TextViews inside those Views.
Strategy: click the View -> if a picker/dialog opens, dismiss; else type directly.
Then Save."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

# Click batch field container
d.click(347, 1266)   # center of (166,1178,529,1354)
time.sleep(2)
xml = d.dump_hierarchy()
texts = re.findall(r'text="([^"]{1,50})"', xml)
print('after batch-field tap:', texts[:14])
