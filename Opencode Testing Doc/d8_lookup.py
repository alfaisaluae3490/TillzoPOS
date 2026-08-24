#!/usr/bin/env python3
"""Returns screen functional (QR/UUID entry). Test UUID path with a real sale UUID from
sheet Sales_Aug_2026? We have none handy — verify UI accepts input & shows not-found
gracefully (bug check). Type a bogus UUID, expect graceful error, not crash."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
els = d(className='android.widget.EditText')
print('input fields:', els.count)
if els.count:
    els[0].click(); time.sleep(1)
    d.shell('input text 99999999-9999-9999-9999-999999999999'); time.sleep(1)
    # find submit/lookup button
    xml = dump(d)
    btn = None
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="(Lookup|Search|Find|Submit|Next)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if m and b:
            x1,y1,x2,y2 = map(int,b.groups())
            btn = (m.group(1),(x1,y1,x2,y2))
    print('lookup btn:', btn)
    if btn:
        b = btn[1]
        d.click((b[0]+b[2])//2,(b[1]+b[3])//2)
        time.sleep(3)
    else:
        # maybe IME action
        d.press('enter') if hasattr(d,'press') else None
        d.shell('input keyevent 66'); time.sleep(3)

xml = dump(d)
texts = [t for t in re.findall(r'text="([^"]{1,60})"', xml) if t.strip()]
print('after lookup:', texts[:12])
