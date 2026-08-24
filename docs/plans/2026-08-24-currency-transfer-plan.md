# Currency Transfer & Inflation App — Implementation Plan

> **For agentic workers:** 按本计划任务逐项实施。步骤用 checkbox（`- [ ]`）跟踪。

**Goal:** 构建一个 Android 应用，提供全球货币实时汇率转换，以及任意两种年份之间的通货膨胀/购买力计算。

**Architecture:** 单 Activity + Jetpack Compose + Material3，两个 Tab（Exchange / Inflation）。数据层用 OkHttp + kotlinx.serialization 调用两个免费无 key API；核心计算逻辑（通胀）为纯 Kotlin 可单测函数。

**Tech Stack:** Kotlin 2.0.21, AGP 8.5.2, Compose BOM 2024.10.00, OkHttp 4.12.0, kotlinx-serialization 1.7.3, minSdk 26, targetSdk 35。

**Spec:** 用户需求（中文）：「全球货币实时汇率 + 货币从年份1到年份2之间的通货膨胀情况」。

## Global Constraints

- 数据源 1（汇率）：`https://open.er-api.com/v6/latest/{BASE}`，免费无 key，返回 `rates` map + `time_last_update_utc`。已实测可用（2026-08-24）。
- 数据源 2（通胀）：`https://api.worldbank.org/v2/country/{ISO3}/indicator/FP.CPI.TOTL?format=json&per_page=200&date=1990:2026`，返回年度 CPI 指数（2010=100），降序排列，`value` 可能为 null（当年未发布）。已实测可用。
- 欧元区映射：EUR → `EMU`（World Bank "Euro area"，已实测有效）。
- 无 API key，无敏感信息。`.gitignore` 必须包含 `local.properties`、`.gradle/`、`build/`。
- 包名：`com.xieguiawu.currencytransfer`。UI 文本用英文（ASD-STE100 短句规范）。
- 金额计算用 Double 足够（显示精度 4 位小数），输入框过滤负数/非法字符。
- 网络错误必须显示可读错误消息 + 重试按钮，禁止崩溃。

## 任务分解

### Task 1: 项目脚手架

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `local.properties`, `.gitignore`
- Copy: `gradle/wrapper/*`, `gradlew`, `gradlew.bat` ← 从 `~/Desktop/android-projects/api-checkers/` 复制（Gradle 8.9 wrapper）
- Create: `app/build.gradle.kts`（namespace `com.xieguiawu.currencytransfer`，dependencies 同 api-checkers：compose-bom、material3、okhttp、kotlinx-serialization-json、core-ktx、activity-compose、lifecycle-viewmodel-compose、material-icons-extended；test：junit + kotlinx-coroutines-test）
- Create: `app/src/main/AndroidManifest.xml`（INTERNET 权限、usesCleartextTraffic=false、theme）
- Create: `app/src/main/res/values/strings.xml` + `themes.xml`（Material3 主题，无 ActionBar）

**Produces:** 可编译的空 Android 项目骨架。

- [ ] Step 1: 复制 wrapper 与 gradle 配置文件
- [ ] Step 2: 写 app/build.gradle.kts + manifest + res
- [ ] Step 3: `./gradlew assembleDebug` 验证骨架编译

### Task 2: 数据层 + 核心计算（含测试）

**Files:**
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/data/Currencies.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/data/ExchangeRateApi.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/data/WorldBankApi.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/data/ApiClient.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/data/InflationCalculator.kt`
- Test: `app/src/test/java/com/xieguiawu/currencytransfer/InflationCalculatorTest.kt`
- Test: `app/src/test/java/com/xieguiawu/currencytransfer/CurrenciesTest.kt`

**Interfaces:**
- Produces:
  - `data class CurrencyInfo(code: String, name: String, countryIso3: String?)`
  - `object Currencies { val all: List<CurrencyInfo>; fun iso3For(code: String): String?; fun displayName(code: String): String }`
  - `data class ExchangeRates(baseCode: String, updatedUtc: String, rates: Map<String, Double>)`
  - `data class CpiPoint(year: Int, value: Double)`
  - `class ExchangeRateApi { suspend fun fetchRates(base: String): ExchangeRates }`
  - `class WorldBankApi { suspend fun fetchCpi(iso3: String): List<CpiPoint> }`
  - `object InflationCalculator { fun nearestCpi(cpi: List<CpiPoint>, year: Int): CpiPoint?; fun cumulativeInflation(cpi: List<CpiPoint>, fromYear: Int, toYear: Int): Double?; fun annualRate(cpi: List<CpiPoint>, fromYear: Int, toYear: Int): Double?; fun purchasingPower(cpi: List<CpiPoint>, fromYear: Int, toYear: Int, amount: Double): Double? }`
- Consumes: Task 1 骨架

- [ ] Step 1: 写 `InflationCalculatorTest`（失败先行）——累计通胀、年均率、购买力、缺失年份取最近非空、year1≥year2、空列表、null 值跳过
- [ ] Step 2: 实现 `Currencies`（≥60 种常用货币，含 ISO3 映射；EUR→EMU、CNY→CHN、USD→USA、JPY→JPN、GBP→GBR、INR→IND 等；加密货币/无 CPI 货币 iso3=null）
- [ ] Step 3: 实现 `InflationCalculator` 纯函数
- [ ] Step 4: 实现 `ApiClient` + `ExchangeRateApi`（GET latest/USD，JSON 解析，超时 15s）
- [ ] Step 5: 实现 `WorldBankApi`（GET country/{iso3}/indicator/FP.CPI.TOTL，per_page=200，date=1990:2026，解析为 List<CpiPoint>，过滤 null）
- [ ] Step 6: `./gradlew testDebugUnitTest` 全绿

### Task 3: UI 层

**Files:**
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/ui/MainScreen.kt`（Tab 导航）
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/ui/ExchangeScreen.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/ui/InflationScreen.kt`
- Create: `app/src/main/java/com/xieguiawu/currencytransfer/MainActivity.kt`

**Interfaces:**
- Consumes: Task 2 全部接口
- Produces: 可交互 UI（状态：Loading / Error(message) / Success）

- [ ] Step 1: Theme + MainActivity + Tab 框架
- [ ] Step 2: ExchangeScreen——from/to 货币下拉（含搜索过滤）、金额输入（数字键盘、过滤负号）、实时换算（1:1 双向，输入任一方向）、更新时间显示、刷新按钮、错误重试
- [ ] Step 3: InflationScreen——货币下拉、year1/year2 输入（1990..2026 范围校验）、结果卡片（累计通胀 %、年均通胀率、购买力等价：X@year1 ≈ Y@year2）、无 CPI 货币提示、错误重试
- [ ] Step 4: `./gradlew assembleDebug` 通过

### Task 4: 文档 + 完成验证

**Files:**
- Create: `README.md` + `README_zh.md`（双语，互链）
- Create: `CONTEXT_FOR_NEXT_AGENT.md`
- Create: `LICENSE`（MIT，与 api-checkers 一致）

- [ ] Step 1: 双语 README + LICENSE + CONTEXT
- [ ] Step 2: `./gradlew testDebugUnitTest assembleDebug` 全绿，记录 APK 路径
- [ ] Step 3: git init + 首次提交（关卡 9：提交前 grep 无 key）
