# 货币换算与通胀计算

[**English**](README.md) | [**中文版**](#)

注重隐私的安卓应用：全球货币实时汇率 + 任意年份之间的通货膨胀计算。

## 功能

- **汇率换算**：160+ 种货币的实时汇率（open.er-api.com，免费、无需密钥）
- **通胀计算**：使用世界银行 CPI 数据，比较任意两年（1990–2026）之间的购买力
- **购买力对比**：如 2015 年的 100 美元 ≈ 2025 年的多少钱，含累计通胀率与年均通胀率
- **货币搜索**：按代码或名称快速查找
- **无广告、无跟踪、无账号**：仅 INTERNET 权限，禁用明文流量

## 界面

| Tab | 功能 |
|---|---|
| Exchange（汇率） | 输入金额、选择两种货币，查看实时换算结果与双向汇率 |
| Inflation（通胀） | 选择货币、输入两个年份，查看累计通胀率、年均通胀率与购买力等价金额 |

## 数据源

| 来源 | 数据 | 许可/密钥 |
|---|---|---|
| [open.er-api.com](https://www.exchangerate-api.com/docs/free) | 实时汇率（166 种货币） | 免费，无需密钥 |
| [World Bank API](https://data.worldbank.org/indicator/FP.CPI.TOTL) | 年度 CPI（2010 年 = 100） | 免费，无需密钥 |

说明：
- 世界银行没有发布欧元区的 CPI 指数。应用自动回退到年度通胀率序列并重建指数，
  比值计算（累计通胀、购买力）仍然精确。
- 最新年份的数据往往尚未发布；应用使用最近可用的数据点。

## 构建

要求：JDK 17+、Android SDK 35。

```bash
./gradlew assembleDebug
# APK 路径: app/build/outputs/apk/debug/app-debug.apk
```

## 测试

```bash
./gradlew testDebugUnitTest
# 42 个单元测试：通胀计算、货币表、API 响应解析
```

## 隐私

- 唯一权限：INTERNET（获取汇率与 CPI 所需）
- 无遥测、无广告、无统计
- 本地不存储任何数据；请求直接发往公开数据源
- 禁用明文 HTTP，全部走 HTTPS

## 许可证

MIT — 见 [LICENSE](LICENSE)。
