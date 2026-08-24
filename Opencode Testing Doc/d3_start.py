#!/usr/bin/env python3
"""Cleanup orphan AUDIT-CAT-X sheet row: add category locally with SAME name? No —
orphan has different system_row_id. Proper fix: delete the sheet row directly via
the app's own sheet context? Simplest reliable path: use gspread via Python with the
same OAuth? No creds available headless.
ALTERNATIVE: leave row but mark is_deleted via app: create NEW category named
AUDIT-CAT-X (gets new UUID) -> sync appends -> then delete it -> sync removes BOTH?
No — only new UUID row removed.
BEST: Use Sheets API directly from Python using credentials from the app's token store?
Not accessible.
PRAGMATIC: Manually clear the orphan row by writing empty values through the public
sheet URL requires auth too.
DECISION: Document orphan + verify D2-1 code fix prevents future occurrences. Move on."""
import uiautomator2 as u2, time, re

d = u2.connect('emulator-5554')

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1,y1,x2,y2 = map(int,b.groups())
                if x2>x1 and y2>y1: return (x1,y1,x2,y2)
    return None

# D2 continue: Categories module verified ADD + DELETE locally; UPDATE not supported in UI
# (missing rename feature — noted as enhancement). Move to Vendors module test.
xml = dump()
print('inventory list:', 'Search items' in xml)

# open Advanced Menu for Vendors — menu icon on inventory screen is at 992,171? No,
# that was picker's. Inventory toolbar icons: Search(794-926), More options(926-1058)?
# Actually earlier dump on inventory showed Import CSV at (970,221). Menu icon = ?
# Scanner home had Menu at (1003,254)? Try opening Advanced Options:
d.press('back'); time.sleep(2)
xml = dump()
texts = re.findall(r'text="([^"]{1,30})"', xml)
print('now:', texts[:6])
