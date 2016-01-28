package com.weidian.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by zhaoyu on 15/6/29.
 */
public class PullAndAutoLoadListView extends PullToRefreshAdapterViewBase<ListView> {

    private LoadingLayout mHeaderLoadingView;
    private LoadingLayout mFooterLoadingView;

    private FrameLayout mLvFooterLoadingFrame;

    public PullAndAutoLoadListView(Context context) {
        super(context);
    }

    public PullAndAutoLoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullAndAutoLoadListView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullAndAutoLoadListView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mRefreshableView.setAdapter(adapter);
    }

    @Override
    public ListAdapter getAdapter() {
        return mRefreshableView.getAdapter();
    }

    @Override
    protected void handleStyledAttributes(TypedArray a) {
        super.handleStyledAttributes(a);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

        // Create Loading Views ready for use later
        FrameLayout frame = new FrameLayout(getContext());
        mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, a);
        mHeaderLoadingView.setVisibility(View.GONE);
        frame.addView(mHeaderLoadingView, lp);
        mRefreshableView.addHeaderView(frame, null, false);

        mLvFooterLoadingFrame = new FrameLayout(getContext());
        mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, a);
        if (getMode() == Mode.AUTO_LOAD || getMode() == Mode.PULL_START_AND_AUTO_REFRESH) {
            mFooterLoadingView.setVisibility(View.VISIBLE);
            mFooterLoadingView.refreshing();
            mFooterLoadingView.setRetryListener(this);
        } else {
            mFooterLoadingView.setVisibility(View.GONE);
        }
        mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);

        /**
         * If the value for Scrolling While Refreshing hasn't been
         * explicitly set via XML, enable Scrolling While Refreshing.
         */
        if (!a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
            setScrollingWhileRefreshingEnabled(true);
        }
    }

    @Override
    protected final void onResetFinish() {
        //如果不满一屏自动触发加载下一页数据
        autoLoadMore();
    }

    private void autoLoadMore() {
        ListView refreshableView = getRefreshableView();
        int lastVisiblePosition = refreshableView.getLastVisiblePosition();
        int count = refreshableView.getCount();
        int headerCount = refreshableView.getFooterViewsCount();
        int footerCount = refreshableView.getHeaderViewsCount();
        if(lastVisiblePosition >=0) {
            if (lastVisiblePosition >= (count - 1)
                    && (count > (headerCount + footerCount))) {
                onLoadingMore();
            }
        }else if(lastVisiblePosition < 0 && count > 0
                && (count > (headerCount + footerCount))){
            post(new Runnable() {
                @Override
                public void run() {
                    autoLoadMore();
                }
            });
        }
    }

    @Override
    protected void updateUIForMode() {
        super.updateUIForMode();
        if (getMode() == Mode.AUTO_LOAD || getMode() == Mode.PULL_START_AND_AUTO_REFRESH) {
            mFooterLoadingView.setVisibility(View.VISIBLE);
            if (!mFooterLoadingView.isRefreshing()) {
                mFooterLoadingView.refreshing();
            }
            mFooterLoadingView.setRetryListener(this);
        } else {
            mFooterLoadingView.reset();
            getFooterLayout().showInvisibleViews();
            mFooterLoadingView.setVisibility(View.GONE);
            mFooterLoadingView.setRetryListener(null);
        }
    }

    @Override
    protected int getHeaderCount() {
        return mRefreshableView.getHeaderViewsCount();
    }

    @Override
    protected void onRefreshing(final boolean doScroll) {

        ListAdapter adapter = mRefreshableView.getAdapter();
        if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
            super.onRefreshing(doScroll);
            return;
        }

        super.onRefreshing(false);

        final LoadingLayout origLoadingView, listViewLoadingView, oppositeListViewLoadingView;
        final int selection, scrollToY;

        switch (getCurrentMode()) {
            case PULL_FROM_END:
                origLoadingView = getFooterLayout();
                listViewLoadingView = mFooterLoadingView;
                oppositeListViewLoadingView = mHeaderLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToY = getScrollY() - getFooterSize();
                break;
            case AUTO_LOAD:
                origLoadingView = getFooterLayout();
                listViewLoadingView = mFooterLoadingView;
                oppositeListViewLoadingView = mHeaderLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToY = 0;
                break;
            case PULL_FROM_START:
            case MANUAL_REFRESH_ONLY:
            default:
                origLoadingView = getHeaderLayout();
                listViewLoadingView = mHeaderLoadingView;
                oppositeListViewLoadingView = mFooterLoadingView;
                selection = 0;
                scrollToY = getScrollY() + getHeaderSize();
                break;
        }

        //if (getCurrentMode() != Mode.AUTO_LOAD) {
        // Hide our original Loading View
        origLoadingView.reset();
        origLoadingView.hideAllViews();
        //}

        // Make sure the opposite end is hidden too
        oppositeListViewLoadingView.setVisibility(View.GONE);

        // Show the ListView Loading View and set it to refresh.
        if (listViewLoadingView.getVisibility() != View.VISIBLE) {
            listViewLoadingView.setVisibility(View.VISIBLE);
        }

        if (!listViewLoadingView.isRefreshing()) {
            listViewLoadingView.refreshing();
        }

        if (doScroll) {
            // We need to disable the automatic visibility changes for now
            disableLoadingLayoutVisibilityChanges();


            // We scroll slightly so that the ListView's header/footer is at the
            // same Y position as our normal header/footer
            setHeaderScroll(scrollToY);

            // Make sure the ListView is scrolled to show the loading
            // header/footer
            mRefreshableView.setSelection(selection);

            // Smooth scroll as normal
            smoothScrollTo(0);

        } else {
            if (getCurrentMode() == Mode.AUTO_LOAD) {
                disableLoadingLayoutVisibilityChanges();
            }
        }


    }

    @Override
    protected void onReset() {

        final LoadingLayout originalLoadingLayout, listViewLoadingLayout, oppositeListViewLoadingView;
        final int scrollToHeight, selection;
        final boolean scrollLvToEdge;

        switch (getCurrentMode()) {
            case PULL_FROM_END:
                originalLoadingLayout = getFooterLayout();
                listViewLoadingLayout = mFooterLoadingView;
                oppositeListViewLoadingView = mHeaderLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToHeight = getFooterSize();
                scrollLvToEdge = Math.abs(mRefreshableView.getLastVisiblePosition() - selection) <= 1;
                break;
            case AUTO_LOAD:
                originalLoadingLayout = getFooterLayout();
                listViewLoadingLayout = mFooterLoadingView;
                oppositeListViewLoadingView = mHeaderLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToHeight = getFooterSize();
                scrollLvToEdge = Math.abs(mRefreshableView.getLastVisiblePosition() - selection) <= 1;
                break;
            case PULL_FROM_START:
            case MANUAL_REFRESH_ONLY:
            default:
                originalLoadingLayout = getHeaderLayout();
                listViewLoadingLayout = mHeaderLoadingView;
                oppositeListViewLoadingView = mFooterLoadingView;
                scrollToHeight = -getHeaderSize();
                selection = 0;
                scrollLvToEdge = Math.abs(mRefreshableView.getFirstVisiblePosition() - selection) <= 1;
                break;
        }

        // If the ListView header loading layout is showing, then we need to
        // flip so that the original one is showing instead
        if (listViewLoadingLayout.getVisibility() == View.VISIBLE && getCurrentMode() != Mode.AUTO_LOAD) {

            // Set our Original View to Visible
            originalLoadingLayout.showInvisibleViews();

            // Hide the ListView Header/Footer
            listViewLoadingLayout.setVisibility(View.GONE);

            /**
             * Scroll so the View is at the same Y as the ListView
             * header/footer, but only scroll if: we've pulled to refresh, it's
             * positioned correctly
             */
            if (scrollLvToEdge && getState() != State.MANUAL_REFRESHING) {
                mRefreshableView.setSelection(selection);
                setHeaderScroll(scrollToHeight);
            }
        }

        if ((getMode() == Mode.AUTO_LOAD ||
                getMode() == Mode.PULL_START_AND_AUTO_REFRESH)) {
            if (getCurrentMode() == Mode.PULL_FROM_START) {
                if (oppositeListViewLoadingView != null) {
                    if (oppositeListViewLoadingView.getVisibility() != View.VISIBLE) {
                        oppositeListViewLoadingView.setVisibility(View.VISIBLE);
                        if (!oppositeListViewLoadingView.isRefreshing()) {
                            oppositeListViewLoadingView.refreshing();
                        }
                    }
                }
            } else if (getCurrentMode() == Mode.AUTO_LOAD) {
                if (!listViewLoadingLayout.isRefreshing()) {
                    listViewLoadingLayout.refreshing();
                }
            }
        }

        // Finally, call up to super
        super.onReset();
    }

    @Override
    protected void onPauseAutoLoading() {
        super.onPauseAutoLoading();
        mFooterLoadingView.stopAutoLoading();
    }

    @Override
    protected void onFinishAutoLoading() {
        super.onFinishAutoLoading();

        mFooterLoadingView.finishAutoLoading();
    }

    @Override
    protected LoadingLayoutProxy createLoadingLayoutProxy(boolean includeStart, boolean includeEnd) {
        LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(includeStart, includeEnd);

        final Mode mode = getMode();

        if (includeStart && mode.showHeaderLoadingLayout()) {
            proxy.addLayout(mHeaderLoadingView);
        }
        if (includeEnd && mode.showFooterLoadingLayout()) {
            proxy.addLayout(mFooterLoadingView);
        }

        return proxy;
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    protected ListView createListView(Context context, AttributeSet attrs) {
        final ListView lv;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            lv = new InternalListViewSDK9(context, attrs);
        } else {
            lv = new InternalListView(context, attrs);
        }
        return lv;
    }

    @Override
    protected ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView lv = createListView(context, attrs);

        // Set it to this so it can be used in ListActivity/ListFragment
        lv.setId(android.R.id.list);
        return lv;
    }


    public void addHeaderView(View v, Object data, boolean isSelectable) {
        mRefreshableView.addHeaderView(v, data, isSelectable);
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        mRefreshableView.addFooterView(v, data, isSelectable);
    }

    public void removeHeaderView(View v) {
        mRefreshableView.removeHeaderView(v);
    }

    public int getHeaderViewsCount() {
        return mRefreshableView.getHeaderViewsCount();
    }

    public int getFirstVisiblePosition() {
        return mRefreshableView.getFirstVisiblePosition();
    }

    public void setSelection(int selection) {
        mRefreshableView.setSelection(selection);
    }

    public void smoothScrollBy(int distance, int duration) {
        mRefreshableView.smoothScrollBy(distance, duration);
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mRefreshableView.setOnTouchListener(onTouchListener);
    }

    @TargetApi(9)
    final class InternalListViewSDK9 extends InternalListView {

        public InternalListViewSDK9(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
                                       int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullAndAutoLoadListView.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

            return returnValue;
        }
    }

    protected class InternalListView extends ListView implements EmptyViewMethodAccessor {

        private boolean mAddedLvFooter = false;

        public InternalListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                super.dispatchDraw(canvas);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            /**
             * This is a bit hacky, but Samsung's ListView has got a bug in it
             * when using Header/Footer Views and the list is empty. This masks
             * the issue so that it doesn't cause an FC. See Issue #66.
             */
            try {
                return super.dispatchTouchEvent(ev);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            // Add the Footer View at the last possible moment
            if (null != mLvFooterLoadingFrame && !mAddedLvFooter) {
                addFooterView(mLvFooterLoadingFrame, null, false);
                mAddedLvFooter = true;
            }

            super.setAdapter(adapter);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullAndAutoLoadListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }

    }
}
