#!/usr/bin/env python3
"""CRITICAL FINDING: after app restart, HERMES-PROD-001 shows Stock: 19.0 (not 18)!
So local DB has 19 — the recalc-to-18 was REVERTED. Earlier 18 was visible pre-restart.
Sequence: GRN→recalc(18,pending)→delta-pull overwrote to 19? But pull respects pending...
UNLESS pull ran BEFORE recalc marked pending, then recalc set 18+pending, and a LATER
pull with remote=19 & local.timestamp check overwrote again clearing pending.
DeltaSync line 302 checks sync_status != pending — but if the overwrite happened in the
window BEFORE recalc, then recalc ran later setting 18+pending... app now shows 19!

Simplest explanation: recalc wrote 18; THEN a delta-pull arrived with sheet row (stock
19, old) and since at that instant sync_status was 'synced' briefly between insertBatch
and updateItem... race. The fix: recalculateTotalStock must run AFTER batch insert AND
the whole GRN confirm must be atomic w.r.t. pulls.

Repair now: edit product stock via UI: 19 -> 20? Actually correct value = 18 (17+1)?
Original manual stock was 19 (no batches). Batch-INITIAL created at CSV import had 5.0?
Confusing. TRUE intent: GRN added 1 PC to existing 19 → should be 20.
Current local 19 means GRN's +1 got lost entirely!
Verify batches via 'View Batches' tap."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
vb = find_bounds(xml, r'View Batches')
print('view batches:', vb)
if vb:
    d.click((vb[0]+vb[2])//2,(vb[1]+vb[3])//2)
    time.sleep(3)
    xml = dump(d)
    texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
    print('batches:', texts[:16])
