# CONTEXT_FOR_NEXT_AGENT.md

## 项目当前状态
FX Pixel（currency-transfer）— Android 应用（Kotlin/Compose），全球货币实时汇率 + 年份间通胀计算，复古像素风。
v1.0.0 已完成 UI 精修 + 漏洞测试 + F-Droid 发布准备（tag v1.0.0）。
**远程仓库已创建并推送**：https://github.com/xieguaiwu/currency-transfer（PUBLIC，master，含 tag v1.0.0）

## 最后一次完成的工作（2026-08-24 第二轮）
- **多模态界面调查**：Paparazzi（JVM 渲染真实 UI，无设备可用）生成截图，视觉审查
  - 修复 Bug 1：通胀 Note 泄漏 `CpiPoint.toString()`（Kotlin `$from.year` 陷阱 → `${from.year}`）
  - 修复 Bug 2：汇率时间戳 `Mon, 24 Aug 2026 00:02:31 +0000` 溢出换行 → RFC1123 解析后本地紧凑格式
- **像素风精修**：PICO-8 配色（#1D2B53 navy/#FFEC27 yellow）深色主题、Press Start 2P 像素字体（OFL，assets 含许可）、方形形状、金边卡片、像素 $ launcher icon；Button/TextButton 显式 shape=shapes.small（Material3 默认 CornerFull 不随 theme）
- **漏洞测试**：
  - Lint：0 error 0 warning（修 3 警告：DataExtractionRules/fullBackupContent、mipmap-anydpi-v26→anydpi、Monochrome 图标）
  - 静态审计：无硬编码密钥/明文 HTTP/日志/反射/WebView；权限仅 INTERNET；导出组件最小
  - APK 二进制：签名/权限/组件核查，仅 INTERNET + 标准 AndroidX 组件
  - 依赖：OkHttp 4.12、okio 3.6、Compose 1.7.4，无已知 CVE
  - 网络：全 HTTPS、TLS 校验、network_security_config 禁明文+仅系统 CA；备份全面禁用
- **F-Droid 上线准备**：
  - 合规预检全过（MIT/依赖全 FOSS/无专有二进制/权限最小/版本标准）
  - 反特性：NonFreeNet（依赖公开 API，需声明，不阻止）
  - fastlane 元数据 en-US/zh-CN（short/full/changelog/icon 512/真实 UI 截图 ×2）
  - 可复现构建实测通过：verify-reproducible.sh 双构建 SHA-256 一致（7b872bf5）
  - fdroiddata 草稿 docs/fdroid/com.xieguiawu.currencytransfer.yml（Category: **Market & Price** —— 实测官方 config/categories.yml：无 Money 分类！Market & Price 是汇率应用正确分类；subdir: app 必加）
- 数据源重构：ExchangeRateSource/CpiSource 接口化（fake 可注入，Paparazzi 测试用）

## 遗留问题 / 待办
- [ ] **fdroiddata MR**：需用户 GitLab 账号；**提交包已就绪** docs/fdroid/SUBMIT_GUIDE.md（step-by-step 指引）+ fdroiddata-mr-0001.patch（可直接 git am）+ 本地验证分支 commit 2a421f44（/tmp 已清理，补丁即可重现）
- [ ] 截图由 Paparazzi 生成（真实 UI 代码），建议真机侧载替换以更贴近实际（skill §3.2 建议）
- [ ] 真机冒烟（adb 无设备）；可复现构建在本机验证通过（tag v1.0.0 干净树，SHA-256 7b872bf5）
- [ ] 可选：Verified 徽章路线（需自有签名 keystore，首次发布前决策；当前 F-Droid 官方签名）
- [ ] 可选：汇率/CPI 本地缓存、汇率历史图

## CI / 发布
- GitHub Actions .github/workflows/ci.yml：push/PR → 单测 + Lint + assembleRelease + APK artifact（远程实测全绿）
- GitHub Release v1.0.0 已创建（含 unsigned APK + SHA-256 5fdcb0ba...）
- 仓库 topics 已设 7 个；Discussions 未启用（API 不可设，需网页）

## 远程资源
无（数据来自公开免费 API）

## 知识图谱
- graphify-out/: 不存在（小项目）

## 远程资源
- GitHub 远程：https://github.com/xieguaiwu/currency-transfer（PUBLIC，默认 master；gh CLI 已认证 xieguaiwu）

## 关键架构
```
MainActivity → MainScreen(Tab+header) → ExchangeScreen / InflationScreen
  └─ ExchangeViewModel / InflationViewModel（接口注入，可测试）
  └─ data/  Currencies(货币表+ISO3) · ExchangeRateApi(ExchangeRateSource) · WorldBankApi(CpiSource) · InflationCalculator(纯函数)
```
- 安全：res/xml/network_security_config.xml（HTTPS only）、backup_rules.xml、data_extraction_rules.xml
- 像素风：ui/theme/Theme.kt（PICO-8 palette + PressStart2P + 方形 shapes）；字体 res/font/press_start_2p.ttf（OFL）
- 截图测试：AppScreenshotsTest.kt（Paparazzi 1.3.5，fake sources）
- 发布产物：fastlane/metadata/、scripts/verify-reproducible.sh、docs/fdroid/*.yml

## 最后更新时间
2026-08-24 14:50
