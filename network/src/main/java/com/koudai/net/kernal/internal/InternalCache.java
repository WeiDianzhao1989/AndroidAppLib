package com.koudai.net.kernal.internal;

import com.koudai.net.kernal.Request;
import com.koudai.net.kernal.Response;
import com.koudai.net.kernal.internal.http.CacheRequest;
import com.koudai.net.kernal.internal.http.CacheStrategy;

import java.io.IOException;

/**
 * OkHttp's kernal cache interface. Applications shouldn't implement this:
 * instead use {@link com.koudai.net.kernal.Cache}.
 */
public interface InternalCache {
  Response get(Request request) throws IOException;

  CacheRequest put(Response response) throws IOException;

  /**
   * Remove any cache entries for the supplied {@code request}. This is invoked
   * when the client invalidates the cache, such as when making POST requests.
   */
  void remove(Request request) throws IOException;

  /**
   * Handles a conditional request hit by updating the stored cache response
   * with the headers from {@code network}. The cached response body is not
   * updated. If the stored response has changed since {@code cached} was
   * returned, this does nothing.
   */
  void update(Response cached, Response network) throws IOException;

  /** Track an conditional GET that was satisfied by this cache. */
  void trackConditionalCacheHit();

  /** Track an HTTP response being satisfied with {@code cacheStrategy}. */
  void trackResponse(CacheStrategy cacheStrategy);
}
