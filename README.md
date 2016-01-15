# godhelper
a library contains all tools necessary for establishing a app

##network
establish on okhttp

init network
 
 初始化网络库
 
     NetworkFetcherConfiguration configuration =
                    new NetworkFetcherConfiguration.Builder()
                            .context(this)
                            .readTimeOut(30 * 1000)
                            .connectTimeOut(30 * 1000)
                            .requestParamsInterceptor(new WDRequestParamsInterceptor(false,
                                    true,Constants.ENCRYPT_FLAG_PRIVATE))
                            .requestHeaderInterceptor(new WDRequestHeadersInterceptor(false,true))
                            .responseProcessor(new WDResponseProcessor(Constants.ENCRYPT_FLAG_PRIVATE))
                            .build();
    NetworkFetcher.getInstance().initNetwork(configuration);
    
  发个post请求
  
           RequestParams params = new RequestParams();

        params.putParam("product_id", productId);
        params.putParam("page", String.valueOf(page));
        params.putParam("limit", String.valueOf(PAGE_SIZE));


        HttpPostRequest<CommodityCommentResponse>
                request = new HttpPostRequest.Builder<CommodityCommentResponse>()
                .url(Protocol.HOST + "appserver_getItemComments.do")
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
                .url("https://static.koudai.com/m/appupdate/iOS.zip")
                .saveFileAbsolutelyPath(filename)
                .isAutoResume(true)
                .callback(new FileDownloadCallback() {
                    @Override
                    public void onProgress(long total, long current) {
                        Log.e("weidian", "total=" + total + "--current =" + current);
                    }

                    @Override
                    public void onResponse(File result) {
                        Log.e("weidian",result.getName());
                    }

                    @Override
                    public void onError(NetworkError error) {
                        Log.e("weidian",error.getErrorMessage());
                    }

                    @Override
                    public void onCancel() {
                        Log.e("weidian", "onCancel");
                    }


                }).build();

        NetworkFetcher.getInstance().execute(downloadrequest);
