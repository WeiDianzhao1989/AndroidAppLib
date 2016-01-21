package com.weidian.plugin.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.weidian.plugin.task.pool.Priority;
import com.weidian.plugin.task.pool.PriorityExecutor;
import com.weidian.plugin.task.pool.PriorityRunnable;

import java.util.concurrent.Executor;

/*package*/ final class TaskProxy<ResultType> extends Task<ResultType> {

	private final Task<ResultType> task;
	private Executor executor;
	/*package*/ static final InternalHandler sHandler = new InternalHandler();
	/*package*/ static final PriorityExecutor sDefaultExecutor = new PriorityExecutor();

	private ResultType result;
	private Throwable exception;
	private CancelledException cancelledException;

	/*package*/ TaskProxy(Task<ResultType> task) {
		if (task == null) {
			throw new IllegalArgumentException("task must not be null");
		}

		this.task = task;
		this.executor = task.getExecutor();
		if (this.executor == null) {
			this.executor = sDefaultExecutor;
		}
	}

	@Override
	protected ResultType doBackground() throws Exception {
		this.setState(State.Waiting);
		PriorityRunnable runnable = new PriorityRunnable(
			task.getPriority(),
			new Runnable() {
				@Override
				public void run() {
					try {
						// start running
						TaskProxy.this.setState(State.Running);
						TaskProxy.this.onStart();

						result = task.doBackground();
						if (TaskProxy.this.state == State.Cancelled) { // 没有在doBackground过程中取消成功
							throw new CancelledException("");
						}
						TaskProxy.this.setState(State.Finished);
						TaskProxy.this.onFinished(result);
					} catch (CancelledException cex) {
						TaskProxy.this.setState(State.Cancelled);
						TaskProxy.this.onCancelled(cex);
					} catch (Throwable ex) {
						TaskProxy.this.setState(State.Error);
						TaskProxy.this.onError(ex, false);
					}
				}
			});
		this.executor.execute(runnable);
		return null;
	}

	@Override
	protected void onFinished(ResultType result) {
		sHandler.obtainMessage(MSG_WHAT_ON_FINISH, this).sendToTarget();
	}

	@Override
	protected void onError(Throwable ex, boolean isCallbackError) {
		exception = ex;
		sHandler.obtainMessage(MSG_WHAT_ON_ERROR, this).sendToTarget();
	}

	@Override
	protected void onStart() {
		sHandler.obtainMessage(MSG_WHAT_ON_START, this).sendToTarget();
	}

	@Override
	protected void onUpdate(int flag, Object... args) {
		sHandler.obtainMessage(MSG_WHAT_ON_UPDATE, flag, 0, new ArgsObj(this, args)).sendToTarget();
	}

	@Override
	protected void onCancelled(CancelledException cex) {
		cancelledException = cex;
		sHandler.obtainMessage(MSG_WHAT_ON_CANCEL, this).sendToTarget();
	}

	private void setState(State state) {
		this.state = state;
		this.task.state = state;
	}

	@Override
	public Priority getPriority() {
		return task.getPriority();
	}

	@Override
	public Executor getExecutor() {
		return task.getExecutor();
	}

	// ########################### inner type #############################
	private static class ArgsObj {
		TaskProxy taskProxy;
		Object[] args;

		public ArgsObj(TaskProxy taskProxy, Object[] args) {
			this.taskProxy = taskProxy;
			this.args = args;
		}
	}

	private final static int MSG_WHAT_ON_START = 1;
	private final static int MSG_WHAT_ON_FINISH = 2;
	private final static int MSG_WHAT_ON_ERROR = 3;
	private final static int MSG_WHAT_ON_UPDATE = 4;
	private final static int MSG_WHAT_ON_CANCEL = 5;

	/*package*/ final static class InternalHandler extends Handler {

		private InternalHandler() {
			super(Looper.getMainLooper());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj == null) {
				throw new IllegalArgumentException("msg must not be null");
			}
			TaskProxy taskProxy = null;
			Object[] args = null;
			if (msg.obj instanceof TaskProxy) {
				taskProxy = (TaskProxy) msg.obj;
			} else if (msg.obj instanceof ArgsObj) {
				ArgsObj argsObj = (ArgsObj) msg.obj;
				taskProxy = argsObj.taskProxy;
				args = argsObj.args;
			}
			if (taskProxy == null) {
				throw new RuntimeException("msg.obj not instanceof TaskProxy");
			}

			try {
				switch (msg.what) {
					case MSG_WHAT_ON_START: {
						taskProxy.task.onStart();
						break;
					}
					case MSG_WHAT_ON_FINISH: {
						taskProxy.task.onFinished(taskProxy.result);
						break;
					}
					case MSG_WHAT_ON_ERROR: {
						taskProxy.task.onError(taskProxy.exception, false);
						break;
					}
					case MSG_WHAT_ON_UPDATE: {
						taskProxy.task.onUpdate(msg.arg1, args);
						break;
					}
					case MSG_WHAT_ON_CANCEL: {
						taskProxy.task.onCancelled(taskProxy.cancelledException);
						break;
					}
					default: {
						break;
					}
				}
			} catch (Throwable ex) {
				taskProxy.setState(State.Error);
				if (msg.what != MSG_WHAT_ON_ERROR) {
					taskProxy.task.onError(ex, true);
				} else {
					ex.printStackTrace();
				}
			}
		}
	}
}
