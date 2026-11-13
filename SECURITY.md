# Security policy

## Supported versions

Security fixes are applied to the `main` branch. Tagged releases receive fixes
when practical.

## Reporting a vulnerability

Please do not open a public issue for security problems.

Use GitHub's private vulnerability reporting:
https://github.com/tradade/QuizLock/security/advisories/new

Include:

- A description of the issue and its impact.
- Steps to reproduce, with the Android version and device/ROM if relevant.
- Whether the issue affects a release, debug build, or both.
- Any suggested fix or mitigation.

## Scope

Areas of particular interest:

- Parent PIN, recovery code and app lock.
- Data exposure through backup, logs or exported files.
- Accessibility service behavior and permission handling.
- Question import (Excel/CSV/JSON) parsing and content validation.
- Bypass of the quiz gate or hard block.

## Out of scope

- Force-stop/uninstall behavior imposed by Android; these limitations are
  documented in the README.
- Physical access to an unlocked device.
- Vulnerabilities in third-party dependencies that are already fixed upstream
  and have no practical impact on QuizLock.
