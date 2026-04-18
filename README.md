# NekoFlight4j

## ビルドと起動

### 1) Maven でそのまま起動

```bash
mvn compile exec:java
```

### 2) 実行可能 JAR を作成して起動

```bash
mvn package
java -jar target/jflight-1.0-SNAPSHOT.jar
```

## ソース構成

- `src/main/java/jflight` : アプリ本体
- `pom.xml` : Maven 設定

## 操作

- `W` / `S` または `↑` / `↓` : ピッチ
- `A` / `D` または `←` / `→` : ロール
- `Q` / `E` : ラダー
- `Space` : 射撃 / ミサイル
- `Shift` : ブースト
- `Enter` : AUTO / MANUAL 切替（設定メニューでは項目切替）
- `Tab` : 設定メニュー表示
- `H` : HUD クローム表示切替
- `R` : ステージリセット

## メモ

- 元のブラウザアプレット構成は廃止し、Swing ベースのデスクトップアプリとして起動します。
- メインクラスは `jflight.mains.Jflight` です。
