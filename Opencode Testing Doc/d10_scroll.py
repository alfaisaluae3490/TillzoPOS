#!/usr/bin/env python3
"""Search cleared but items still not visible. Check full screen + scroll."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:20])
# scroll down looking for CUST items
for _ in range(3):
    d.swipe(540, 1800, 540, 800, duration=0.4); time.sleep(1.2)
    xml = dump(d)
    if 'AUDIT-CUST-A' in xml:
        print('FOUND A')
        break
