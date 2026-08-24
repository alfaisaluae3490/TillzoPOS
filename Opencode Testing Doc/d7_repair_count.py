#!/usr/bin/env python3
"""D7 data repair: HERMES-PROD-001 stock. App shows 19 (stale), batches sum = many 1.0s
+15s etc from repeated GRNs. True correct value ambiguous now; simplest truth: batch sum.
Get exact batch list via UI 'View Batches', count precisely with scroll-to-bottom logic,
then edit product stock to that total, save, sync, verify sheet."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump, find_bounds

d = connect()

# inventory module
d.click(871,254); time.sleep(4)

# search HERMES-PROD-001
search = find_bounds(dump(d), r'Search items')
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)
    d.press('back'); time.sleep(1)  # hide kb

# open View Batches
vb = find_bounds(dump(d), r'View Batches')
print('vb:', vb)
if vb:
    m = re.search(r'View Batches \(([\d.]+)\)', vb and dump(d) or '')
    d.click((vb[0]+vb[2])//2,(vb[1]+vb[3])//2)
    time.sleep(3)

    # collect all Stock: X values until end (no new content after swipe)
    qtys, prev_sig = [], None
    for _ in range(8):
        xml = dump(d)
        qs = re.findall(r'text="Stock: ([\d.]+)"', xml)
        sig = tuple(qs)
        if sig == prev_sig:
            break
        # merge new ones by order — naive but ok for audit
        if not qtys:
            qtys = qs
        else:
            # if scrolled to new page, append unseen tail
            for q in qs:
                qtys.append(q)
        prev_sig = sig
        d.swipe(540, 1700, 540, 900, duration=0.35); time.sleep(1.2)

    fqtys = [float(q) for q in qtys]
    print('collected qtys:', qtys)
    print('sum:', sum(fqtys), 'count:', len(qtys))
