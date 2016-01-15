package com.koudai.net.toolbox;

/**
 * Created by zhaoyu on 15/11/8.
 * 网络加载流程的控制器，目前提供了取消，未来会提供暂停，重新开始等多种控制
 */
public final class RequestControl {

    private HttpRequestRunnable control;//网络任务

    public RequestControl(HttpRequestRunnable control) {
        this.control = control;
    }

    public void cancel() {
        if (control != null && !control.isFinished()) {
            CancelQueue.getInstance().enqueue(new CancelQueue.CancelItem(control, false));
        }
    }

    public void cancel(boolean isNotify) {
        if (control != null && !control.isFinished()) {
            CancelQueue.getInstance().enqueue(new CancelQueue.CancelItem(control, isNotify));
        }
    }

    public boolean isCanceled() {
        return control != null && control.isCanceled();
    }

    public boolean isFinished() {
        return control != null && control.isFinished();
    }

}
