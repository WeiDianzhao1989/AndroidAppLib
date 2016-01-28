package com.weidian.pulltorefresh;

import android.view.View;
import android.view.animation.Interpolator;

import com.weidian.pulltorefresh.PullToRefreshBase.*;

public interface IPullToRefresh<T extends View> {

	/**
	 * Get the mode that this view is currently in. This is only really useful
	 * when using <code>Mode.BOTH</code>.
	 * 
	 * @return Mode that the view is currently in
	 */
	Mode getCurrentMode();

	/**
	 * Returns whether the Touch Events are filtered or not. If true is
	 * returned, then the View will only use touch events where the difference
	 * in the Y-axis is greater than the difference in the X-axis. This means
	 * that the View will not interfere when it is used in a horizontal
	 * scrolling View (such as a ViewPager).
	 * 
	 * @return boolean - true if the View is filtering Touch Events
	 */
	boolean getFilterTouchEvents();

	/**
	 * Returns a proxy object which allows you to call methods on all of the
	 * LoadingLayouts (the Views which show when Pulling/Refreshing).
	 * <p />
	 * You should not keep the result of this method any longer than you need
	 * it.
	 * 
	 * @return Object which will proxy any calls you make on it, to all of the
	 *         LoadingLayouts.
	 */
	ILoadingLayout getLoadingLayoutProxy();

	/**
	 * Returns a proxy object which allows you to call methods on the
	 * LoadingLayouts (the Views which show when Pulling/Refreshing). The actual
	 * LoadingLayout(s) which will be affected, are chosen by the parameters you
	 * give.
	 * <p />
	 * You should not keep the result of this method any longer than you need
	 * it.
	 * 
	 * @param includeStart - Whether to include the Start/Header Views
	 * @param includeEnd - Whether to include the End/Footer Views
	 * @return Object which will proxy any calls you make on it, to the
	 *         LoadingLayouts included.
	 */
	ILoadingLayout getLoadingLayoutProxy(boolean includeStart, boolean includeEnd);

	/**
	 * Get the mode that this view has been set to. If this returns
	 * <code>Mode.BOTH</code>, you can use <code>getCurrentMode()</code> to
	 * check which mode the view is currently in
	 * 
	 * @return Mode that the view has been set to
	 */
	Mode getMode();

	/**
	 * Get the Wrapped Refreshable View. Anything returned here has already been
	 * added to the content view.
	 * 
	 * @return The View which is currently wrapped
	 */
	T getRefreshableView();

	/**
	 * Get whether the 'Refreshing' View should be automatically shown when
	 * refreshing. Returns true by default.
	 * 
	 * @return - true if the Refreshing View will be show
	 */
	boolean getShowViewWhileRefreshing();

	/**
	 * @return - The state that the View is currently in.
	 */
	State getState();

	/**
	 * Whether Pull-to-Refresh is enabled
	 * 
	 * @return enabled
	 */
	boolean isPullToRefreshEnabled();

	/**
	 * Gets whether Overscroll support is enabled. This is different to
	 * Android's standard Overscroll support (the edge-glow) which is available
	 * from GINGERBREAD onwards
	 * 
	 * @return true - if both PullToRefresh-OverScroll and Android's inbuilt
	 *         OverScroll are enabled
	 */
	boolean isPullToRefreshOverScrollEnabled();

	/**
	 * Returns whether the Widget is currently in the Refreshing mState
	 * 
	 * @return true if the Widget is currently refreshing
	 */
	boolean isRefreshing();

	/**
	 * Returns whether the widget has enabled scrolling on the Refreshable View
	 * while refreshing.
	 * 
	 * @return true if the widget has enabled scrolling while refreshing
	 */
	boolean isScrollingWhileRefreshingEnabled();

	/**
	 * Mark the current Refresh as complete. Will Reset the UI and hide the
	 * Refreshing View
	 */
	void onRefreshComplete();

	/**
	 * Set the Touch Events to be filtered or not. If set to true, then the View
	 * will only use touch events where the difference in the Y-axis is greater
	 * than the difference in the X-axis. This means that the View will not
	 * interfere when it is used in a horizontal scrolling View (such as a
	 * ViewPager), but will restrict which types of finger scrolls will trigger
	 * the View.
	 * 
	 * @param filterEvents - true if you want to filter Touch Events. Default is
	 *            true.
	 */
	void setFilterTouchEvents(boolean filterEvents);

	/**
	 * Set the mode of Pull-to-Refresh that this view will use.
	 * 
	 * @param mode - Mode to set the View to
	 */
	void setMode(Mode mode);

	/**
	 * Set OnPullEventListener for the Widget
	 * 
	 * @param listener - Listener to be used when the Widget has a pull event to
	 *            propogate.
	 */
	void setOnPullEventListener(OnPullEventListener<T> listener);

	/**
	 * Set OnRefreshListener for the Widget
	 * 
	 * @param listener - Listener to be used when the Widget is set to Refresh
	 */
	void setOnRefreshListener(OnRefreshListener<T> listener);

	/**
	 * Set OnRefreshListener for the Widget
	 * 
	 * @param listener - Listener to be used when the Widget is set to Refresh
	 */
	void setOnRefreshListener(OnRefreshListener2<T> listener);

	/**
	 * Sets whether Overscroll support is enabled. This is different to
	 * Android's standard Overscroll support (the edge-glow). This setting only
	 * takes effect when running on device with Android v2.3 or greater.
	 * 
	 * @param enabled - true if you want Overscroll enabled
	 */
	void setPullToRefreshOverScrollEnabled(boolean enabled);

	/**
	 * Sets the Widget to be in the refresh state. The UI will be updated to
	 * show the 'Refreshing' view, and be scrolled to show such.
	 */
	void setRefreshing();

	/**
	 * Sets the Widget to be in the refresh state. The UI will be updated to
	 * show the 'Refreshing' view.
	 * 
	 * @param doScroll - true if you want to force a scroll to the Refreshing
	 *            view.
	 */
	void setRefreshing(boolean doScroll);

	/**
	 * Sets the Animation Interpolator that is used for animated scrolling.
	 * Defaults to a DecelerateInterpolator
	 * 
	 * @param interpolator - Interpolator to use
	 */
	void setScrollAnimationInterpolator(Interpolator interpolator);

	/**
	 * By default the Widget disables scrolling on the Refreshable View while
	 * refreshing. This method can change this behaviour.
	 * 
	 * @param scrollingWhileRefreshingEnabled - true if you want to enable
	 *            scrolling while refreshing
	 */
	void setScrollingWhileRefreshingEnabled(boolean scrollingWhileRefreshingEnabled);

	/**
	 * A mutator to enable/disable whether the 'Refreshing' View should be
	 * automatically shown when refreshing.
	 * 
	 * @param showView
	 */
	void setShowViewWhileRefreshing(boolean showView);


    boolean isAutoLoadingFinish();

	boolean isAutoLoadingPause();

    void pauseAutoLoading();

    void onAutoLoadingFinish();

	void onLoadingMore();

}