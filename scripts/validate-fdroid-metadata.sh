#!/usr/bin/env bash
# Validate the fdroiddata metadata draft locally.
# Checks: YAML syntax, required fields, category whitelist, build block.
set -euo pipefail

FILE="${1:-docs/fdroid/com.xieguiawu.currencytransfer.yml}"

echo "== Validating $FILE =="

python3 - "$FILE" <<'EOF'
import sys, re
path = sys.argv[1]
text = open(path).read()

# Minimal YAML parse supporting flat keys + indented list items under Categories/Builds
data = {}
list_items = {}
current_list = None
for line in text.splitlines():
    stripped = line.strip()
    if not stripped or stripped.startswith('#'):
        continue
    indent = len(line) - len(line.lstrip())
    if indent == 0:
        m = re.match(r'^([A-Za-z][A-Za-z0-9]*):\s*(.*)$', line)
        if m:
            key, val = m.group(1), m.group(2).strip()
            data[key] = val
            current_list = key if val == '' else None
            if val != '':
                list_items[key] = [val]
    elif indent > 0 and current_list:
        item = re.sub(r'^[-*]\s*', '', stripped)
        list_items.setdefault(current_list, []).append(item)

required = [
    'Categories', 'License', 'AuthorName', 'SourceCode', 'IssueTracker',
    'Changelog', 'AutoName', 'RepoType', 'Repo', 'Builds',
    'AutoUpdateMode', 'UpdateCheckMode', 'CurrentVersion', 'CurrentVersionCode',
]
missing = [k for k in required if k not in data]
if missing:
    print(f"FAIL: missing required fields: {missing}")
    sys.exit(1)

# Validate against the actual fdroiddata config/categories.yml
from collections import OrderedDict
cats = list_items.get('Categories', [data.get('Categories', '')])

# Fetch official categories from a local fdroiddata clone if present, else fallback knowledge
official = None
for candidate in ['config/categories.yml', '/tmp/fdroiddata/config/categories.yml']:
    import os
    if os.path.exists(candidate):
        found = []
        for line in open(candidate):
            m = re.match(r'^([A-Za-z][A-Za-z0-9 &.-]*):\s*$', line)
            if m:
                found.append(m.group(1).strip())
        official = set(found)
        break
if official:
    invalid = [c for c in cats if c not in official]
    if invalid:
        print(f"FAIL: category not in official fdroiddata categories.yml: {invalid}")
        print("  Valid finance-related: Finance Manager, Market & Price")
        sys.exit(1)
    print(f"OK: categories {cats} all valid in official fdroiddata list")

if data.get('RepoType') != 'git':
    print(f"FAIL: RepoType must be 'git', got '{data.get('RepoType')}'")
    sys.exit(1)

if data.get('UpdateCheckMode') != 'Tags':
    print(f"FAIL: UpdateCheckMode must be 'Tags', got '{data.get('UpdateCheckMode')}'")
    sys.exit(1)

builds_text = text.split('Builds:')[1].split('AntiFeatures:')[0] if 'Builds:' in text else ''
if not re.search(r'commit:\s*v1\.0\.0', builds_text) or 'versionCode: 1' not in builds_text:
    print("FAIL: Builds block must reference commit v1.0.0 and versionCode 1")
    sys.exit(1)
if 'subdir: app' not in builds_text:
    print("FAIL: Builds must include subdir: app (module is in app/ subdirectory)")
    sys.exit(1)

if 'NonFreeNet' not in text:
    print("WARN: AntiFeatures NonFreeNet not declared")

print(f"OK: {len(required)} required fields, categories={cats}, repo={data.get('Repo')}, subdir=app")
EOF
