package com.black.spring.distributelock;

import com.black.spring.distributelock.interceptor.LockOperationContext;

/**
 * Created by ASUS on 2016/8/16.
 */
public interface LockManager {

	Lock getLock(LockOperationContext context, String key);

}
