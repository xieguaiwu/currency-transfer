# CONTEXT_FOR_NEXT_AGENT.md

## 项目当前状态
FX Pixel（currency-transfer）— Android 应用（Kotlin/Compose），全球货币实时汇率 + 年份间通胀计算，复古像素风。
v1.0.0 已完成：UI 精修 + 漏洞测试 + F-Droid 发布准备 + GitHub 远程/CI/Release。
**远程仓库**：https://github.com/xieguaiwu/currency-transfer（PUBLIC，默认 master，tag v1.0.0）

## 最后一次完成的工作（2026-08-24 第四轮：货币切换修复）
- **致命交互 bug 修复**：货币选择字段点击无反应（readOnly OutlinedTextField 消费 pointer 事件 → 外层 clickable 永不触发）。修复：Box 包裹 + matchParentSize 透明 overlay 捕获点击（CurrencyPicker.kt）
- **新增 CurrencyPickerInteractionTest**：performTouchInput { click() } 物理注入测试（语义 performClick 假阳性测不出此 bug）；修复前 2/3 失败 → 修复后 3/3 通过；全套 51/51 绿 + lint 干净
- 经验已沉淀：~/prompt_boilerplates/Coding/android-development.md（安卓开发 skill，§2 入口测试 / §3 触摸交互铁律）

## 最后一次完成的工作（2026-08-24 第三轮：F-Droid 提交包 + 手机推送）
- **fdroiddata 提交包就绪**：docs/fdroid/SUBMIT_GUIDE.md（Web IDE 方法 A / git am 方法 B）+ fdroiddata-mr-0001.patch（45 行，git am 即用）+ metadata 内容 + MR 描述草稿
- **分类修正（重要）**：实测官方 fdroiddata config/categories.yml —— **没有 "Money" 分类**（web_search 信息过时）；正确 = **Market & Price**（价格/汇率/股票）；Builds 补 **subdir: app**（否则 buildserver 构建失败）
- **校验脚本升级**：scripts/validate-fdroid-metadata.sh 读取官方 categories.yml 白名单 + 强制 subdir: app → 本地通过
- **buildserver 模拟验证**：tag v1.0.0 干净树 assembleRelease 成功，SHA-256 7b872bf5 与可复现验证一致
- **手机推送脚本**：scripts/push-apk-to-phone.sh（adb push 到 /sdcard/Download/FX-Pixel-debug.apk）
- **USB 诊断结论**：华为 NOH-AN00 物理连接存在（12d1:107e），当前模式 hisuite_mtp_mass_storage_hdb（无 ADB 接口）→ adb 不可见；系统缺 gvfsd-mtp/libmtp（装包需 sudo）→ MTP 挂载不可行；**待用户手机开启 USB 调试**后运行脚本

## 遗留问题 / 待办
- [ ] **手机 USB 调试**：用户手机开启 USB 调试 + 允许授权后，运行 `scripts/push-apk-to-phone.sh` 推送 APK（诊断：华为 NOH-AN00 当前无 ADB 接口）
- [ ] **fdroiddata MR**：需用户 GitLab 账号（无 glab/token/Chrome 会话）；提交包已就绪 docs/fdroid/SUBMIT_GUIDE.md，fork 后 2 分钟可提 MR
- [ ] 截图由 Paparazzi 生成（真实 UI 代码），建议真机侧载替换（skill §3.2）
- [ ] 可选：Verified 徽章路线（需自有签名 keystore，首次发布前决策；当前 F-Droid 官方签名）
- [ ] 可选：汇率/CPI 本地缓存、汇率历史图

## CI / 发布
- GitHub Actions .github/workflows/ci.yml：push/PR → 单测 + Lint + assembleRelease + APK artifact（远程实测全绿）
- GitHub Release v1.0.0 已创建（unsigned APK + SHA-256 5fdcb0ba...；tag 处构建 SHA-256 7b872bf5）
- 仓库 topics 7 个；Discussions 未启用（API 不可设，需网页）
- 发版纪律：bump versionCode/versionName → tag v<ver> → fastlane changelogs/<versionCode>.txt → push tags

## 远程资源
- GitHub 远程：origin → https://github.com/xieguaiwu/currency-transfer.git（PUBLIC，master；gh CLI 已认证 xieguaiwu）
- 数据源：open.er-api.com（汇率，免费无 key）+ World Bank（CPI，免费无 key）—— 无其他远程依赖

## 知识图谱
- graphify-out/: 不存在（<10 源文件小项目，未建图谱）

## 关键架构
```
MainActivity → MainScreen(Tab+header) → ExchangeScreen / InflationScreen
  └─ ExchangeViewModel / InflationViewModel（接口注入，可测试）
  └─ data/  Currencies(货币表+ISO3) · ExchangeRateApi(ExchangeRateSource) · WorldBankApi(CpiSource) · InflationCalculator(纯函数)
```
- 安全：res/xml/network_security_config.xml（HTTPS only）、backup_rules.xml、data_extraction_rules.xml
- 像素风：ui/theme/Theme.kt（PICO-8 palette + PressStart2P + 方形 shapes）；字体 res/font/press_start_2p.ttf（OFL）
- 截图测试：AppScreenshotsTest.kt（Paparazzi 1.3.5，fake sources）
- 发布产物：fastlane/metadata/（en-US/zh-CN）、scripts/verify-reproducible.sh、scripts/validate-fdroid-metadata.sh、scripts/push-apk-to-phone.sh、docs/fdroid/

## 最后更新时间
2026-08-24 15:30
