#!/usr/bin/env python3
"""Save Expense visible at (710,1572). Fill desc first (idx1), then click save.
NO back-press before save this time."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
els = d(className='android.widget.EditText')
# fill description
if els.count >= 2 and not els[1].info.get('text'):
    els[1].click(); time.sleep(1)
    d.shell('input text AUDIT-EXP-1'); time.sleep(0.8)

xml = dump(d)
save = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'text="Save Expense"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        save = tuple(map(int,b.groups()))
print('save:', save)
if save:
    # kb may cover it; hide kb via one back (dialog has dismissible=false? we saw form survives)
    d.press('back'); time.sleep(1.5)
    xml = dump(d)
    print('form alive:', 'Log New Expense' in xml)
    save2 = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="Save Expense"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            save2 = tuple(map(int,b.groups()))
    tgt = save2 or save
    print('clicking save at:', tgt)
    if tgt:
        d.click((tgt[0]+tgt[2])//2,(tgt[1]+tgt[3])//2)
        time.sleep(4)

xml = dump(d)
print('saved:', 'AUDIT-EXP-1' in xml or '45.75' in xml, '| dialog gone:', 'Log New Expense' not in xml)
