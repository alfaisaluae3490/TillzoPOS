#!/usr/bin/env python3
"""P3 DELETE TEST: create a disposable item via CSV import, then delete it via UI
(long-press or Delete button), verify gone from app + sheet."""
import uiautomator2 as u2, time, re, subprocess

d = u2.connect('emulator-5554')
ADB = r'C:/Users/Faisal Khan/AppData/Local/Android/Sdk/platform-tools/adb.exe'

def dump():
    return d.dump_hierarchy()

def find_bounds(xml, pattern):
    for n in re.findall(r'<node[^>]*>', xml):
        m = re.search(r'text="([^"]*)"', n)
        if m and re.search(pattern, m.group(1)):
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
            if b:
                x1, y1, x2, y2 = map(int, b.groups())
                if x2 > x1 and y2 > y1:
                    return (x1, y1, x2, y2)
    return None

# STEP 0: push disposable CSV to device Downloads
csv_content = "name,sku,barcode,category,cost_price,selling_price,stock_qty,unit\nDELETE-ME-TEST,SKU-DEL-1,,Testing,1.00,2.00,5,pcs\n"
with open(r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/delete_me.csv', 'w') as f:
    f.write(csv_content)
subprocess.run([ADB, '-s', 'emulator-5554', 'push',
                r'C:/Users/Faisal Khan/Desktop/Tillzo/Opencode Testing Doc/delete_me.csv',
                '/sdcard/Download/delete_me.csv'], capture_output=True)

# STEP 1: go to inventory list
xml = dump()
if 'Search items' not in xml and 'OUT OF STOCK' not in xml:
    d.press('back'); time.sleep(1)
    d.click(871, 254); time.sleep(3)
    xml = dump()

# STEP 2: open CSV import (icon top-right)
c = find_bounds(xml, r'Import CSV')
print('import icon:', c)
if c:
    d.click((c[0]+c[2])//2, (c[1]+c[3])//2)
    time.sleep(2.5)

# STEP 3: navigate file picker -> Downloads -> delete_me.csv
xml = dump()
hamburger = find_bounds(xml, r'Show roots|Open navigation drawer')
if hamburger:
    d.click((hamburger[0]+hamburger[2])//2, (hamburger[1]+hamburger[3])//2)
    time.sleep(2)
xml = dump()
dl = find_bounds(xml, r'^Downloads$')
if dl:
    d.click((dl[0]+dl[2])//2, (dl[1]+dl[3])//2)
    time.sleep(2.5)
xml = dump()
f = find_bounds(xml, r'delete_me\.csv')
print('file entry:', f)
if f:
    d.click((f[0]+f[2])//2, (f[1]+f[3])//2)
    time.sleep(4)

# STEP 4: verify item in list
xml = dump()
found = 'DELETE-ME-TEST' in xml
print('DELETE-ME-TEST imported:', found)

# STEP 5: tap the item card to open detail (find its card), locate Delete button
c = find_bounds(xml, r'DELETE-ME-TEST')
if c:
    # tap on card body (not the delete icon yet)
    d.click((c[0]+c[2])//2, c[3] + 30)   # just below name text
    time.sleep(3)
xml = dump()
# look for Delete button (content-desc or text)
del_btn = find_bounds(xml, r'^Delete$')
desc_del = None
for n in re.findall(r'<node[^>]*>', xml):
    m = re.search(r'content-desc="Delete"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if m and b:
        x1, y1, x2, y2 = map(int, b.groups())
        desc_del = (x1, y1, x2, y2)
print('detail Delete btn:', del_btn, '| desc Delete:', desc_del)

target = del_btn or desc_del
if target:
    d.click((target[0]+target[2])//2, (target[1]+target[3])//2)
    time.sleep(3)
    xml = dump()
    texts = re.findall(r'text="([^"]{1,50})"', xml)
    print('after delete tap:', texts[:10])
    # confirm dialog? click confirm Delete
    conf = find_bounds(xml, r'^Delete$')
    if conf:
        d.click((conf[0]+conf[2])//2, (conf[1]+conf[3])//2)
        time.sleep(4)
xml = dump()
gone_from_list = 'DELETE-ME-TEST' not in xml
print('deleted from list:', gone_from_list)
