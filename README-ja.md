# Meganekko

Oculus Mobile SDKの上に構築された、Gear VR用の3Dレンダリングフレームワークです。

## 使い方

Android Studioで新規プロジェクトを作成します。

### ライブラリの追加

プロジェクトのルートの**build.gradle**にリポジトリURLの追記をします。

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url = 'http://ejeinc.github.io/Meganekko/repository' } // この行を追加
    }
}
```

そして、モジュールの中の**build.gradle**に依存ライブラリとして追加します。

```gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.eje_c:meganekko:2.3.4'
}
```

`allprojects`ブロックをいじりたくない場合は、`repositories`ブロックをモジュールの中の**build.gradle**に記述することもできます。

画面右上に表示されるSync Nowをクリックします。正しく設定できていればプロジェクトにMeganekkoライブラリーが追加されます。

注意: バージョン2.3.0からMeganekkoはマルチビューレンダリングを使用していますが、この機能はAndroid M以前の端末では動作しません。詳しくはこちら https://developer3.oculus.com/documentation/mobilesdk/latest/concepts/release/

### Hello World

Meganekkoのアプリケーションは`MeganekkoApp`を継承したクラスがメインになります。

```java
import com.eje_c.meganekko.MeganekkoApp;

public class MyApp extends MeganekkoApp {

    @Override
    public void init() {
        super.init();
        // Init application here
    }
}
```

次に、VRのシーンを作成します。シーンはAndroidのViewを記述するのと似た方法で、XMLを使って記述します。XMLファイルはres/xml/scene.xmlに作成します。
ファイル名は任意です。複数用意することもできます。

```xml:res/xml/scene.xml
<scene>
    <!-- 前方 5.0 の距離に、@layout/hello_worldで指定するViewを表示する。 -->
    <object
        layout="@layout/hello_world"
        z="-5.0" />
</scene>
```

`@layout/hello_world`の中身は通常のAndroidアプリで使用するレイアウト指定方法と同じです。

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Hello World!と白色で表示する -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        android:textColor="#fff" />
</FrameLayout>
```

作成したXMLシーンを利用するために`MyApp`の中で`setSceneFromXML`を呼び出します。

```java
import com.eje_c.meganekko.MeganekkoApp;

public class MyApp extends MeganekkoApp {

    @Override
    public void init() {
        super.init();
        setSceneFromXML(R.xml.scene);
    }
}
```

Meganekkoを使うためにはAndroidManifestにも手を加える必要があります。

[Oculus developer document](https://developer.oculus.com/documentation/mobilesdk/latest/concepts/mobile-new-apps-intro/#mobile-native-manifest)で推奨されている属性値があるので、追記します。

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name">

    <meta-data
        android:name="com.eje_c.meganekko.App"
        android:value="com.eje_c.meganekko.sample.MyApp"/> <!-- Appクラスのフルネームを指定する -->

    <activity
        android:name="com.eje_c.meganekko.gearvr.MeganekkoActivity"
        android:configChanges="orientation|screenSize|keyboard|keyboardHidden"
        android:excludeFromRecents="true"
        android:label="@string/app_name"
        android:launchMode="singleTask"
        android:screenOrientation="landscape">

        <!-- デバッグ時にホームアプリから起動できるようにするために付ける。Oculusストアへリリースする場合は削除する。 -->
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>

    </activity>
</application>
```

```xml
<meta-data
    android:name="com.samsung.android.vr.application.mode"
    android:value="vr_only" />
```

の記述はライブラリからインジェクトされるので、定義しなくても良いです。

Gear VRのアプリケーションを動作させるにはosigファイルが必要です。osigファイルについては[Oculusデベロッパードキュメント](https://developer.oculus.com/osig/)を読んでください。

osigファイルが用意できたら、`app/src/main/assets`ディレクトリを作成して、その中にosigファイルをコピーします。

ここまでできたらUSBでGalaxy端末を接続してアプリをインストール、実行してください。ここまでの手順を正しくこなしていれば、Hello Worldという白い文字が表示されるはずです。

## Meganekkoをビルドする

Meganekko自体をカスタマイズして利用したい場合は、以下の手順で開発環境を整えてください。

1. Android Studioを起動する。
2. git clone したMeganekkoリポジトリのディレクトリを開く。

**sample/src/main/assets**の中にosigファイルをコピーしてからsampleモジュールをビルド、実行してください。正しく設定できていればサンプルアプリケーションが起動します。

Meganekkoの機能を改善した場合は、Pull Requestしてくれると嬉しいです。
