package com.black.spring.distributelock.interceptor;

import com.black.spring.distributelock.Lock;
import com.black.spring.distributelock.LockKeyGenerator;
import com.black.spring.distributelock.LockManager;
import com.black.spring.distributelock.annotation.DistributeLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
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
    private LockManager lockManager;

    @PostConstruct
    public void init() {

    }

    public Object execute(LockOperationInvoker invoker, Object target, Method method, Object[] args) throws Exception {
        Class<?> targetClass = getTargetClass(target);
        Collection<LockOperation> operations = parseAnnotations(targetClass, method, method);
        if (operations == null || operations.isEmpty()) {
            return invoker.invoke();
        }
        return execute(invoker, new LockContexts(operations, target, targetClass, method, args));
    }

    public Object execute(LockOperationInvoker invoker, LockContexts contexts) throws Exception {
        Collection<LockOperationContext> lockOperationContexts = contexts.get(LockOperation.class);
        List<Lock> locks = new ArrayList<Lock>();
        boolean locked = true;
        for (LockOperationContext context : lockOperationContexts) {
            //try lock
            String key = String.valueOf(context.generateLockKey(evaluator));//parse Spel
            Lock lock = lockManager.getLock(context, key);
            locks.add(lock);
            locked &= lock.lock();
        }
        if (locked) {
            return null;
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
                LockOperationMetadata metadata = new LockOperationMetadata(ops, target, targetClass, method, keyGenerator, lockManager);
                this.contexts.add(ops.getClass(), new LockOperationContext(metadata, target, method, args));
            }
        }

        public Collection<LockOperationContext> get(Class<? extends LockOperation> operationClass) {
            Collection<LockOperationContext> result = this.contexts.get(operationClass);
            return (result != null ? result : Collections.<LockOperationContext>emptyList());
        }
    }

    private Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null && target != null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    private Collection<LockOperation> parseAnnotations(Class<?> targetClass, AnnotatedElement ae, Method method) {
        List<LockOperation> list = new ArrayList<LockOperation>();
        DistributeLock lock = ae.getAnnotation(DistributeLock.class);
        if (lock != null) {
            String name = lock.value();
            if (name.isEmpty()) {
                name = targetClass.getSimpleName() + "#" + method.getName();
            }
            list.add(new LockOperation(name, lock.key(), lock.timeout(), lock.expire(), lock.errMsg()));
        }
        return list;
    }

    public void setKeyGenerator(LockKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

}
