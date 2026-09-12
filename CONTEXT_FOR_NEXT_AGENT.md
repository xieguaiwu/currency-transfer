# CONTEXT_FOR_NEXT_AGENT.md

## 项目当前状态
FX Pixel（currency-transfer）— Android 应用（Kotlin/Compose），全球货币实时汇率 + 年份间通胀计算，复古像素风。
v1.0.1 已完成：UI 精修 + 漏洞测试 + F-Droid 发布准备 + GitHub 远程/CI/Release。
**远程仓库**：https://github.com/xieguaiwu/currency-transfer（PUBLIC，默认 master，tag v1.0.1）

## 最后一次完成的工作（2026-08-25：CPI 数据基础核查 + 可靠性修复）
- **全量实弹核查**：139 个 ISO3 映射全部实测 World Bank API（模拟真实请求参数），零映射错误；发现 5 个永久无数据货币（TWD/CUP/SOS/TMT/ERN）与 ~15 个短/旧序列国家（VEN/ZWE/SDN/YEM 等），API 首轮超时率 14%
- **修复 1（死货币）**：TWD/CUP/SOS/TMT/ERN → countryIso3=null，从通胀页选择器移除（Currencies.kt + CurrenciesTest 新增验证测试）
- **修复 2（缓存）**：新增 DiskCpiCache（7 天 TTL、原子写、损坏自愈）+ CpiCache 接口；WorldBankApi 注入 cache + client 参数；缓存命中零网络、网络错误回退最近缓存（离线可用）；InflationScreen 用 viewModelFactory 接缓存（applicationContext.cacheDir/cpi-cache）
- **测试**：DiskCpiCacheTest 9 项（roundtrip/TTL/隔离/损坏/跨实例）+ WorldBankApiTest 新增 5 项 MockWebServer 测试（缓存命中、缓存写入、离线回退、无缓存报错、EMU 回退链入缓存）；新增 mockwebserver 4.12.0 测试依赖

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

## 2026-09-06 F-Droid 复核轮次
- yml 补 **v1.0.1（versionCode 2）** Build 块并同步 CurrentVersion——此前只声明 1.0.0，
  一上线就落后实际发布版
- MR 补丁重生成并在干净树 `git am` 验证通过；SUBMIT_GUIDE 内嵌 metadata 与 yml **逐字一致**（脚本校验）
- `validate-fdroid-metadata.sh` 升级为通用版：校验 commit: 是真 tag、每个 versionCode 有
  en+zh changelog、CurrentVersion 与最新 Builds 项一致
- 🔴 **可复现性结论修正**：旧记录 `7b872bf5` 无法复现，且 `verify-reproducible.sh` 现在会
  **假失败**——AGP 8.x 用 RSA-PSS 签名，随机 salt 落在 APK Signing Block，签名 APK 逐构建不同
  （实测同 commit 两次 `5f61f0fb...` vs `537ec282...`）。旧结论之所以成立，是因为那次验证
  跑在 `keystore.properties` 创建**前 5 分钟**，无意中比的是 unsigned 产物。
  脚本已改为临时藏起 keystore + 只比 `*-unsigned.apk`（与 F-Droid apksigcopier 同法）。
  在 tag 的干净 worktree 上重测：v1.0.0 → `55d73c40...dbf97ad`，v1.0.1 → `6875b026...4c6798c`，双构建一致 ✅
- ⚠️ **推论**：作者其余安卓 app 凡称「签名构建双哈希一致」的都需按同法重验
  （picture-trans 已重验并改；android-rebirth / api-checkers 本来就比 unsigned）

## 2026-09-12 真机截图批

- fastlane en-US + zh-CN phoneScreenshots（各 2 张）+ README docs/screenshots 全部换真机实截
  （1152x2250）；Paparazzi 渲染保留作视觉回归（脚本仍在）
- GitLab 账号已注册 → **fdroiddata 已提交**：MR !48683（2026-09-12）；fork CI 因新账号
  身份验证零 job，本地 `fdroid lint`（2.4.5）exit 0；等待审核（排期常 1-4 周）

## 遗留问题 / 待办
- [ ] **手机 USB 调试**：用户手机开启 USB 调试 + 允许授权后，运行 `scripts/push-apk-to-phone.sh` 推送 APK（诊断：华为 NOH-AN00 当前无 ADB 接口）
- [x] **fdroiddata MR 已提交**：!48683（2026-09-12），等审核；响应 reviewer 需登录 GitLab 网页
- [x] 截图已换真机实截（2026-09-12；fastlane en-US+zh-CN + README docs/screenshots）
- [ ] 可选：Verified 徽章路线（需自有签名 keystore，首次发布前决策；当前 F-Droid 官方签名）
- [ ] 可选：汇率本地缓存、汇率历史图

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
  └─ data/  Currencies(货币表+ISO3) · ExchangeRateApi(ExchangeRateSource) · WorldBankApi(CpiSource，7 天磁盘缓存+离线回退) · CpiCache/DiskCpiCache · InflationCalculator(纯函数)
```
- 安全：res/xml/network_security_config.xml（HTTPS only）、backup_rules.xml、data_extraction_rules.xml
- 像素风：ui/theme/Theme.kt（PICO-8 palette + PressStart2P + 方形 shapes）；字体 res/font/press_start_2p.ttf（OFL）
- 截图测试：AppScreenshotsTest.kt（Paparazzi 1.3.5，fake sources）
- 发布产物：fastlane/metadata/（en-US/zh-CN）、scripts/verify-reproducible.sh、scripts/validate-fdroid-metadata.sh、scripts/push-apk-to-phone.sh、docs/fdroid/

## 最后更新时间
2026-09-12（真机截图批）
