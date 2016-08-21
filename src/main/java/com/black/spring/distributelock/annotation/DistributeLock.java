package com.black.spring.distributelock.annotation;

import java.lang.annotation.*;

/**
 * Created by karl on 2016/8/21.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {

    // 锁的名字,默认是方法或类的名字
    String value() default "";

    // 锁的key 支持(Spel)
    String key() default "";

    // 获取锁 失败的提示 new LockException(errMsg)
    String errMsg() default "请勿重复提交";

    long timeout() default 60 * 1000;

}
