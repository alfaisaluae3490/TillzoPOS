#!/usr/bin/env python3
"""State check after escapes."""
import sys, os, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump
d = connect()
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print(texts[:12])
