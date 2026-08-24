# Contributing to FX Pixel

Thanks for your interest in FX Pixel. This document explains how to contribute.

## Ways to contribute

- Report a bug in [Issues](https://github.com/xieguaiwu/currency-transfer/issues)
- Suggest a feature in [Discussions](https://github.com/xieguaiwu/currency-transfer/discussions)
- Submit a code change via a pull request

## Code style

This project follows the official Kotlin style. Use the project's Gradle
configuration when you open it in Android Studio.

Before you submit a change:

1. Run `./gradlew testDebugUnitTest` — all tests must pass.
2. Run `./gradlew lintDebug` — no new errors or warnings.
3. Keep user-visible text short and consistent (ASD-STE100).

## Data and API

- FX Pixel uses only free, keyless public data sources.
- Do not add a data source that requires an API key (the app is F-Droid friendly).
- Document any new data source in the README.
- Add a test fixture from a real response under `app/src/test/resources/`.

## Translations

The app UI is English. Store metadata lives in `fastlane/metadata/android/`
(en-US, zh-CN). Add a locale folder for a new translation.

## Privacy rule

FX Pixel has zero tracking and zero analytics. Do not add analytics,
advertising, or any SDK that collects data. Only the INTERNET permission
is allowed.

## License

By contributing, you agree that your contributions are licensed under the
MIT License (see [LICENSE](LICENSE)).
