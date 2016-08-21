package com.black.spring.distributelock;

import com.black.spring.distributelock.annotation.DistributeLock;
import com.black.spring.distributelock.redis.RedisLockManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ASUS on 2016/8/16.
 */
@Component
@Aspect
public class LockAspectSupport {

	@Pointcut("@annotation(com.black.spring.distributelock.annotation.DistributeLock)")
	private void lockPoint() {
	}

	@Around("lockPoint()")
	public Object around(final ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		Method method = methodSignature.getMethod();
		try {
			LockOperationInvoker invoker = new LockOperationInvoker() {
				@Override
				public Object invoke() throws ThrowableWrapper {
					try {
						return pjp.proceed();
					} catch (Throwable e) {
						throw new ThrowableWrapper(e);
					}
				}
			};
			return execute(invoker, pjp.getTarget(), method, pjp.getArgs());
		} catch (LockOperationInvoker.ThrowableWrapper e) {
			throw e.getOriginal();
		}
	}

	private final LockExpressionEvaluator evaluator = new LockExpressionEvaluator();
	private LockKeyGenerator keyGenerator = new ParameterLockKeyGenerator();
	private LockManager defaultLockManager;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@PostConstruct
	public void init() {
		defaultLockManager = new RedisLockManager(redisTemplate);
	}

	public Object execute(LockOperationInvoker invoker, Object target, Method method, Object[] args) throws Exception {
		Class<?> targetClass = getTargetClass(target);
		Collection<LockOperation> operations = parseAnnotations(method, method);
		if (operations == null || operations.isEmpty()) {
			return invoker.invoke();
		}
		return execute(invoker, new LockContexts(operations, target, targetClass, method, args));
	}

	public Object execute(LockOperationInvoker invoker, LockContexts contexts) throws Exception {
		Collection<LockOperationContext> lockOperationContexts = contexts.get(LockOperation.class);
		List<Lock> locks = new ArrayList<Lock>();
		for (LockOperationContext context : lockOperationContexts) {
			//try lock
			String key = String.valueOf(context.generateLockKey(evaluator));//parse Spel
			Lock lock = defaultLockManager.getLock(context, key);
			locks.add(lock);
			lock.lock();
		}
		try {
			return invoker.invoke();
		} finally {
			for (Lock lock : locks) {
				lock.unlock();
			}
		}
	}

	private class LockContexts {
		private final MultiValueMap<Class<? extends LockOperation>, LockOperationContext> contexts = new LinkedMultiValueMap<Class<? extends LockOperation>, LockOperationContext>();

		public LockContexts(Collection<? extends LockOperation> operations, Object target, Class<?> targetClass, Method method,
				Object[] args) {
			for (LockOperation ops : operations) {
				LockOperationMetadata metadata = new LockOperationMetadata(ops, target, targetClass, method, keyGenerator,
						defaultLockManager);
				this.contexts.add(ops.getClass(), new LockOperationContext(metadata, target, method, args));
			}
		}

		public Collection<LockOperationContext> get(Class<? extends LockOperation> operationClass) {
			Collection<LockOperationContext> result = this.contexts.get(operationClass);
			return (result != null ? result : Collections.<LockOperationContext> emptyList());
		}
	}

	private Class<?> getTargetClass(Object target) {
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (targetClass == null && target != null) {
			targetClass = target.getClass();
		}
		return targetClass;
	}

	private Collection<LockOperation> parseAnnotations(AnnotatedElement ae, Method method) {
		List<LockOperation> list = new ArrayList<LockOperation>();
		DistributeLock lock = ae.getAnnotation(DistributeLock.class);
		if (lock != null) {
			list.add(
					new LockOperation(lock.value() != null ? lock.value() : method.getName(), lock.key(), lock.timeout(), lock.errMsg()));
		}
		return list;
	}

}
