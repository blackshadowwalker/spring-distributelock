package com.black.spring.distributelock;

/**
 * Created by ASUS on 2016/8/16.
 */
public interface LockManager {

	Lock getLock(LockOperationContext context, String key);

}
