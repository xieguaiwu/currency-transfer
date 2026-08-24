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

```yaml
Categories:
  - Market & Price
License: MIT
AuthorName: xieguaiwu
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
AntiFeatures:
  - NonFreeNet
AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
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
- Reproducible build verified (dual-build SHA-256 match at tag v1.0.0)
- Fastlane metadata (en-US / zh-CN), real Paparazzi-rendered screenshots
- Category Market & Price (validated against config/categories.yml)

## Build
`gradle: yes`, `subdir: app`, commit v1.0.0 (clean tree, wrapper committed)
```

## 评审关注点（reviewer 可能问）

- **NonFreeNet**：应用依赖 open.er-api.com 和 World Bank API（公开免费）——已声明
- **数据来源**：full_description 已说明两个 API
- **可复现性**：tag v1.0.0 双构建 SHA-256 一致（`7b872bf5...`）
- **许可证**：MIT（LICENSE 在仓库根）

MR 合并后 24-48 小时出现在 F-Droid 主仓库（签名步骤人工介入）。
