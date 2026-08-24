#!/usr/bin/env python3
"""Three clickable squares: (88,1154) = ? ; (728,1045)=Print QR; (860,1045)=Delete.
Edit icon likely at (88,1154)?? That's left-bottom. OR edit opens by tapping card
name area. Earlier in session, tapping card text opened Edit Product directly!
(288,962 tap → Edit Product). So: tap the card name text now."""
import sys, os, time, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tz import connect, dump

d = connect()
d.click(288, 962)
time.sleep(3)
xml = dump(d)
print('edit form:', 'Edit Product' in xml)
