package com.koudai.net.toolbox;

import android.text.TextUtils;

import com.koudai.net.io.BufferedSink;
import com.koudai.net.io.BufferedSource;
import com.koudai.net.io.Okio;
import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.error.DownloadException;
import com.koudai.net.error.NetworkError;
import com.koudai.net.error.RetryException;
import com.koudai.net.kernal.Call;
import com.koudai.net.kernal.Callback;
import com.koudai.net.kernal.Response;
import com.koudai.net.kernal.ResponseBody;
import com.koudai.net.toolbox.processor.FileDownloadProcessor;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Created by zhaoyu on 16/1/14.
 * 文件下载逻辑
 */
final class InternalFileDownloadCallback implements Callback {

    private final FileDownloadRequest fileDownloadRequest;
    private final FileDownloadProcessor processor;
    private StringBuilder log = new StringBuilder(1024);

    public InternalFileDownloadCallback(FileDownloadRequest fileDownloadRequest, FileDownloadProcessor processor) {
        this.fileDownloadRequest = fileDownloadRequest;
        this.processor = processor;
    }

    @Override
    public void onFailure(Call call, IOException e) throws RetryException {
        if (fileDownloadRequest.retryTimes()
                < fileDownloadRequest.maxRetryTimesAfterFailed()) {
            if (e instanceof SocketTimeoutException) {//socket 连接，读写超时重试
                throw new RetryException(e.getMessage());
            }
        }

        NetworkError error = new NetworkError(NetworkLibraryConstants.INTERNAL_ERROR,
                NetworkLibraryConstants.INTERNAL_ERROR, e.getMessage());
        DefaultResponseDelivery.getInstance().postError(fileDownloadRequest, error);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException, RetryException {
        boolean isDownloadSuccess = false;
        try {
            if (!TextUtils.isEmpty(fileDownloadRequest.saveFileAbsolutelyPath())) {
                int responseCode = response.code();
                if (responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_PARTIAL) {

                    try {

                        if (checkDownloadFileExists()) {
                            deleteDownloadFile();
                        }

                        saveFile(response);
                        if (!fileDownloadRequest.isCanceled()) {
                            downloadSuccess();
                            isDownloadSuccess = true;
                        } else {
                            DefaultResponseDelivery.getInstance().postCancel(fileDownloadRequest);
                        }
                        return;
                    } catch (OutOfMemoryError e) {
                        NetworkLog.getInstance().e(
                                "out of memory", e.getCause()
                        );
                        NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                NetworkLibraryConstants.INTERNAL_ERROR, e.getMessage());
                        DefaultResponseDelivery.getInstance().postError(fileDownloadRequest, error);
                        return;
                    } catch (DownloadException e) {
                        NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                NetworkLibraryConstants.INTERNAL_ERROR, e.getMessage());
                        DefaultResponseDelivery.getInstance().postError(fileDownloadRequest, error);
                        return;
                    } finally {
                        if (!isDownloadSuccess) {
                            if (!fileDownloadRequest.isCanceled()) {
                                deleteDownloadFile();
                            }
                        }
                    }
                } else if (responseCode == 416) {
                    fileDownloadRequest.setIsAutoResume(false);
                    throw new RetryException("server doesn't support to download break point");
                } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE
                        || responseCode == HttpURLConnection.HTTP_BAD_GATEWAY
                        || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED) { //服务器内部错误

                    log.setLength(0);
                    log = new StringBuilder("business server error")
                            .append("response_code=").append(responseCode)
                            .append("response_message [").append(response.message())
                            .append("]");
                    NetworkLog.getInstance().v(log.toString());

                    //重新连接
                    if (fileDownloadRequest.retryTimes() < fileDownloadRequest.maxRetryTimesAfterFailed()) {
                        StringBuilder log = new StringBuilder("due to server,request failed,response_code=")
                                .append(responseCode).append("url:[").append(response.request().url().toString()).append("]")
                                .append("retryTimes=").append(fileDownloadRequest.retryTimes());
                        NetworkLog.getInstance().e(log.toString());

                        throw new RetryException(responseCode, response.message());
                    } else {
                        NetworkError error = new NetworkError(responseCode, responseCode
                                , response.message());
                        DefaultResponseDelivery.getInstance().postError(fileDownloadRequest, error);
                        return;
                    }
                }
            }

