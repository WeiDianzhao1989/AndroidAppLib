package com.koudai.net.kernal.internal.http;

import com.koudai.net.io.Buffer;
import com.koudai.net.io.Sink;
import com.koudai.net.io.Timeout;

import java.io.IOException;
import java.net.ProtocolException;

import static com.koudai.net.kernal.internal.Util.checkOffsetAndCount;

/**
 * An HTTP request body that's completely buffered in memory. This allows
 * the post body to be transparently re-sent if the HTTP request must be
 * sent multiple times.
 */
public final class RetryableSink implements Sink {
  private boolean closed;
  private final int limit;
  private final Buffer content = new Buffer();

  public RetryableSink(int limit) {
    this.limit = limit;
  }

  public RetryableSink() {
    this(-1);
  }

  @Override public void close() throws IOException {
    if (closed) return;
    closed = true;
    if (content.size() < limit) {
      throw new ProtocolException(
          "content-length promised " + limit + " bytes, but received " + content.size());
    }
  }

  @Override public void write(Buffer source, long byteCount) throws IOException {
    if (closed) throw new IllegalStateException("closed");
    checkOffsetAndCount(source.size(), 0, byteCount);
    if (limit != -1 && content.size() > limit - byteCount) {
      throw new ProtocolException("exceeded content-length limit of " + limit + " bytes");
    }
    content.write(source, byteCount);
  }

  @Override public void flush() throws IOException {
  }

  @Override public Timeout timeout() {
    return Timeout.NONE;
  }

  public long contentLength() throws IOException {
    return content.size();
  }

  public void writeToSocket(Sink socketOut) throws IOException {
    // Copy the content; otherwise we won't have data to retry.
    Buffer buffer = new Buffer();
    content.copyTo(buffer, 0, content.size());
    socketOut.write(buffer, buffer.size());
  }
}
