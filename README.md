# NekoFlight for Java

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

- `Space` : 射撃 / ミサイル
- `矢印キー` : 操縦
- `B` : ブースト

## メモ

- 元のブラウザアプレット構成は廃止し、Swing ベースのデスクトップアプリとして起動します。
- メインクラスは `jflight.mains.Jflight` です。
