#!/usr/bin/env python3
"""Debug: what screen is app on now? Full dump analysis."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = re.findall(r'text="([^"]{1,60})"', xml) if 're' in dir() else []
import re
texts = re.findall(r'text="([^"]{1,60})"', xml)
print(texts[:16])
