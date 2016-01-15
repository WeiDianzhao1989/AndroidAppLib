package com.koudai.net.kernal.internal.http;

import com.koudai.net.io.Sink;

import java.io.IOException;

public interface CacheRequest {
  Sink body() throws IOException;
  void abort();
}
