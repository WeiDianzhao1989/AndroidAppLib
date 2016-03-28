package com.koudai.net.kernal.internal;

import com.koudai.net.io.Buffer;
import com.koudai.net.io.ForwardingSink;
import com.koudai.net.io.Sink;

import java.io.IOException;

/** A sink that never throws IOExceptions, even if the underlying sink does. */
class FaultHidingSink extends ForwardingSink {
  private boolean hasErrors;

  public FaultHidingSink(Sink delegate) {
    super(delegate);
  }

  @Override
  public void write(Buffer source, long byteCount) throws IOException {
    if (hasErrors) {
      source.skip(byteCount);
      return;
    }
    try {
      super.write(source, byteCount);
    } catch (IOException e) {
      hasErrors = true;
      onException(e);
    }
  }

  @Override
  public void flush() throws IOException {
    if (hasErrors) return;
    try {
      super.flush();
    } catch (IOException e) {
      hasErrors = true;
      onException(e);
    }
  }

  @Override
  public void close() throws IOException {
    if (hasErrors) return;
    try {
      super.close();
    } catch (IOException e) {
      hasErrors = true;
      onException(e);
    }
  }

  protected void onException(IOException e) {
  }
}