            NetworkError error = new NetworkError(NetworkLibraryConstants.DOWNLOAD_FILE_ERROR_CODE,
                    NetworkLibraryConstants.DOWNLOAD_FILE_ERROR_CODE, "you don't supply download load file save path");
            DefaultResponseDelivery.getInstance().postError(fileDownloadRequest, error);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }

    @Override
    public void onCancel(Call call) {
        log.setLength(0);
        log.append("file download ").append(fileDownloadRequest.url()).append(" has been canceled");
        DefaultResponseDelivery.getInstance().postCancel(fileDownloadRequest);
    }


    /**
     * 保存文件，文件先保存成.tmp结尾的临时文件，当下载成功后，会重名为
     * 正式名称
     *
     * @param response
     * @return
     */
    private void saveFile(final Response response) throws IOException,
            DownloadException, RetryException {
        BufferedSink sink = null;
        BufferedSource responseSource = null;
        File destTmpFile = null;
        File destFile = null;

        try {
            destFile = new File(fileDownloadRequest.saveFileAbsolutelyPath());
            destTmpFile = new File(fileDownloadRequest.saveFileAbsolutelyPath()
                    + NetworkLibraryConstants.TMP_FILE_SUFFIX);
            File parent = destTmpFile.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    NetworkLog.getInstance().e("create file parent dir failed");
                    throw new IOException("create file failed");
                }
            }

            if (!destTmpFile.exists()) {
                if (!destTmpFile.createNewFile()) {
                    NetworkLog.getInstance().e("create file failed");
                    throw new IOException("create file failed");
                }
            } else {
                if (!fileDownloadRequest.isAutoResume()) {
                    if (destTmpFile.delete()) {
                        if (!destTmpFile.createNewFile()) {
                            NetworkLog.getInstance().e("create file failed");
                            throw new IOException("create file failed");
                        } else {
                            throw new IOException("create file failed");
                        }
                    } else {
                        throw new IOException("delete old file failed");
                    }
                } else {
                    if (!destTmpFile.exists()) {
                        throw new RetryException("cached file has been deleted, so can't finish download break point,retry from the beginning");
                    }
                }
            }

            //判断服务端是否支持断点下载
            if (fileDownloadRequest.isAutoResume()) {
                fileDownloadRequest.setIsAutoResume(isSupportRange(response));
            }

            if (fileDownloadRequest.isAutoResume()) {
                //支持断点下载的话，接着后头写文件
                sink = Okio.buffer(Okio.appendingSink(destTmpFile));
            } else {
                sink = Okio.buffer(Okio.sink(destTmpFile));
            }

            ResponseBody responseBody = response.body();

            if (responseBody != null && responseBody.source() != null) {
                responseSource = response.body().source();//留到外边同一关body

                if (fileDownloadRequest.isAutoResume()) {
                    /*
                     * 判断本地已有文件的默认最末端字节(500或已有字节)
                     * 是否和服务端获取的流对应的字节一样，一样的话证明服务端文件
                     * 和本地比起来没改动，可以继续断点下载
                     */
                    if (!checkFileValid(destTmpFile, responseSource)) {
                        fileDownloadRequest.setIsAutoResume(false);
                        throw new RetryException("the server file has been modified and different from the local cached file");
                    }
                }

                responseSource.readAll(sink);

                sink.flush();

                // 将文件名称重命名到正式名称
                if (destTmpFile.renameTo(destFile)) {
                    fileDownloadRequest.markSuccess();
                } else {
                    throw new DownloadException("save download file failed");
                }
            }

        } finally {
            try {
                if (sink != null) {
                    sink.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查断点文件有效性
     *
     * @param destTmpFile
     * @param responseSource
     * @return
     * @throws IOException
     */
    private boolean checkFileValid(File destTmpFile, BufferedSource responseSource) throws IOException {
        final long fileLength = destTmpFile.length();//文件大小
        long skip = 0; //跳过多少字节
        int checkSize = NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE; //检查字节大小

        BufferedSource existFileSource = Okio.buffer(Okio.source(destTmpFile));

        if (fileLength > NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE) {
            skip = fileLength - NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE;
        }

        if (fileLength - skip < NetworkLibraryConstants.DOWNLOAD_BREAK_POINT_CHECK_SIZE) {
            checkSize = (int) fileLength;
        }

        if (skip > 0) {
            existFileSource.skip(skip);
        }

        byte[] fileCheckBuffer = NetworkFetcherGlobalParams.getInstance()
                .getByteArrayPool().getBuf(checkSize);
        byte[] responseCheckBuffer = NetworkFetcherGlobalParams.getInstance()
                .getByteArrayPool().getBuf(checkSize);

        existFileSource.read(fileCheckBuffer, 0, checkSize);
        responseSource.readFully(responseCheckBuffer);

        return Arrays.equals(fileCheckBuffer, responseCheckBuffer);
    }

    private void downloadSuccess() {
        File downloadFile = new File(fileDownloadRequest.saveFileAbsolutelyPath());
        //后处理处理file
        File postProcessorFile = downloadFile;
        if (processor != null) {
            postProcessorFile = processor.postProcessor(downloadFile);
        }
        fileDownloadRequest.markSuccess();
        DefaultResponseDelivery.getInstance().postResponse(fileDownloadRequest, postProcessorFile);
    }

    private void deleteDownloadFile() {
        File downloadFile = new File(fileDownloadRequest.saveFileAbsolutelyPath());
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
    }

    private boolean checkDownloadFileExists() {
        File downloadFile = new File(fileDownloadRequest.saveFileAbsolutelyPath());
        return downloadFile.exists();
    }

    private boolean isSupportRange(Response response) {
        if (response == null) return false;

        String ranges = response.header("Accept-Ranges");
        if (ranges != null) {
            return ranges.contains("bytes");
        }
        ranges = response.header("Content-Range");
        return ranges != null && ranges.contains("bytes");
    }
}
