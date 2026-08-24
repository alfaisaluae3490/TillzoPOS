#!/usr/bin/env python3
"""Form closed but did the new expense save? Check list for AUDIT-EXP-1 desc / 45.75.
The category chip 'Misc' tap failed earlier (misc: None) — maybe form validation blocked
save OR saved with different state. Check now."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
has_new = ('AUDIT-EXP-1' in xml) or ('45.75' in xml)
print('new expense visible:', has_new)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print(texts[:16])
