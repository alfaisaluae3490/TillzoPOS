import subprocess, re, sys, time

def sh(cmd):
    r = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return r.stdout

def dump(tag):
    sh("adb shell uiautomator dump /sdcard/u.xml >/dev/null 2>&1")
    xml = sh("adb shell cat /sdcard/u.xml 2>/dev/null")
    nodes = []
    for m in re.finditer(r'<node[^>]+?>', xml):
        n = m.group(0)
        t = re.search(r'text="([^"]*)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        c = re.search(r'class="([^"]+)"', n)
        cls = c.group(1).split('.')[-1] if c else ''
        txt = t.group(1) if t else ''
        bnd = tuple(map(int, b.groups())) if b else None
        if txt.strip() or cls in ('Button', 'EditText', 'ImageButton'):
            nodes.append((cls, txt, bnd))
    print(f"--- {tag} ---")
    for cls, txt, bnd in nodes:
        print(f"{cls:12s} | {txt[:60]} | {bnd}")
    return nodes

def tap(x, y):
    sh(f"adb shell input tap {x} {y}")
    time.sleep(1.5)

def center(bnd):
    x1, y1, x2, y2 = bnd
    return ((x1 + x2) // 2, (y1 + y2) // 2)

if __name__ == "__main__":
    # usage: python3 uiauto.py launch|tap <x> <y>|type <text>|dump <tag>
    cmd = sys.argv[1]
    if cmd == "launch":
        sh("adb shell am start -n com.tillzo.pos/.ui.MainActivity")
        time.sleep(9)
        dump("after-launch")
    elif cmd == "tap":
        tap(int(sys.argv[2]), int(sys.argv[3]))
        dump("after-tap")
    elif cmd == "type":
        sh(f'adb shell input text "{sys.argv[2]}"')
        time.sleep(2.5)
        dump("after-type")
    elif cmd == "dump":
        dump(sys.argv[2])
