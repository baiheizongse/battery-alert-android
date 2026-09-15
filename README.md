# Battery Alert（バッテリー通知アプリ）

バッテリー残量が指定した%に達すると、**音声で**知らせてくれる Android アプリです。

- **English**: [English](#english)
- **中文**: [中文](#中文)
- **日本語**: [日本語](#日本語)

---

## English

**Battery Alert** is an Android app that speaks aloud when the battery level reaches a set percentage.

### Why I made it

My mother often forgets to charge her phone, so I made this app to help prevent the battery from running too low (or from being overcharged).

### Features

- **Detects both upper and lower limits**: notifies you both when the battery gets too low and when it is fully charged.
- **Speaks aloud**: a beep or a plain notification doesn't tell you *what* happened, so this app explains it with voice (e.g. "The battery is now 80%").

### How to use

1. Open the app and set the battery level(s) you want to be notified at (lower and upper limit).
2. Monitoring starts in the background.
3. When the battery reaches the set level, the app announces it by voice.

### Download

The latest APK is available from [Releases](../../releases).

### Tech

- Kotlin + Jetpack Compose
- Foreground service for battery monitoring
- TextToSpeech (TTS) voice notifications

### License

MIT License. See [LICENSE](LICENSE).

---

## 中文

**Battery Alert（电池提醒）** 是一款 Android 应用，当电量达到设定百分比时，会用**语音**提醒你。

### 开发原因

因为母亲经常忘记给手机充电，所以制作了这款应用，用来防止电量过低（或过度充电）。

### 特点

- **同时检测上下限**：电量过低时、以及充满电时，都会提醒。
- **语音播报**：铃声或普通通知无法说明发生了什么，因此本应用会用语音说明（例如「电量现在是 80%」）。

### 使用方法

1. 打开应用，设置希望提醒的电量（下限和上限）。
2. 应用会在后台开始监测电量。
3. 当电量达到设定值时，应用会用语音提醒。

### 下载

最新版 APK 请见 [Releases](../../releases)。

### 技术

- Kotlin + Jetpack Compose
- 前台服务监测电量
- TextToSpeech（TTS）语音通知

### 许可证

MIT License。详见 [LICENSE](LICENSE)。

---

## 日本語

**バッテリー通知**は、バッテリー残量が指定した%に達すると**音声で**知らせてくれる Android アプリです。

### 作った理由

母がスマホの充電を忘れてしまいがちなため作りました。充電し忘れ（残量が少なくなりすぎる）や、充電しっぱなし（過充電）を防ぐためのアプリです。

### 特徴

- **上限・下限の両方を検知**：残量が「少なくなりすぎたとき」と「十分に充電されたとき」の両方でお知らせします。
- **音声で読み上げ**：アラーム音や通知だけでは「何の知らせか」が分かりにくいので、音声で「バッテリーが○%になりました」と説明します。

### 使い方

1. アプリを起動して、通知したい残量（上限・下限）を設定します。
2. バックグラウンドでバッテリー残量の監視が始まります。
3. 設定した残量に達すると、音声でお知らせします。

### ダウンロード

最新の APK は [Releases](../../releases) からダウンロードできます。

### 技術情報

- Kotlin + Jetpack Compose
- フォアグラウンドサービスでバッテリー残量を監視
- TextToSpeech（TTS）による音声通知

### ライセンス

MIT License。詳細は [LICENSE](LICENSE) を参照してください。
