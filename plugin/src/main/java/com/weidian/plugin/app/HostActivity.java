package com.weidian.plugin.app;
//
//##
//##
//##
//注意:
//这是一个不存在的Activity,
//但是必须在AndroidManifest.xml注册它,
//否则插件中的Activity将无法打开.
//
//<activity android:theme="@style/host_activity_style"
//          android:name="com.weidian.plugin.app.HostActivity">
//    <intent-filter>
//        <action android:name="action.plugin.Activity" />
//        <category android:name="android.intent.category.DEFAULT" />
//    </intent-filter>
//</activity>
//
//style:
//<style name="host_activity_style">
//    <!-- Dialog样式透明背景支持 -->
//    <item name="android:windowIsFloating">true</item>
//</style>
//
//####
//在插件中定义 Fragment 或 Activity:
//action的value值必须以plugin开头, 定义为plugin.Main的 Fragment 或 Activity 将成为主入口.
//<activity android:name=".TestFragment">
//    <meta-data android:name="action" android:value="plugin.test.TestFragment"/>
//</activity>
//<activity android:theme="@style/real_style" android:name=".TestActivity">
//    <meta-data android:name="action" android:value="plugin.Main"/>
//</activity>