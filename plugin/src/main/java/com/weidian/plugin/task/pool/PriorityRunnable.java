package com.weidian.plugin.task.pool;

public class PriorityRunnable extends PriorityObject<Runnable> implements Runnable {

    public PriorityRunnable(Priority priority, Runnable obj) {
        super(priority, obj);
    }

    @Override
    public final void run() {
        this.obj.run();
    }
}
