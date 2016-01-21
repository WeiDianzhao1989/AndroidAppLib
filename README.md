# godhelper
a library contains all tools necessary for establishing a app

##network
以okhttp为基础构建的网络库，支持 post/get 请求,断点下载,文件上传，批处理执行网络请求.


init network
 
 初始化网络库
 
     NetworkFetcherConfiguration configuration =
                    new NetworkFetcherConfiguration.Builder()
                            .context(this)
                            .readTimeOut(30 * 1000)
                            .connectTimeOut(30 * 1000)
                            .requestParamsInterceptor(new xxRequestParamsInterceptor(false,
                                    true,Constants.ENCRYPT_FLAG_PRIVATE))
                            .requestHeaderInterceptor(new xxRequestHeadersInterceptor(false,true))
                            .responseProcessor(new xxResponseProcessor(Constants.ENCRYPT_FLAG_PRIVATE))
                            .build();
    NetworkFetcher.getInstance().initNetwork(configuration);
    
  发个post请求
  
           RequestParams params = new RequestParams();

        params.putParam("product_id", productId);
        params.putParam("page", String.valueOf(page));
        params.putParam("limit", String.valueOf(PAGE_SIZE));


        HttpPostRequest<CommodityCommentResponse>
                request = new HttpPostRequest.Builder<CommodityCommentResponse>()
                .url(Protocol.HOST + "xxxxxxx.do")
                .params(params)
                .parser(new CommodityCommentResponseParser())
                .callback(new Callback<CommodityCommentResponse>() {
                    @Override
                    public void onResponse(CommodityCommentResponse result) {
                      
                    }

                    @Override
                    public void onError(NetworkError error) {
                
                    }

                    @Override
                    public void onCancel() {
                  
                    }

                }).build();

        NetworkFetcher.getInstance().execute(request);
        
        下载文件
        
        FileDownloadRequest downloadrequest = new FileDownloadRequest.Builder()
                .url("https://xxxxxx/m/appupdate/iOS.zip")
                .saveFileAbsolutelyPath(filename)
                .isAutoResume(true)
                .callback(new FileDownloadCallback() {
                    @Override
                    public void onProgress(long total, long current) {
                    }

                    @Override
                    public void onResponse(File result) {
                    }

                    @Override
                    public void onError(NetworkError error) {
                    }

                    @Override
                    public void onCancel() {
                    }


                }).build();

        NetworkFetcher.getInstance().execute(downloadrequest);
        
        
        ## Amap Android客户端插件化-HelloWorld

###1. 创建宿主工程

1. 创建Android应用工程.
2. 添加com.weidian.plugin.jar到依赖.
3. 添加继承自HostApplication的自定义Application.
4. 创建packagename.Controller类.

###2. 创建模块工程

1. 创建Android应用工程.
2. 添加com.weidian.plugin.jar到依赖, 但是编译时不导出.
3. 创建packagename.Controller类.
4. 编译模块工程为apk文件, 放到宿主工程的assets/module/文件夹, 修改扩展名为png(部分android2.2系统assets中的普通文件大小限制在1M以内, 但png不受限制).
5. 在宿主工程的AndroidManifest.xml中application节点内加入:
    `<meta-data android:name="dependence" android:value="module_pkg_name" />`,
    不加这个信息不会自动加载对应模块, 但会在真正需要调用改模块时才加载.
6. 到此插件工程基本配置完成, 接下来开始插件间的交互(:

###3. 打开插件中的 Fragment 或 Activity

1. 在模块工程中添加自定义的Fragment 或 Activity.(plugin项目已经集成support_v4并重写了Fragment获取Context和资源的代码, Activity需继承自PluginActivity或PluginFragmentActivity)
2. 在模块工程中的AndroidManifest.xml中注册Fragment:
```xml
约定:action的value值必须以plugin开头, 定义为plugin.Main的 Fragment 或 Activity 将成为主入口.
<activity android:name=".TestFragment">
    <meta-data android:name="action" android:value="plugin.test.TestFragment"/>
</activity>
<activity android:theme="@style/real_style" android:name=".TestActivity">
    <meta-data android:name="action" android:value="plugin.Main"/>
</activity>
```
3. 打开这个Fragment:
```java

```


####4. 打开插件中的Activity

####5. 插件中使用Dialog

####6. 插件中使用AlertDialog

####7. 插件间使用PluginMsg交互

