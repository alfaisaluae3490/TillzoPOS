#!/usr/bin/env python3
"""Import icon not found (screen state?). Diagnose + navigate to inventory first."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,40})"', xml) if t.strip()]
print('state:', texts[:8])
