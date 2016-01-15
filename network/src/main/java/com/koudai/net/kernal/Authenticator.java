
package com.koudai.net.kernal;

import java.io.IOException;

/**
 * Responds to an authentication challenge from either a remote web server or a proxy server.
 * Implementations may either attempt to satisfy the challenge by returning a request that includes
 * an authorization header, or they may refuse the challenge by returning null. In this case the
 * unauthenticated response will be returned to the caller that triggered it.
 *
 * <p>When authentication is requested by an origin server, the response code is 401 and the
 * implementation should respond with a new request that sets the "Authorization" header.
 * <pre>   {@code
 *
 *    String credential = Credentials.basic(...)
 *    return response.request().newBuilder()
 *        .header("Authorization", credential)
 *        .build();
 * }</pre>
 *
 * <p>Whn authentication is requested by a proxy server, the response code is 407 and the
 * implementation should respond with a new request that sets the "Proxy-Authorization" header.
 * <pre>   {@code
 *
 *    String credential = Credentials.basic(...)
 *    return response.request().newBuilder()
 *        .header("Proxy-Authorization", credential)
 *        .build();
 * }</pre>
 *
 * <p>Applications may configure OkHttp with an authenticator for origin servers, or proxy servers,
 * or both.
 */
public interface Authenticator {
  /** An authenticator that knows no credentials and makes no attempt to authenticate. */
  Authenticator NONE = new Authenticator() {
    @Override public Request authenticate(Route route, Response response) {
      return null;
    }
  };

  /**
   * Returns a request that includes a credential to satisfy an authentication challenge in {@code
   * response}. Returns null if the challenge cannot be satisfied.
   */
  Request authenticate(Route route, Response response) throws IOException;
}

