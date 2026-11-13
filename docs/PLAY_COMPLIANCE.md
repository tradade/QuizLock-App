# Google Play compliance notes

This is an internal checklist for the Play Console and reviewers, not legal
advice. Review it with counsel before publishing.

## AccessibilityService

- Declared with `BIND_ACCESSIBILITY_SERVICE`; `canRetrieveWindowContent="false"`.
- Use case: parental control / screen-time gate.
- It only reads the foreground package name to count screen time and open the
  quiz gate. It does not read screen content or input.
- The onboarding flow explains the use case before the parent enables the
  service in system Settings. The setting is never enabled automatically.
- Play Console: declare the accessibility use and provide the disclosure video
  or description if requested.

## Foreground service (`specialUse`)

- Declared as `android:foregroundServiceType="specialUse"` with
  `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`.
- Purpose: persistent parental-control watchdog and status notification.
- Android 15/16 can reject a `specialUse` FGS started directly from
  `BOOT_COMPLETED`; the app catches the rejection and retries from the bound
  AccessibilityService. The enforcement logic itself lives in the
  AccessibilityService.
- Play Console: declare the `specialUse` subtype and explain the persistent
  notification.

## Battery optimization

- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is requested from a user action in
  onboarding/settings so the watchdog survives Doze and OEM killers.
- The app falls back to the general battery-optimization settings screen when a
  device does not support the direct request.

## Device Admin

- Optional and user-initiated. The app is not a device owner and does not use
  device-owner capabilities.
- `onDisableRequested` shows a warning; disabling admin moves the app to a
  broken state and notifies the parent.

## Data safety

- No data collected and no data shared.
- Profiles and history are stored locally; backup and device transfer are
  disabled for the app database.
- `INTERNET` is used only for an optional HTTPS question-bank import. No data is
  uploaded.

## Privacy policy

- Publish `docs/PRIVACY.md` at a stable public URL (for example GitHub Pages)
  and link it from the Play Console listing.
- The in-app Terms & Privacy screen contains the same commitments.

## Target audience

- The app is intended for parents and guardians, not as a child-directed app.
  Children use the device after a parent configures it. Review the Play Families
  policy before publishing if the listing targets families.
