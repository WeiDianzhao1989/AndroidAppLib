
package com.koudai.net.io;

/**
 * A collection of unused segments, necessary to avoid GC churn and zero-fill.
 * This pool is a thread-safe static singleton.
 */
final class SegmentPool {
  /** The maximum number of bytes to pool. */
  // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
  static final long MAX_SIZE = 64 * 1024; // 64 KiB.

  /** Singly-linked list of segments. */
  static Segment next;

  /** Total bytes in this pool. */
  static long byteCount;

  private SegmentPool() {
  }

  static Segment take() {
    synchronized (SegmentPool.class) {
      if (next != null) {
        Segment result = next;
        next = result.next;
        result.next = null;
        byteCount -= Segment.SIZE;
        return result;
      }
    }
    return new Segment(); // Pool is empty. Don't zero-fill while holding a lock.
  }

  static void recycle(Segment segment) {
    if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
    if (segment.shared) return; // This segment cannot be recycled.
    synchronized (SegmentPool.class) {
      if (byteCount + Segment.SIZE > MAX_SIZE) return; // Pool is full.
      byteCount += Segment.SIZE;
      segment.next = next;
      segment.pos = segment.limit = 0;
      next = segment;
    }
  }
}
