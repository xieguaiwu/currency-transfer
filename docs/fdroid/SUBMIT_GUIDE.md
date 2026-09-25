# F-Droid 收录提交指引（FX Pixel）

本目录包含提交流程所需的一切。你只需要一个有 GitLab 的账号，约 2 分钟完成。

> ✅ **已提交**：[MR !48683](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/48683)（2026-09-12），等待审核（排期常 1-4 周）。以下内容保留作记录；fork CI 因新账号身份验证不可用（零 job），本地 `fdroid lint`（2.4.5）exit 0。
>
> 🔄 **审核第一轮已响应**（2026-09-15，reviewer linsui）：MR 描述已换成官方 App Inclusion 模板+勾选框（标题 `New app: FX Pixel`）、`commit` 钉全 hash、单 Build、NonFreeNet 补理由、元数据 rewritemeta 规范形；本地已复刻 CI 全套（rewritemeta/lint/checkupdates/tools/**fdroid build 端到端**/scanner）全绿，待维护者重触发上游 CI。本文件的原始提交说明保留作记录；`fdroiddata-mr-0001.patch` 已按当前分支重生成（基于最新上游 master，仍可直接 `git am`）。
>
> 🔄 **审核第二轮已响应**（2026-09-25）：联系邮箱换可达地址 `xieguaiwu@163.com`（reviewer 行内点名）；元数据在与 CI 一致依赖集（ruamel.yaml 0.18.10 + fdroidserver master a35fddd）下重新 canonical 化——`NonFreeNet` 理由折行同步修正。已推 fork 分支（add-currency-transfer）并逐条回复 reviewer，待其重触发上游 CI。

## 已就绪的文件

| 文件 | 用途 |
|---|---|
| `com.xieguiawu.currencytransfer.yml` | fdroiddata metadata（已按官方 categories.yml 验证：Market & Price + subdir: app + NonFreeNet）|
| `fdroiddata-mr-0001.patch` | 完整 commit 补丁（metadata 文件，可直接 `git am`）|
| `../screenshots/` | 应用真实截图（fastlane 也有一份）|

## 提交方法（二选一）

### 方法 A：Web 界面（最简单，无需本地 GitLab 配置）

1. 打开 https://gitlab.com/fdroid/fdroiddata
2. 点右上角 **Fork**（fork 到你自己的账号）
3. 在你的 fork 里打开 **Web IDE**（或 "+" → "New file"）
4. 新建路径：`metadata/com.xieguiawu.currencytransfer.yml`
5. 粘贴下方「metadata 内容」段的完整内容
6. 提交到新分支（如 `add-currency-transfer`）
7. 回到 fork 页面，点 **Create merge request**（来源分支 = 你的新分支，目标 = fdroid/fdroiddata master）
8. MR 标题：`Add FX Pixel (com.xieguiawu.currencytransfer)`
9. MR 描述：粘贴下方「MR 描述」段

### 方法 B：本地 git（需 GitLab 账号 SSH/HTTPS 认证）

```bash
git clone https://gitlab.com/fdroid/fdroiddata.git
cd fdroiddata
git checkout -b add-currency-transfer
git am /path/to/docs/fdroid/fdroiddata-mr-0001.patch   # 或手动创建 metadata 文件
git remote add mine <你的-fork-地址>
git push mine add-currency-transfer
# 在 GitLab 网页创建 MR: 你的 fork:add-currency-transfer → fdroid/fdroiddata:master
```

## metadata 内容

> 与 `com.xieguiawu.currencytransfer.yml` 逐字一致（改一处必改两处）。
> 校验：`bash scripts/validate-fdroid-metadata.sh docs/fdroid/com.xieguiawu.currencytransfer.yml`

