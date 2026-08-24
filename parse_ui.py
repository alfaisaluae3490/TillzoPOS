import xml.etree.ElementTree as ET, sys
fn = sys.argv[1] if len(sys.argv) > 1 else 'ui1.xml'
tree = ET.parse(fn)
for el in tree.iter('node'):
    t = (el.get('text') or '').strip()
    d = (el.get('content-desc') or '').strip()
    if t or d:
        print(f"[{el.get('class').split('.')[-1]}] '{t}' | desc='{d}' | bounds={el.get('bounds')} clickable={el.get('clickable')}")
