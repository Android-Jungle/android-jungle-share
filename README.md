# android-jungle-share 简介

### 1、简介

`android-jungle-share` 是用于在各个社交软件中进行分享 App 内容的 Android 辅助库。支持以下第三方 App：

- 微信好友
- 微信朋友圈
- QQ 好友
- QQ 空间
- 新浪微博

### 2、依赖引入

```
compile 'com.jungle.share:android-jungle-share:1.0'
```

混淆 Proguard 配置如下：

```
# QQ Share.
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}


# WX Share.
-keep class com.tencent.mm.**{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
```

### 3、使用方法

#### 3.1、分享权限申请

我们自己的 App 在分享内容之前，必须去上述分享目标应用的官方开发者平台上申请分享权限，地址如下：

|第三方应用|获得的 Key|权限申请网址|
|---|---|---|
|微信 / 朋友圈|WXAppId & WXAppSecret|[https://open.weixin.qq.com](https://open.weixin.qq.com)|
|QQ / QQ 空间|TencentAppId|[http://open.qq.com/](http://open.qq.com/)|
|新浪微博|SinaAppKey|[http://open.weibo.com/apps](http://open.weibo.com/apps)|

#### 3.2、声明上述获得的 Key

在我们的 App 的 `AndroidManifest.xml` 中，需要声明上述获得的 Key：

```xml
<uses-permission android:name="android.permission.INTERNET"/>

<application>
    <meta-data
        android:name="TencentAppId"
        android:value="xxx"/>

    <meta-data
        android:name="WXAppId"
        android:value="xxx"/>

    <meta-data
        android:name="SinaAppKey"
        android:value="xxx"/>
</application>
```

#### 3.3、声明分享结果中转 Activity

我们已经预定义好了各个第三方应用的分享结果中转 Activity：**WXShareBaseEntryActivity**、**WBlogBaseShareActivity**，我们需要在自己的 App 中派生它，然后声明：

```xml
<application>
    <activity
        android:name="com.jungle.share.demo.wxapi.WXEntryActivity"
        android:exported="true"
        android:screenOrientation="portrait"
        android:launchMode="singleTop"
        android:theme="@android:style/Theme.Translucent.NoTitleBar">
        <intent-filter>
            <action android:name="android.intent.action.VIEW"/>
            <category android:name="android.intent.category.DEFAULT"/>
        </intent-filter>
    </activity>

    <activity
        android:name="com.tencent.tauth.AuthActivity"
        android:noHistory="true"
        android:launchMode="singleTask"
        android:theme="@android:style/Theme.Translucent.NoTitleBar"
        android:screenOrientation="portrait">
        <intent-filter>
            <action android:name="android.intent.action.VIEW"/>

            <category android:name="android.intent.category.DEFAULT"/>
            <category android:name="android.intent.category.BROWSABLE"/>

            <data android:scheme="tencentxxx"/>
        </intent-filter>
    </activity>

    <activity
        android:name="com.jungle.share.demo.WBShareActivity"
        android:configChanges="keyboardHidden|orientation"
        android:screenOrientation="portrait"
        android:theme="@android:style/Theme.Translucent.NoTitleBar">
        <intent-filter>
            <action android:name="com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY"/>
            <category android:name="android.intent.category.DEFAULT"/>
        </intent-filter>
    </activity>
</application>
```

其中注意：**com.tencent.tauth.AuthActivity** 声明的时候，intent-filter 中带了一个 **data**，其 scheme 必须为 **tencent+你的TencentAppId**。

微信的 Activity 必须为 **`com.your.app.wxapi.WXEntryActivity`**，新浪微博的必须为 **`com.your.app.WBShareActivity`**。

**测试分享的时候，将 App 编译为 Release 包并且用 keystore 签名后再测。** 一般来说，签名信息在你申请分享权限的时候，需要在开放平台上面填入。

#### 3.4、分享

分享就比较简单了，直接调用 `com.jungle.share.ShareHelper` 中的方法即可。例如：

```java
ShareInfo info = new ShareInfo();
shareInfo.mTitle = "Android Jungle Share Library";
shareInfo.mSummary = "Thirdparty share components for Android. Supports WeChat / QZone / Weibo etc.";
shareInfo.mShareUrl = "https://github.com/arnozhang/android-jungle-share";

Bitmap shareBitmap = ...;

ShareHelper.getInstance().shareToWXFriend(context, info, shareBitmap, new ShareHelper.OnShareListener() {

    @Override
    public void onSuccess() {
        showToast("Share successfully!");
    }

    @Override
    public void onCancel() {
        showToast("User canceled share.");
    }

    @Override
    public void onFailed(String message) {
        showToast("Share FAILED: " + message);
    }
});
```

### 4、图片分享

分享图片有两种方法：

- 使用分享接口，直接传入 Bitmap。如 `ShareHelper.getInstance().shareToWXFriend(context, info, bitmap, listener)`；
- 在 ShareInfo 中指定 **mImageUrl**。

采用第二种方法的时候，分享组件会使用 **ShareHelper.ShareImageLoader** 这个帮助图片加载类，来加载图片获得 Bitmap，然后分享出去。所以，你必须先设置这个 ImageLoader：

```java
ShareHelper.getInstance().setShareImageLoader(new ShareHelper.ShareImageLoader() {

    @Override
    public void loadImage(String url, Callback callback) {
        Bitmap bitmap = ...;

        callback.onSuccess(bitmap);
    }
});

ShareInfo info = new ShareInfo();
info.mImageUrl = "https://avatars3.githubusercontent.com/u/2292646?v=3&s=466";

ShareHelper.getInstance().shareToWXFriend(context, info, listener);
```

<br>

## License

```
/**
 * Android Jungle-Share framework project.
 *
 * Copyright 2016 Arno Zhang <zyfgood12@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```
