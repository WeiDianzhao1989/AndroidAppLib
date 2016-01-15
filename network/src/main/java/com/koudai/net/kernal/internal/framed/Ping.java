package com.koudai.net.kernal.internal.framed;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A locally-originated ping.
 */
public final class Ping {
  private final CountDownLatch latch = new CountDownLatch(1);
  private long sent = -1;
  private long received = -1;

  Ping() {
  }

  void send() {
    if (sent != -1) throw new IllegalStateException();
    sent = System.nanoTime();
  }

  void receive() {
    if (received != -1 || sent == -1) throw new IllegalStateException();
    received = System.nanoTime();
    latch.countDown();
  }

  void cancel() {
    if (received != -1 || sent == -1) throw new IllegalStateException();
    received = sent - 1;
    latch.countDown();
  }

  /**
   * Returns the round trip time for this ping in nanoseconds, waiting for the
   * response to arrive if necessary. Returns -1 if the response was
   * canceled.
   */
  public long roundTripTime() throws InterruptedException {
    latch.await();
    return received - sent;
  }

  /**
   * Returns the round trip time for this ping in nanoseconds, or -1 if the
   * response was canceled, or -2 if the timeout elapsed before the round
   * trip completed.
   */
  public long roundTripTime(long timeout, TimeUnit unit) throws InterruptedException {
    if (latch.await(timeout, unit)) {
      return received - sent;
    } else {
      return -2;
    }
  }
}
