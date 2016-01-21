package com.weidian.plugin.task;

import android.os.Looper;
import android.util.Log;

/**
 * @author: wyouflf
 * @date: 2014/11/12
 */
public class TaskManager {

	private TaskManager() {
	}

	/**
	 * run task
	 *
	 * @param task
	 * @param <T>
	 * @return
	 */
	public static <T> Task<T> start(Task<T> task) {
		TaskProxy<T> proxy = null;
		if (task instanceof TaskProxy) {
			proxy = (TaskProxy<T>) task;
		} else {
			proxy = new TaskProxy<T>(task);
			task.taskProxy = proxy;
		}
		try {
			proxy.doBackground();
		} catch (Throwable ex) {
			Log.e("never happened", ex.getMessage(), ex);
		}
		return proxy;
	}

	/**
	 * run in UI thread
	 *
	 * @param runnable
	 */
	public static void post(Runnable runnable) {
		Looper mainLooper = Looper.getMainLooper();
		if (mainLooper != null && mainLooper.getThread() == Thread.currentThread()) {
			runnable.run();
		} else {
			TaskProxy.sHandler.post(runnable);
		}
	}

	/**
	 * run in UI thread
	 *
	 * @param runnable
	 * @param delayMillis
	 */
	public static void postDelayed(Runnable runnable, long delayMillis) {
		TaskProxy.sHandler.postDelayed(runnable, delayMillis);
	}

	/**
	 * run in background thread
	 *
	 * @param runnable
	 */
	public static void run(Runnable runnable) {
		TaskProxy.sDefaultExecutor.execute(runnable);
	}
}
