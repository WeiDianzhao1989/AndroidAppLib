
package com.koudai.net.kernal.internal.http;

import java.io.IOException;

/**
 * Indicates a problem with interpreting a request. It may indicate there was a problem with the
 * request itself, or the environment being used to interpret the request (network failure, etc.).
 */
public final class RequestException extends Exception {

  public RequestException(IOException cause) {
    super(cause);
  }

  @Override
  public IOException getCause() {
    return (IOException) super.getCause();
  }
}
