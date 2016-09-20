package com.github.blackshadowwalker.spring.distributelock.interceptor;

import com.github.blackshadowwalker.spring.distributelock.LockKeyGenerator;
import com.github.blackshadowwalker.spring.distributelock.LockManager;
import com.github.blackshadowwalker.spring.distributelock.LockKeyGenerator;
import com.github.blackshadowwalker.spring.distributelock.LockManager;

import java.lang.reflect.Method;

/**
 * Created by ASUS on 2016/8/16.
 */
public class LockOperationMetadata {

	private final LockOperation operation;
	private final Object target;
	private final Method method;
	private final Class<?> targetClass;
	private final LockKeyGenerator keyGenerator;
	private final LockManager lockManager;

	public LockOperationMetadata(LockOperation operation, Object target, Class<?> targetClass, Method method, LockKeyGenerator keyGenerator,
			LockManager lockManager) {
		this.operation = operation;
		this.target = target;
		this.method = method;
		this.targetClass = targetClass;
		this.keyGenerator = keyGenerator;
		this.lockManager = lockManager;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public LockOperation getOperation() {
		return operation;
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public LockKeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

}
