package com.black.spring.distributelock;

/**
 * Created by ASUS on 2016/8/16.
 */
public interface LockOperationInvoker {

	/**
	 * Invoke the cache operation defined by this instance. Wraps any
	 * exception that is thrown during the invocation in a
	 * {@link com.black.spring.distributelock.LockOperationInvoker.ThrowableWrapper}.
	 * 
	 * @return the result of the operation
	 * @throws com.black.spring.distributelock.LockOperationInvoker.ThrowableWrapper
	 *             if an error occurred while invoking the operation
	 */
	Object invoke() throws ThrowableWrapper;

	/**
	 * Wrap any exception thrown while invoking {@link #invoke()}
	 */
	@SuppressWarnings("serial")
	public static class ThrowableWrapper extends RuntimeException {

		private final Throwable original;

		public ThrowableWrapper(Throwable original) {
			super(original.getMessage(), original);
			this.original = original;
		}

		public Throwable getOriginal() {
			return original;
		}
	}
}
