# CONTEXT_FOR_NEXT_AGENT.md

## 项目当前状态
currency-transfer — Android 应用（Kotlin/Compose），全球货币实时汇率 + 年份间通胀计算。
已完成 v1.0.0：全部功能实现，42 个单元测试全绿，APK 构建成功。

## 最后一次完成的工作
- 2026-08-24：项目从零搭建完成
  - 汇率 Tab：open.er-api.com 实时汇率（166 货币），金额输入 + 双向换算 + 刷新/错误重试
  - 通胀 Tab：World Bank CPI（FP.CPI.TOTL，2010=100），1990-2026 年份区间，
    累计通胀 + 年均通胀率 + 购买力等价；货币选择带搜索
  - **欧元区 fallback**：WB 不发布 EMU/EUU 的 CPI 指数（全 null）→ 自动回退
    FP.CPI.TOTL.ZG（年度通胀率）重建指数，比值精确（已验证 36 点）
  - 42 测试：InflationCalculator(17) / Currencies(8) / ExchangeRateApi(5) /
    WorldBankApi(8) / 真实响应样本解析（2026-08-24 固化）
  - 端到端 smoke 验证（已删临时测试）：USD 2015→2025 通胀 32.3%，年均 3.16%
  - 文档：README.md + README_zh.md + LICENSE(MIT) + docs/plans/ 实施计划

## 遗留问题 / 待办
- [ ] 真机冒烟测试（本机无连接设备；adb devices 为空）
- [ ] 可选：汇率/CPI 本地缓存（当前每次启动重新拉取）
- [ ] 可选：夜间模式/深色主题（当前仅浅色）
- [ ] 可选：汇率历史趋势图（需要历史汇率 API，如 frankfurter）

## 远程资源
无（纯本地项目；数据来自公开免费 API）

## 知识图谱
- graphify-out/: 不存在（项目 <10 源文件，未建图谱）

## 关键架构
```
MainActivity → MainScreen(Tab) → ExchangeScreen / InflationScreen
  └─ ExchangeViewModel / InflationViewModel（fetch + 错误映射）
  └─ data/  Currencies(货币表+ISO3) · ExchangeRateApi · WorldBankApi · InflationCalculator(纯函数)
```
- 数据源切换不敏感：ExchangeRateApi 请求 latest/USD；WorldBankApi 请求
  country/{iso3}/indicator/FP.CPI.TOTL，空时 fallback FP.CPI.TOTL.ZG
- InflationCalculator 对 CPI 序列顺序不敏感（升序/降序均可）
- 测试资源 app/src/test/resources/ 固化真实 API 响应（防契约漂移）

## 最后更新时间
2026-08-24 14:30
