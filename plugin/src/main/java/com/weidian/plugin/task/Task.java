package com.weidian.plugin.task;

import com.weidian.plugin.task.pool.Priority;

import java.util.concurrent.Executor;

public abstract class Task<ResultType> {

    /*package*/ volatile State state = State.Null;
    /*package*/ TaskProxy taskProxy = null;

    protected abstract ResultType doBackground() throws Exception;

    protected abstract void onFinished(ResultType result);

    protected abstract void onError(Throwable ex, boolean isCallbackError);

    protected void onStart() {
    }

    protected void onUpdate(int flag, Object... args) {
    }

    protected void onCancelled(CancelledException cex) {
    }

    public final void update(int flag, Object... args) {
        if (taskProxy != null) {
            taskProxy.onUpdate(flag, args);
        }
    }

    public final void cancel() {
        this.state = State.Cancelled;
        if (taskProxy != null) {
            taskProxy.cancel();
        }
    }

    public final State getState() {
        return state;
    }

    public final boolean isStopped() {
        return this.state.value() > State.Running.value();
    }

    public Priority getPriority() {
        return null;
    }

    public Executor getExecutor() {
        return null;
    }

    public static class CancelledException extends RuntimeException {
        public CancelledException(String detailMessage) {
            super(detailMessage);
        }
    }

    public enum State {
        Null(0), Waiting(1), Running(2), Finished(3), Cancelled(4), Error(5);
        private final int value;

        State(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }
}
