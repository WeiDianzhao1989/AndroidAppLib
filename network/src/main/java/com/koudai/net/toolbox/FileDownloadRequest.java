package com.koudai.net.toolbox;

import android.net.Uri;
import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.callback.FileDownloadCallback;
import com.koudai.net.kernal.Callback;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.RequestBody;
import com.koudai.net.kernal.internal.Util;
import com.koudai.net.netutils.CollectionUtils;
import com.koudai.net.toolbox.processor.FileDownloadProcessor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


/**
 * Created by zhaoyu on 15/11/16.
 * 通过Get方式下载指定文件url到本地(filename)
 */
public final class FileDownloadRequest extends HttpRequest<File> {

    private final String saveFileAbsolutelyPath;
    private final FileDownloadProcessor processor;
    //设置是否支持断点下载
    private boolean isAutoResume;

    public FileDownloadRequest(Builder builder) {
        this.url = builder.url;
        this.headers = builder.headers != null ? builder.headers : new RequestHeaders();
        this.params = builder.params != null ? builder.params : new RequestParams();
        this.encodeCharset = builder.encodeCharset;
        this.requestLevel = builder.requestLevel;
        this.callback = builder.callback;
        this.saveFileAbsolutelyPath = builder.saveFileAbsolutelyPath;
        this.isAutoResume = builder.isAutoResume;
        this.processor = builder.processor;
        this.priority = builder.priority;
        this.requestHeaderInterceptor = builder.requestHeaderInterceptor;
        this.requestParamsInterceptor = builder.requestParamsInterceptor;

        if (builder.retryTimesAfterFailed >= 0) {
            this.maxRetryTimesAfterFailed =
                    builder.retryTimesAfterFailed;
        }

        this.timestamp = System.currentTimeMillis();
    }

    @Override
    protected String httpMethod() {
        return NetworkLibraryConstants.GET;
    }

    @Override
    protected HttpUrl buildUrl(Map<String, String> params) {
        try {
            Uri uri = Uri.parse(url);

            HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                    .scheme(uri.getScheme())
                    .host(uri.getHost());

            List<String> paths = uri.getPathSegments();

            if (paths != null) {
                for (String pathSegment : paths) {
                    httpUrlBuilder.addPathSegment(pathSegment);
                }
            }

            if (!CollectionUtils.isMapEmpty(params)) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    httpUrlBuilder.addQueryParameter(
                            URLEncoder.encode(entry.getKey(), encodeCharset.name()),
                            URLEncoder.encode(entry.getValue(), encodeCharset.name())
                    );
                }
            }

            return httpUrlBuilder.build();
        } catch (UnsupportedEncodingException e) {
            NetworkLog.getInstance().e(e.getMessage());
        }


        return null;
    }

    @Override
    protected RequestBody buildBody(Map<String, String> params) {
        return null;
    }

    @Override
    protected Map<String, String> assembleHeaders() {
        Map<String, String> headers = super.assembleHeaders();
        //断点下载需要Range头告诉服务器从文件哪儿部分开始下载
        httpRangeHeader(headers);
        return headers;
    }

    @Override
    protected Callback buildResponseCallback() {
        return new InternalFileDownloadCallback(this, processor);
    }

    /**
     * 断点下载，range头添加
     *
     * @param headers
     */
    private void httpRangeHeader(Map<String, String> headers) {
        if (isAutoResume) {
            File saveFile = new File(saveFileAbsolutelyPath + NetworkLibraryConstants.TMP_FILE_SUFFIX);
            if (saveFile.exists()) {
                long fileLen = saveFile.length();
                long range = 0;

                if (fileLen > NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE) {
                    range = fileLen - NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE;
                }
                headers.put("RANGE", "bytes=" + range + "-");
            }
        } else {
            if (headers.containsKey("RANGE")) {
                headers.remove("RANGE");
            }
        }
    }

    String saveFileAbsolutelyPath() {
        return saveFileAbsolutelyPath;
    }

    boolean isAutoResume() {
        return isAutoResume;
    }

    public void setIsAutoResume(boolean isAutoResume) {
        this.isAutoResume = isAutoResume;
    }

    @Override
    public String tag() {
        if (TextUtils.isEmpty(tag)) {
            int result = 0;

            if (!TextUtils.isEmpty(this.url)) {
                result += this.url.hashCode();
            }

            if (this.params != null) {
                result += this.params.hashCode();
            }

            if (this.headers != null) {
                result += this.headers.hashCode();
            }

            if (!TextUtils.isEmpty(this.saveFileAbsolutelyPath)) {
                result += this.saveFileAbsolutelyPath.hashCode();
            }

            tag = String.valueOf(result);
        }

        return tag;
    }


    public static final class Builder {
        private String url;
        private RequestHeaders headers;
        private RequestParams params;

        private Charset encodeCharset = Util.UTF_8;
        private int requestLevel;
        private FileDownloadCallback callback;
        private FileDownloadProcessor processor;
        private String saveFileAbsolutelyPath;
        private int retryTimesAfterFailed = -1;
        private int priority = 0;
        private boolean isAutoResume;

        private IRequestHeaderInterceptor requestHeaderInterceptor;
        private IRequestParamsInterceptor requestParamsInterceptor;


        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder headers(RequestHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder params(RequestParams parmas) {
            this.params = parmas;
            return this;
        }

        public Builder charset(Charset encodeCharset) {
            this.encodeCharset = encodeCharset;
            return this;
        }

        public Builder saveFileAbsolutelyPath(String fileName) {
            this.saveFileAbsolutelyPath = fileName;
            return this;
        }

        public Builder isAutoResume(boolean isAutoResume) {
            this.isAutoResume = isAutoResume;
            return this;
        }

        public Builder callback(FileDownloadCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder processor(FileDownloadProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder requestLevel(int requestLevel) {
            if (requestLevel < 0 || requestLevel > Level.MAIN_TASK)
                throw new IllegalArgumentException("level is a invalid value");
            this.requestLevel = requestLevel;
            return this;
        }

        public Builder retryTimesAfterFailed(int retryTimesAfterFailed) {
            this.retryTimesAfterFailed = retryTimesAfterFailed;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder requestHeaderInterceptor(IRequestHeaderInterceptor requestHeaderInterceptor) {
            this.requestHeaderInterceptor = requestHeaderInterceptor;
            return this;
        }

        public Builder requestParamsInterceptor(IRequestParamsInterceptor requestParamsInterceptor) {
            this.requestParamsInterceptor = requestParamsInterceptor;
            return this;
        }

        public FileDownloadRequest build() {
            return new FileDownloadRequest(this);
        }
    }

}
