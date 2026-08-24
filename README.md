# Currency Transfer

[**中文版**](README_zh.md) | [**English**](#)

A privacy-friendly Android app for global currency conversion and inflation calculation.

## Features

- **Exchange**: live exchange rates for 160+ currencies (open.er-api.com, free, keyless)
- **Inflation**: compare purchasing power between any two years (1990–2026) using World Bank CPI data
- **Buying power**: see how much a 2015 amount is worth in 2025, with cumulative and average annual inflation
- **Search**: find any currency by code or name
- **No ads, no tracking, no accounts**: single INTERNET permission, cleartext traffic disabled

## Screens

| Tab | What it does |
|---|---|
| Exchange | Enter an amount, pick two currencies, see the live conversion and both directions of the rate |
| Inflation | Pick a currency, enter two years, see cumulative inflation, average annual rate, and the purchasing-power equivalent |

## Data sources

| Source | Data | License/Key |
|---|---|---|
| [open.er-api.com](https://www.exchangerate-api.com/docs/free) | Live exchange rates (166 currencies) | Free, no key |
| [World Bank API](https://data.worldbank.org/indicator/FP.CPI.TOTL) | Annual CPI, 2010 = 100 | Free, no key |

Notes:
- The World Bank publishes no CPI index for the Euro area aggregate.
  The app falls back to the annual inflation-rate series and rebuilds the index.
  Ratios (cumulative inflation, purchasing power) remain exact.
- The latest calendar year is often unpublished; the app uses the most recent available data point.

## Build

Requirements: JDK 17+, Android SDK 35.

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Test

```bash
./gradlew testDebugUnitTest
# 42 unit tests: inflation math, currency table, API response parsing
```

## Privacy

- Single permission: INTERNET (needed to fetch rates and CPI)
- No telemetry, no ads, no analytics
- No data is stored locally; all requests go directly to the public data sources
- Cleartext HTTP is disabled; requests use HTTPS

## License

MIT — see [LICENSE](LICENSE).
