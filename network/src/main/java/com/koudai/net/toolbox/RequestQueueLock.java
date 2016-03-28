package com.koudai.net.toolbox;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhaoyu on 16/3/9.
 * 用于同步请求队列的锁
 */
public final class RequestQueueLock {

    private Lock lock = new ReentrantLock(false);
    private Condition condition = lock.newCondition();

    public void lock() throws InterruptedException {
        lock.lockInterruptibly();
    }

    public void unlock() {
        lock.unlock();
    }

    public void waitFor() throws InterruptedException {
        condition.await();
    }

    public void signalAll() {
        condition.signalAll();
    }
}
