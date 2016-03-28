package com.koudai.net.toolbox;

import com.koudai.net.io.*;
import com.koudai.net.callback.ProgressCallback;
import com.koudai.net.kernal.MediaType;
import com.koudai.net.kernal.ResponseBody;

import java.io.IOException;

/**
 * Created by zhaoyu on 15/12/2.
 */
public final class ProgressResponseBody extends ResponseBody {
    //实际的待包装响应体
    private final ResponseBody responseBody;
    //进度回调接口
    private final ProgressCallback progressCallback;
    //包装完成的BufferedSource
    private BufferedSource bufferedSource;


    /**
     * 构造函数，赋值
     *
     * @param responseBody     待包装的响应体
     * @param progressCallback 回调接口
     */
    public ProgressResponseBody(ResponseBody responseBody,
                                ProgressCallback progressCallback) {
        this.responseBody = responseBody;
        this.progressCallback = progressCallback;
    }


    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    /**
     * 重写进行包装source
     *
     * @return BufferedSource
     * @throws IOException 异常
     */
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            //包装
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //回调，如果contentLength()不知道长度，会返回-1
                DefaultResponseDelivery.getInstance().postProgress(responseBody.contentLength(), totalBytesRead, progressCallback);
                return bytesRead;
            }
        };
    }
}
