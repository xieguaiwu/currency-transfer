# Security Policy

FX Pixel is a privacy-focused Android app. This policy explains how to
report security issues and what the project does to protect users.

## Supported versions

Only the latest release is supported for security fixes.

## Reporting a vulnerability

Do not open a public issue for a vulnerability. Email the maintainers:

- Maintainer: xieguaiwu
- Contact: open a private security advisory at
  https://github.com/xieguaiwu/currency-transfer/security/advisories/new

Include in your report:

1. App version and device/Android version
2. Steps to reproduce
3. Impact (what an attacker could do)
4. Evidence (screenshots, logs) without sensitive data

We aim to respond within 7 days.

## Security baseline

- Single permission: INTERNET
- HTTPS only (network security config blocks cleartext)
- System trust anchors only (no user-added CAs)
- No backup or cloud sync (data extraction rules)
- No analytics, ads, or tracking
- No secrets or API keys in the codebase
