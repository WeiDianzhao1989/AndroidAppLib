package com.koudai.net.kernal.internal.framed;

public enum HeadersMode {
  SPDY_SYN_STREAM,
  SPDY_REPLY,
  SPDY_HEADERS,
  HTTP_20_HEADERS;

  /** Returns true if it is an error these headers to create a new stream. */
  public boolean failIfStreamAbsent() {
    return this == SPDY_REPLY || this == SPDY_HEADERS;
  }

  /** Returns true if it is an error these headers to update an existing stream. */
  public boolean failIfStreamPresent() {
    return this == SPDY_SYN_STREAM;
  }

  /**
   * Returns true if it is an error these headers to be the initial headers of a
   * response.
   */
  public boolean failIfHeadersAbsent() {
    return this == SPDY_HEADERS;
  }

  /**
   * Returns true if it is an error these headers to be update existing headers
   * of a response.
   */
  public boolean failIfHeadersPresent() {
    return this == SPDY_REPLY;
  }
}
