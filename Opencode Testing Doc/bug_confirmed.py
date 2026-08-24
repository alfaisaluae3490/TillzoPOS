#!/usr/bin/env python3
"""TIMELINE DECODED:
23:13 category added (pending, local only)
~23:14 first Force Sync ran but sheet export happened BEFORE add? No — sync at 23:27 said
'nothing to sync' for Categories... wait, that was PID 14998's earlier run. The ADD synced
to sheet during the 23:36 SyncWorker? Sheet row says status='synced' => YES it uploaded.
23:31 delete clicked -> local hard-delete pending
23:36 sync -> 'Hard deleted local-only category' — meaning idToRowMap did NOT contain the ID!
But the sheet HAD the row (we see it now with 'synced'). Why missing from map?
=> The GET Categories!A:ZZ at 23:37 returned HTTP 429! And at 23:46 sync said 'nothing to sync'
because local pendingDeletions was already cleared by hardDelete.

BUG CONFIRMED: on API failure (429) the code still hard-deletes locally when map lookup fails,
losing the deletion marker forever. Fix: don't hard-delete on null rowIndex if remote read failed;
only treat as not-on-sheet when remote read SUCCEEDED."""
import uiautomator2 as u2
d = u2.connect('emulator-5554')
# check the current local DB state via UI: reopen manager to confirm gone locally
xml = d.dump_hierarchy()
print('app alive:', 'TillzoPOS' in xml or 'scanner' in xml.lower())
