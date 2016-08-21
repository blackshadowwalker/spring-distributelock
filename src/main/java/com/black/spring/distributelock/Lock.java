package com.black.spring.distributelock;

/**
 * Created by ASUS on 2016/8/16.
 */
public interface Lock {

	String getName();

	String getKey();

	long getTimeout();

	String getLockName();

	void lock() throws LockException;

	void unlock();

}
