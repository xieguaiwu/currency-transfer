#!/usr/bin/env bash
# Validate the fdroiddata metadata draft locally.
# Checks: YAML syntax, required fields, category whitelist, build block.
set -euo pipefail

FILE="${1:-docs/fdroid/com.xieguiawu.currencytransfer.yml}"

echo "== Validating $FILE =="

python3 - "$FILE" <<'EOF'
import sys, re, pathlib
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
if 'subdir: app' not in builds_text:
    print("WARN: no 'subdir: app' — fine only if the Gradle project root IS the repo root")

# Every commit: reference must be a real, pushed tag, and versionName/versionCode
# must agree with fastlane changelogs/<versionCode>.txt.
import subprocess
refs = re.findall(r'commit:\s*([A-Za-z0-9._^{}~/-]+)', builds_text)
codes = re.findall(r'versionCode:\s*(\d+)', builds_text)
names = re.findall(r'versionName:\s*([\d.]+)', builds_text)
if not refs:
    print("FAIL: Builds block has no commit: references")
    sys.exit(1)
try:
    tags = set(subprocess.run(['git', 'tag'], capture_output=True, text=True,
                              check=True).stdout.split())
except Exception as e:
    tags = set()
    print(f"WARN: cannot list git tags ({e}) — skipping tag existence check")
missing = [r for r in refs if r not in tags]
if missing:
    print(f"FAIL: commit: references are not local tags: {missing}")
    print("  fix: git tag <name> <sha> && git push origin --tags")
    sys.exit(1)
print(f"OK: all {len(refs)} commit: references exist as tags {refs}")

for code in codes:
    for loc in ('en-US', 'zh-CN'):
        p = pathlib.Path(f'fastlane/metadata/android/{loc}/changelogs/{code}.txt')
        if not p.exists():
            print(f"FAIL: missing changelog {p} (versionCode {code})")
            sys.exit(1)
print(f"OK: changelogs present for versionCodes {codes} (en-US + zh-CN)")

cur = data.get('CurrentVersion', '')
if names and cur != names[-1]:
    print(f"FAIL: CurrentVersion '{cur}' != last Builds versionName '{names[-1]}'")
    sys.exit(1)
if codes and str(data.get('CurrentVersionCode')) != codes[-1]:
    print(f"FAIL: CurrentVersionCode '{data.get('CurrentVersionCode')}' != last Builds versionCode '{codes[-1]}'")
    sys.exit(1)
print(f"OK: CurrentVersion {cur}/{data.get('CurrentVersionCode')} matches newest Build entry")

if 'NonFreeNet' not in text:
    print("NOTE: NonFreeNet not declared (correct for pure-LAN/offline apps; "
          "required if the app depends on a proprietary network service)")

print(f"OK: {len(required)} required fields, categories={cats}, repo={data.get('Repo')}")
EOF
