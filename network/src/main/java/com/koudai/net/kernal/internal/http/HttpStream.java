package com.koudai.net.kernal.internal.http;

import com.koudai.net.io.Sink;
import com.koudai.net.kernal.Request;
import com.koudai.net.kernal.Response;
import com.koudai.net.kernal.ResponseBody;

import java.io.IOException;

public interface HttpStream {
    /**
     * The timeout to use while discarding a stream of input data. Since this is used for connection
     * reuse, this timeout should be significantly less than the time it takes to establish a new
     * connection.
     */
    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    /** Returns an output stream where the request body can be streamed. */
    Sink createRequestBody(Request request, long contentLength) throws IOException;

    /** This should update the HTTP engine's sentRequestMillis field. */
    void writeRequestHeaders(Request request) throws IOException;

    /**
     * Sends the request body returned by {@link #createRequestBody} to the remote peer.
     */
    void writeRequestBody(RetryableSink requestBody) throws IOException;

    /** Flush the request to the underlying socket. */
    void finishRequest() throws IOException;

    /** Read and return response headers. */
    Response.Builder readResponseHeaders() throws IOException;

    /** Returns a stream that reads the response body. */
    ResponseBody openResponseBody(Response response) throws IOException;

    void setHttpEngine(HttpEngine httpEngine);

    /**
     * Cancel this stream. Resources held by this stream will be cleaned up, though not synchronously.
     * That may happen later by the connection pool thread.
     */
    void cancel();
}
