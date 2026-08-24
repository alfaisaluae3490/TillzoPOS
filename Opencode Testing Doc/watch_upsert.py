#!/usr/bin/env python3
"""toSyncMap is fine (stock_qty=current_stock). So the pending row SHOULD have uploaded.
Unless... recalculate ran but a LATER full-row write from another path overwrote with 19?
OR the GRN batch insert happened AFTER recalc? Order in code: insertBatch THEN recalc. OK.

Test hypothesis: maybe recalc never ran because inventoryAction was 'PENDING' and the
PENDING branch requires targetBatchId OR creates new batch — we saw new batch created
(ffc10eb4) so recalc DID run → current_stock = sum(active batches).
If HERMES had NO prior batches, sum = 1.0 only! But app shows 18.0...

Wait: app shows 18.0 now. Sheet shows 19.0. If recalc set current=1.0, app would show 1.
App=18 means something else. hasBatches was true w/ existing batches totaling 17+1=18 ✓.
So local is CORRECT at 18. Sheet upload of this pending row failed silently or got skipped.

Direct test: check sheet row updated_at vs app. Simplest: force one more sync & watch
logcat for 'InventoryUpsert' with pending counts."""
import uiautomator2 as u2, time
d = u2.connect('emulator-5554')
d.shell('logcat -c')
# trigger sync via menu tap sequence
d.click(1003,254); time.sleep(3)
xml = d.dump_hierarchy()
import re as r
for n in r.findall(r'<node[^>]*>', xml):
    m = r.search(r'text="Force Sync"', n)
    b = r.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        if y1 > 100:
            d.click((x1+x2)//2,(y1+y2)//2)
            break
time.sleep(4)
xml = d.dump_hierarchy()
for n in r.findall(r'<node[^>]*>', xml):
    m = r.search(r'text="Force Sync"', n)
    b = r.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        if y1 > 1000:
            d.click((x1+x2)//2,(y1+y2)//2)
            break
time.sleep(15)
out = d.shell('logcat -d | grep -E "InventoryUpsertUseCase|pendingItems|upserted"')
s = out.output if hasattr(out,'output') else str(out)
print(s.strip()[-500:])
