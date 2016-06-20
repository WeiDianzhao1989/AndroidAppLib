package com.koudai.net.toolbox;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoyu on 15/12/9.
 */
public final class MultiRequestWatchDog {

    private CountDownLatch latch;
    private AtomicInteger mainTaskSuccessCount;


    public MultiRequestWatchDog(int mainTaskCount) {
        latch = new CountDownLatch(mainTaskCount);
        this.mainTaskSuccessCount = new AtomicInteger();
    }

    public void countDown(boolean isSuccess) {

        if (isSuccess)
            this.mainTaskSuccessCount.incrementAndGet();

        latch.countDown();
    }

    public boolean await(int time, TimeUnit unit) throws InterruptedException {

        this.latch.await(time, unit);

        return mainTaskSuccessCount.get() > 0;

    }
}
