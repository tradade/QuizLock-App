# QuizLock Privacy Policy

Last updated: 2026-11-06

QuizLock is a parental-control app that runs entirely on the device. This policy
explains what the app stores, which permissions it uses, and what it never does.

## Data stored on the device

- Child profiles: name, age, avatar, daily limits and policy settings.
- Screen-time ledger and quiz attempt history.
- Parent PIN hash, recovery-code hash and app configuration.

All of this is stored in a local SQLite database in the app's private storage.
There is no account system, no backend, no analytics and no crash reporter.

## Data we do not collect

- We do not receive child names, ages, learning results or device identifiers.
- The app does not read screen content, keystrokes, messages, passwords or
  banking information.
- The AccessibilityService uses only the foreground package name to measure
  screen time and show quiz gates.

## Backup and device transfer

Android cloud backup and device-to-device transfer are disabled for the app
database. Uninstalling the app deletes the local database and settings.

## Network access

The app declares `INTERNET` only for the optional question-bank update. If a
parent enters a direct **HTTPS** URL, the app downloads that JSON file and
imports it into the local database. No user data is uploaded.

## Permissions

| Permission | Purpose |
|---|---|
| AccessibilityService | Detect the foreground app and show quiz gates. No screen content is read. |
| Notifications (Android 13+) | Show the persistent protection status. |
| Ignore battery optimization | Keep the watchdog service alive. |
| Device Admin (optional) | Add a warning step before uninstalling. |
| Internet | Optional HTTPS question-bank import only. |

The app does not request usage-access or overlay permission.

## Children's privacy

QuizLock is configured by a parent or guardian. It does not create accounts for
children and does not transmit child data anywhere.

## Changes

This policy may be updated; the current version is kept in this repository.

## Contact

tradade@gmail.com

> This document is a repository template. Review it with legal counsel and
> publish a stable public URL before submitting to Google Play.
