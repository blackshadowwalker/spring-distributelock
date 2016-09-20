package com.github.blackshadowwalker.spring.distributelock;

import com.github.blackshadowwalker.spring.distributelock.interceptor.LockOperationContext;

/**
 * Created by ASUS on 2016/8/16.
 */
public interface LockManager {

	Lock getLock(LockOperationContext context, String key);

}
