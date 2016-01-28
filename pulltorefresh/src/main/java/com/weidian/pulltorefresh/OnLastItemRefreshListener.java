package com.weidian.pulltorefresh;

/**
 * Created by zhaoyu on 15/4/2.
 */
public class OnLastItemRefreshListener<T extends PullToRefreshBase> implements PullToRefreshBase.OnLastItemVisibleListener {

    private T internalView;

    public OnLastItemRefreshListener(T internalView) {
        this.internalView = internalView;
    }

    @Override
    public void onLastItemVisible() {
        if (internalView != null) {
            internalView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            internalView.post(new Runnable() {
                @Override
                public void run() {
                    internalView.setRefreshing();
                    internalView.setMode(PullToRefreshBase.Mode.BOTH);
                }
            });
        }
    }
}
