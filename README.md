# Spring DistributeLock

![Version](https://img.shields.io/badge/Version-1.0.5-brightgreen.svg)

![jdk    ](https://img.shields.io/badge/Jdk-1.7+-blue.svg)

![Spring ](https://img.shields.io/badge/Spring-4.2.5.RELEASE-blue.svg)

## 1. Description

 This is a distribute lock with spring. Lock key support `spel`. Default implement is Redis.

**DistributeLock**:

maven

```xml
<dependency>
    <groupId>com.github.blackshadowwalker.spring</groupId>
    <artifactId>spring-distributelock</artifactId>
    <version>1.0.8</version>
</dependency>
```

```java
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
	 * if empty will not throw LockException, just return false;
	 * @return
	 */
	String errMsg() default "Failed Get Lock";

	/**
	 * Timeout when getLock (seconds)
	 *
	 * @return
	 */
	long timeout() default 20;

	/**
     * key expire time (seconds) if gt; 0
     *
     * @return key expire time in seconds
     */
    long expire() default -1;

     /**
     * auto unlock , if false please set expire &gt 0
     * @return
     */
    boolean autoUnLock() default true;

}
```

## 2. Usage

### 2.1 config

spring xml config
```xml
<bean id="lockManager" class="com.github.blackshadowwalker.spring.distributelock.redis.RedisLockManager"
          p:lockPrefix="${lock.prefix}"
          p:cacheLock="false"
          p:redisTemplate-ref="stringRedisTemplate" />

<aop:aspectj-autoproxy proxy-target-class="false"/>
<bean class="com.github.blackshadowwalker.spring.distributelock.interceptor.LockAspectSupport" p:lockManager-ref="lockManager" />

```

### 2.2 define lock annotation

e.g:

```java
@DistributeLock(value = "updateUserStatus", key = "#userId", timeout = 10, expire = 60, errMsg = "更新失败，请刷新重试")
public Integer updateUserStatus(Long userId, Integer status) throws Exception {
    ...
    return ...;
}
```
