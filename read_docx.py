import zipfile
import xml.etree.ElementTree as ET

def extract():
    try:
        ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
        with zipfile.ZipFile('c:/Users/Faii/Desktop/Tillzo/POS_Blueprint_v3_REST_API_Edition.docx') as z:
            root = ET.fromstring(z.read('word/document.xml'))
            lines = []
            for p in root.findall('.//w:p', ns):
                texts = [t.text for t in p.findall('.//w:t', ns) if t.text]
                if texts:
                    lines.append(''.join(texts))
            with open('c:/Users/Faii/Desktop/Tillzo/blueprint.txt', 'w', encoding='utf-8') as f:
                f.write('\n'.join(lines))
            print("SUCCESS")
    except Exception as e:
        print("ERROR:", str(e))

if __name__ == '__main__':
    extract()