```yaml
# F-Droid metadata for com.xieguiawu.currencytransfer (FX Pixel)
# 提交位置：gitlab.com/fdroid/fdroiddata → metadata/com.xieguiawu.currencytransfer.yml
# 校验：bash scripts/validate-fdroid-metadata.sh docs/fdroid/com.xieguiawu.currencytransfer.yml
# ⚠️ 与 docs/fdroid/com.xieguiawu.currencytransfer.yml 及 fdroiddata MR 分支逐字一致（2026-09-25 校验；改一处必改两处）。
# 注意：Builds 只列已打 tag 的版本；HEAD 上未发版的改动不得出现在这里。
# 可复现性（unsigned 比对；签名 APK 逐构建不同）：
#   v1.0.2 -> 80353964339ee0d13a6c658fc0251b306d3002d1ba4bf345c284491f671b1be7（2026-09-25 于 tag 双构建复测）
#   v1.0.0 -> 55d73c405b78a94b70f193523ccab38a6e11371f33a11082afb015238dbf97ad
#   v1.0.1 -> 6875b026f90bb267d123c210bbd6016434e01fffd856607194e2c9b744c6798c
AntiFeatures:
  NonFreeNet:
    en-US: Uses the proprietary open.er-api.com and World Bank API services for exchange
      rates and inflation data.
Categories:
  - Market & Price
License: MIT
AuthorName: xieguaiwu
AuthorEmail: xieguaiwu@163.com
SourceCode: https://github.com/xieguaiwu/currency-transfer
IssueTracker: https://github.com/xieguaiwu/currency-transfer/issues
Changelog: https://github.com/xieguaiwu/currency-transfer/releases

AutoName: FX Pixel

RepoType: git
Repo: https://github.com/xieguaiwu/currency-transfer

Builds:
  - versionName: 1.0.2
    versionCode: 3
    commit: 3d0bd8789e3a75acd72953c41b1179f49fa5fb95
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.2
CurrentVersionCode: 3
```

## MR 描述

```markdown
## Summary
Add FX Pixel (com.xieguiawu.currencytransfer) — a retro-pixel Android app
for global currency exchange rates (open.er-api.com) and inflation
calculation (World Bank CPI).

## Details
- MIT licensed, keyless public data sources (NonFreeNet declared)
- Single INTERNET permission, HTTPS only, zero tracking
- Reproducible build verified at tags v1.0.0 / v1.0.1 / v1.0.2 (unsigned comparison)
- Fastlane metadata (en-US / zh-CN); screenshots are real-device captures (2026-09-12)
- Category Market & Price (validated against config/categories.yml)

## Build
`gradle: yes`, `subdir: app`, commit `3d0bd878…` (full hash, v1.0.2; clean tree, wrapper committed)
Single Build entry (v1.0.2); older versions removed per review.
```

## 评审关注点（reviewer 可能问）

- **NonFreeNet**：应用依赖 open.er-api.com 和 World Bank API（公开免费）——已声明
- **数据来源**：full_description 已说明两个 API
- **可复现性**（unsigned 比对；2026-09-25 增补 v1.0.2）：
  - tag `v1.0.2` → `80353964339ee0d13a6c658fc0251b306d3002d1ba4bf345c284491f671b1be7`（双构建复测）
  - tag `v1.0.0` → `55d73c405b78a94b70f193523ccab38a6e11371f33a11082afb015238dbf97ad`（历史）
  - tag `v1.0.1` → `6875b026f90bb267d123c210bbd6016434e01fffd856607194e2c9b744c6798c`（历史）
  - 旧记录里的 `7b872bf5...` **无法复现**，已弃用；比对必须去签名——
    AGP 8.x 用 RSA-PSS 签名，随机 salt 使签名 APK 逐构建不同（实测同 commit
    两次签名构建 `5f61f0fb...` vs `537ec282...`）
- **许可证**：MIT（LICENSE 在仓库根）
- **subdir: app**：标准多模块 Gradle 工程（root 有 settings.gradle.kts + wrapper），
  fdroiddata 同类工程（Markor / AppManager / DejaVu）均用 `subdir: app`，非笔误

## 提交前自检清单

- [x] `git ls-remote --tags origin` 含 yml 里每一个 `commit:` 值（当前：v1.0.2 全 hash `3d0bd878…`）
- [x] `fastlane/metadata/android/en-US/changelogs/` 有与 versionCode 同名的文件（当前 vc3 → `3.txt`）
- [x] `bash scripts/validate-fdroid-metadata.sh docs/fdroid/com.xieguiawu.currencytransfer.yml` 通过（2026-09-25 复跑）
- [x] 截图已换真机实截（2026-09-12，1152x2250；fastlane en-US + zh-CN + README docs/screenshots 同步）

MR 合并后 24-48 小时出现在 F-Droid 主仓库（签名步骤人工介入）。
