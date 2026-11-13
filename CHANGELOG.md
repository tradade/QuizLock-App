# Changelog

All notable changes to QuizLock are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project uses
semantic versioning.

## [1.0.0] - 2026-11-13

### Added

- Bilingual Vietnamese/English UI and system resources.
- PIN-protected parent app lock with recovery-code unlock.
- Debug-only Super PIN for development access.
- Per-profile policy settings from the Profile Manager.
- Excel/CSV question import with optional columns and a template link.
- Content-based duplicate prevention and deterministic question IDs.
- Delete-all question bank action with a reseed guard.
- Event-driven watchdog with batched ledger writes.
- Permission health check and BROKEN notification.
- Best-effort watchdog restart after task removal.
- Room schema export and CI workflow.
- Apache-2.0 license and open-source project documentation.

### Changed

- Application ID is now `com.cloudon.quizlock`.
- Application name is now QuizLock.
- Minimum supported Java target is 17.
- Core library desugaring is enabled for `java.time` on API 24–25.

### Fixed

- Watch time is no longer counted while the screen is off or Parent Mode is
  active, and deep-sleep time is not counted.
- Changing the PIN no longer invalidates the existing recovery code.
- Local profiles and PIN material are excluded from cloud backup and device
  transfer.
