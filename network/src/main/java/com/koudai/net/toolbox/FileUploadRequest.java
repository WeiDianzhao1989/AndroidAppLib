package com.koudai.net.toolbox;

import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.callback.ProgressCallback;
import com.koudai.net.callback.RetryCallback;
import com.koudai.net.kernal.Callback;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.MediaType;
import com.koudai.net.kernal.MultipartBody;
import com.koudai.net.kernal.RequestBody;
import com.koudai.net.kernal.internal.Util;
import com.koudai.net.netutils.CollectionUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoyu on 15/11/26.
 * post的文件上传请求
 */
public final class FileUploadRequest<T> extends HttpRequest<T> {

    private static final String TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary";

    private static final String TRANSFER_ENCODING_8BIT = "Content-Transfer-Encoding: 8bit";

    private final List<UploadPart> uploadParts;
    private final IResponseProcessor responseProcessor;
    private final Parser<T> parser;


    public FileUploadRequest(Builder<T> builder) {

        this.url = builder.url;
        this.headers = builder.headers != null ? builder.headers : new RequestHeaders();
        this.params = builder.params != null ? builder.params : new RequestParams();
        this.uploadParts = builder.uploadParts;
        this.encodeCharset = Util.UTF_8;
        this.requestLevel = builder.requestLevel;
        this.callback = builder.callback;
        this.retryCallback = builder.retryCallback;
        this.parser = builder.parser;
        this.priority = builder.priority;

        this.requestHeaderInterceptor = builder.requestHeaderInterceptor;
        this.requestParamsInterceptor = builder.requestParamsInterceptor;
        this.responseProcessor = builder.responseProcessor;

        if (builder.retryTimesAfterFailed >= 0) {
            this.maxRetryTimesAfterFailed =
                    builder.retryTimesAfterFailed;
        }

        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 由于图片上传不走proxy，这块需要特殊处理，后去推动文件上传服务走proxy
     *
     * @return
     */
    @Override
    protected final Map<String, String> assembleParams() {
        if (params != null) {
            return params.toMap();
        } else {
            return CollectionUtils.EMPTY_STRING_MAP;
        }
    }

    @Override
    public String tag() {

        if (TextUtils.isEmpty(tag)) {
            int result = 0;

            if (!TextUtils.isEmpty(url)) {
                result += this.url.hashCode();
            }

            if (this.params != null) {
                result += this.params.hashCode();
            }

            if (this.headers != null) {
                result += this.headers.hashCode();
            }


            if (!CollectionUtils.isListEmpty(uploadParts)) {
                for (UploadPart part : uploadParts) {
                    File file = part.getFile();
                    if (file != null) {
                        result += (file.getName().hashCode());
                    }
                }
            }


            result += requestLevel;
            tag = String.valueOf(result);
        }

        return tag;
    }

    @Override
    protected String httpMethod() {
        return NetworkLibraryConstants.POST;
    }

    @Override
    protected HttpUrl buildUrl(Map<String, String> params) {
        return HttpUrl.parse(url);
    }

    @Override
    protected RequestBody buildBody(Map<String, String> params) {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();

        try {
            multipartBuilder.setType(MultipartBody.FORM);
            if (!CollectionUtils.isMapEmpty(params)) { //目前请求体不进行gzip压缩
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue(),
                                MediaType.parse("text/plain"), TRANSFER_ENCODING_8BIT);
                    }
                }
            }

            if (!CollectionUtils.isListEmpty(uploadParts)) {
                for (UploadPart part : uploadParts) {
                    multipartBuilder.addFormDataPart(part.getKey(), part.getFile().getName()
                            , TRANSFER_ENCODING_BINARY,
                            RequestBody.create(MediaType.parse(part.getMediaType()), part.getFile()));
                }
            }

        } catch (Exception e) {

        }

        return new ProgressRequestBody(multipartBuilder.build(),
                (ProgressCallback) callback);
    }

    @Override
    protected Callback buildResponseCallback() {
        return new DefaultResponseCallback<T>(this, responseProcessor, parser);
    }


    public Builder<T> newBuilder() {

        Builder<T> builder = new Builder<T>();
        builder.url(this.url)
                .headers(this.headers)
                .params(this.params)
                .callback(this.callback)
                .parser(this.parser)
                .uploadParts(this.uploadParts)
                .requestLevel(this.requestLevel)
                .retryTimesAfterFailed(this.maxRetryTimesAfterFailed)
                .priority(this.priority)
                .requestHeaderInterceptor(this.requestHeaderInterceptor)
                .requestParamsInterceptor(this.requestParamsInterceptor)
                .responseProcessor(this.responseProcessor)
                .retryCallback(this.retryCallback)
                .build();

        return builder;
    }

    public static final class Builder<T> {
        private String url;
        private RequestHeaders headers;
        private RequestParams params;

        private int requestLevel;
        private com.koudai.net.callback.Callback<T> callback;
        private RetryCallback<HttpPostRequest<T>> retryCallback;

        private Parser<T> parser;
        private List<UploadPart> uploadParts;
        private int retryTimesAfterFailed = -1;
        private int priority = 0;

        private IResponseProcessor responseProcessor;
        private IRequestHeaderInterceptor requestHeaderInterceptor;
        private IRequestParamsInterceptor requestParamsInterceptor;


        public Builder<T> url(String url) {
            this.url = url;
            return this;
        }

        public Builder<T> headers(RequestHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder<T> params(RequestParams parmas) {
            this.params = parmas;
            return this;
        }

        public Builder<T> uploadParts(List<UploadPart> uploadParts) {
            this.uploadParts = uploadParts;
            return this;
        }

        public Builder<T> callback(com.koudai.net.callback.Callback<T> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<T> parser(Parser<T> parser) {
            this.parser = parser;
            return this;
        }

        public Builder<T> requestLevel(int requestLevel) {
            if (requestLevel < 0 || requestLevel > Level.MAIN_TASK)
                throw new IllegalArgumentException("level is a invalid value");
            this.requestLevel = requestLevel;
            return this;
        }

        public Builder<T> retryTimesAfterFailed(int retryTimesAfterFailed) {
            this.retryTimesAfterFailed = retryTimesAfterFailed;
            return this;
        }

        public Builder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder<T> requestHeaderInterceptor(IRequestHeaderInterceptor requestHeaderInterceptor) {
            this.requestHeaderInterceptor = requestHeaderInterceptor;
            return this;
        }

        public Builder<T> requestParamsInterceptor(IRequestParamsInterceptor requestParamsInterceptor) {
            this.requestParamsInterceptor = requestParamsInterceptor;
            return this;
        }

        public Builder<T> responseProcessor(IResponseProcessor responseProcessor) {
            this.responseProcessor = responseProcessor;
            return this;
        }

        public Builder<T> retryCallback(RetryCallback<HttpPostRequest<T>> retryCallback) {
            this.retryCallback = retryCallback;
            return this;
        }

        public FileUploadRequest<T> build() {
            return new FileUploadRequest<T>(this);
        }
    }
}
