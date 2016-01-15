
package com.koudai.net.kernal.internal.framed;

import com.koudai.net.io.BufferedSink;
import com.koudai.net.io.BufferedSource;
import com.koudai.net.kernal.Protocol;

/** A version and dialect of the framed socket protocol. */
public interface Variant {

  /** The protocol as selected using ALPN. */
  Protocol getProtocol();

  /**
   * @param client true if this is the HTTP client's reader, reading frames from a server.
   */
  FrameReader newReader(BufferedSource source, boolean client);

  /**
   * @param client true if this is the HTTP client's writer, writing frames to a server.
   */
  FrameWriter newWriter(BufferedSink sink, boolean client);
}
