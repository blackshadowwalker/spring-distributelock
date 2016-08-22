package com.black.spring.distributelock.annotation;

import java.lang.annotation.*;

/**
 * Created by karl on 2016/8/21.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {

	/**
	 * The lock Name, if empty the value is `targetClass.getName() + "#" + method.getName()`.
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * Lock key (Spel), if empty keyGenerator will be work.
	 */
	String key() default "";

	/**
	 * The error msg if get lock failed(new LockException(errMsg))
	 * 
	 * @return
	 */
	String errMsg() default "Failed Get Lock";

	/**
	 * Timeout when getLock (seconds)
	 * 
	 * @return
	 */
	long timeout() default 20;

}
