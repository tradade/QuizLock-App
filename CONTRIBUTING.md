# Contributing to QuizLock

Thanks for helping improve QuizLock. This project is a parental-control tool that
runs entirely on the device; changes must preserve that privacy guarantee.

## Requirements

- JDK 17 or 21
- Android SDK Platform 36.1
- Android Studio with AGP 9.x support, or the bundled Gradle wrapper

## Build and test

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Debug builds use the standard Android debug keystore; no local keystore setup is
required. Release signing is configured through `KEYSTORE_PATH`,
`STORE_PASSWORD` and `KEY_PASSWORD` environment variables.

## Code guidelines

- Follow the existing Kotlin style and keep changes focused.
- Add or update unit tests for policy, security, import and parsing logic.
- Do not add analytics, crash reporters, or any code that sends child data off
  the device.
- Keep the app usable when the question bank is empty: the fallback generator is
  part of the product, not optional.
- Run the test suite and a debug build before opening a pull request.

## Database changes

- The Room schema is exported to `app/schemas/`; commit the generated schema.
- Add an explicit migration for every schema change. Destructive migration is
  enabled only for debug builds.
- Add a migration test when the schema changes.

## Content contributions

- Only submit questions or artwork that you created or have explicit permission
  to use.
- Do not copy textbook, exam or worksheet content without permission.
- Content contributed to this repository is licensed under Apache-2.0.

## Pull requests

- Describe the user-visible change and the technical approach.
- List the tests you ran and any device/ROM checks you performed.
- Update the relevant README section when behavior, setup or permissions change.
- Keep commit messages focused; do not add automated bot co-author trailers.

## Security

Please do not report vulnerabilities in public issues. See [SECURITY.md](SECURITY.md).

## License

By contributing, you agree that your contributions are licensed under the
Apache License 2.0. See [LICENSE](LICENSE).
