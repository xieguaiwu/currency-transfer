# F-Droid 收录提交指引（FX Pixel）

本目录包含提交流程所需的一切。你只需要一个有 GitLab 的账号，约 2 分钟完成。

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
# 注意：Builds 只列已打 tag 的版本；HEAD 上未发版的改动不得出现在这里。
# 可复现性（2026-09-06 于 tag 实测，unsigned 比对；签名 APK 逐构建不同）：
#   v1.0.0 -> 55d73c405b78a94b70f193523ccab38a6e11371f33a11082afb015238dbf97ad
#   v1.0.1 -> 6875b026f90bb267d123c210bbd6016434e01fffd856607194e2c9b744c6798c
Categories:
  - Market & Price
License: MIT
AuthorName: xieguaiwu
AuthorEmail: xieguaiwu@users.noreply.github.com
SourceCode: https://github.com/xieguaiwu/currency-transfer
IssueTracker: https://github.com/xieguaiwu/currency-transfer/issues
Changelog: https://github.com/xieguaiwu/currency-transfer/releases
AutoName: FX Pixel
RepoType: git
Repo: https://github.com/xieguaiwu/currency-transfer
Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes

  - versionName: 1.0.1
    versionCode: 2
    commit: v1.0.1
    subdir: app
    gradle:
      - yes
AntiFeatures:
  - NonFreeNet
AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.1
CurrentVersionCode: 2
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
- Reproducible build verified at tag v1.0.0 (unsigned comparison; see below)
- Fastlane metadata (en-US / zh-CN), real Paparazzi-rendered screenshots
- Category Market & Price (validated against config/categories.yml)

## Build
`gradle: yes`, `subdir: app`, commit v1.0.1 (clean tree, wrapper committed)
Two Builds entries (v1.0.0 + v1.0.1) so the initial import carries history.
```

## 评审关注点（reviewer 可能问）

- **NonFreeNet**：应用依赖 open.er-api.com 和 World Bank API（公开免费）——已声明
- **数据来源**：full_description 已说明两个 API
- **可复现性**（2026-09-06 在 tag 上重测，unsigned 比对）：
  - tag `v1.0.0` → `55d73c405b78a94b70f193523ccab38a6e11371f33a11082afb015238dbf97ad`
  - tag `v1.0.1` → `6875b026f90bb267d123c210bbd6016434e01fffd856607194e2c9b744c6798c`
  - 旧记录里的 `7b872bf5...` **无法复现**，已弃用；比对必须去签名——
    AGP 8.x 用 RSA-PSS 签名，随机 salt 使签名 APK 逐构建不同（实测同 commit
    两次签名构建 `5f61f0fb...` vs `537ec282...`）
- **许可证**：MIT（LICENSE 在仓库根）
- **subdir: app**：标准多模块 Gradle 工程（root 有 settings.gradle.kts + wrapper），
  fdroiddata 同类工程（Markor / AppManager / DejaVu）均用 `subdir: app`，非笔误

## 提交前自检清单

- [ ] `git ls-remote --tags origin` 含 yml 里每一个 `commit:` 值（v1.0.0 / v1.0.1）
- [ ] `fastlane/metadata/android/en-US/changelogs/` 有与 versionCode 同名的文件（1.txt / 2.txt）
- [ ] `bash scripts/validate-fdroid-metadata.sh docs/fdroid/com.xieguiawu.currencytransfer.yml` 通过
- [ ] 截图为真机或至少与实物一致（当前为 Paparazzi 真实渲染，461x1000）

MR 合并后 24-48 小时出现在 F-Droid 主仓库（签名步骤人工介入）。
