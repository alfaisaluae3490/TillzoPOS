#!/usr/bin/env python3
"""Go to inventory list and find HERMES-PROD-001 stock in app."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(871, 254); time.sleep(4)
xml = dump(d)
# search HERMES
import subprocess
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'
search = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="(Search items[^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1,y1,x2,y2 = map(int,b.groups())
        search = (x1,y1,x2,y2)
if search:
    d.click((search[0]+search[2])//2,(search[1]+search[3])//2); time.sleep(1.5)
    d.shell('input text HERMES-PROD-001'); time.sleep(3)
    d.shell('input keyevent 111'); time.sleep(1)  # close suggestions? no, keep results

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,50})"', xml) if t.strip()]
print(texts[:14])
m = re.search(r'text="Stock: ([\d.]+)"', xml)
print('HERMES stock:', m.group(1) if m else '?')
