
package com.koudai.net.kernal.internal.http;

import com.koudai.net.io.BufferedSource;
import com.koudai.net.kernal.Headers;
import com.koudai.net.kernal.MediaType;
import com.koudai.net.kernal.ResponseBody;

public final class RealResponseBody extends ResponseBody {
  private final Headers headers;
  private final BufferedSource source;

  public RealResponseBody(Headers headers, BufferedSource source) {
    this.headers = headers;
    this.source = source;
  }

  @Override public MediaType contentType() {
    String contentType = headers.get("Content-Type");
    return contentType != null ? MediaType.parse(contentType) : null;
  }

  @Override public long contentLength() {
    return OkHeaders.contentLength(headers);
  }

  @Override public BufferedSource source() {
    return source;
  }
}
