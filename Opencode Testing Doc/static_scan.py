#!/usr/bin/env python3
"""D0 STATIC ANALYSIS - comprehensive bug/vuln scan of Tillzo codebase."""
import subprocess, re, os

BASE = r'C:/Users/Faisal Khan/Desktop/Tillzo/app/src/main/java/com/tillzo/pos'

issues = []

# gather all kt files
kt_files = []
for root, dirs, files in os.walk(BASE):
    for f in files:
        if f.endswith('.kt'):
            kt_files.append(os.path.join(root, f))

print(f'Total Kotlin files: {len(kt_files)}')

def scan(pattern, name, severity, file_filter=None, exclude=None):
    hits = []
    for fp in kt_files:
        rel = os.path.relpath(fp, BASE).replace('\\', '/')
        if file_filter and not re.search(file_filter, rel):
            continue
        if exclude and re.search(exclude, rel):
            continue
        try:
            with open(fp, encoding='utf-8', errors='ignore') as fh:
                for ln, line in enumerate(fh, 1):
                    if re.search(pattern, line):
                        if exclude and re.search(exclude, line):
                            continue
                        hits.append((rel, ln, line.strip()[:100]))
        except Exception:
            pass
    if hits:
        issues.append((name, severity, hits))
    return len(hits)

# 1. Null-safety: non-null assertion !! in sync/business logic (crash risk)
scan(r'!!', 'Non-null assertions (!!) — crash risk on race/missing data', 'HIGH',
     file_filter=r'(sync|usecase|repository)', exclude=r'(test|//|\*|import)')

# 2. runBlocking on main paths (ANR risk) — already known ok in interceptor but check others
scan(r'runBlocking', 'runBlocking usage — ANR risk if on main thread', 'MEDIUM')

# 3. Hardcoded numbers/format strings in money handling
scan(r'toDouble\(\)', 'toDouble() without null-safe fallback check needed', 'LOW',
     file_filter=r'(ui|ViewModel)', exclude=r'toDoubleOrNull')

# 4. Unlogged exception swallow
scan(r'catch \(e: Exception\) \{\s*\}', 'Empty catch block', 'HIGH')

# 5. SQL injection via string concat in queries
scan(r'@Query\("[^"]*(\+\s*\w+|\$\{)', 'Raw concat in @Query — verify parameterization', 'HIGH',
     file_filter=r'dao')

# 6. Missing Dispatchers on viewModelScope.launch
scan(r'viewModelScope\.launch \{(?!.*Dispatchers)', 'viewModelScope.launch without explicit dispatcher', 'INFO',
     exclude=r'Dispatchers|suspend fun invoke')

# 7. Thread-sleep in production code
scan(r'Thread\.sleep', 'Thread.sleep — blocking call', 'MEDIUM', exclude='//')

# 8. Global scope
scan(r'GlobalScope', 'GlobalScope usage — lifecycle leak', 'HIGH')

print('\n=== FINDINGS ===')
for name, sev, hits in issues:
    print(f'\n[{sev}] {name} ({len(hits)} hits)')
    for h in hits[:6]:
        print('  ', h[0] + ':' + str(h[1]), '->', h[2][:80])

print('\n=== SUMMARY ===')
high = sum(1 for _, s, _ in issues if s == 'HIGH')
med = sum(1 for _, s, _ in issues if s == 'MED')
print(f'HIGH: {high}, MED: {med}, categories: {len(issues)}')
