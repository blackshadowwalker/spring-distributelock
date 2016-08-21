package com.black.spring.distributelock.redis;

import com.black.spring.distributelock.Lock;
import com.black.spring.distributelock.LockException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;


/**
 * Created by ASUS on 2016/8/16.
 */
public class RedisLock implements Lock {
	private static Log log = LogFactory.getLog(RedisLock.class);

	private StringRedisTemplate stringRedisTemplate;

	private final String name;
	private final String key;
	private final long timeout;
	private final String msg;

	private final String lockName;

	public RedisLock(String name, String key, long timeout, String msg, StringRedisTemplate redisTemplate,
			RedisLockExecutorService executorService) {
		this.name = name;
		this.key = key;
		this.timeout = timeout;
		this.lockName = this.name + ":" + key;
		this.stringRedisTemplate = redisTemplate;
		this.msg = msg;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public String getLockName() {
		return this.lockName;
	}

	@Override
	public void lock() throws LockException {
		if (StringUtils.isEmpty(name)) {
			return;
		}
        this.tryLock();
		//has get the lock
		if (!locked) {
			throw new LockException(msg != null ? msg : "请勿重复提交");
		}
        log.info("Get Lock: " + this.lockName);
	}

	private volatile boolean cancel = false;

	private volatile boolean locked = false;
	long MAX_TIMEOUT = 1000 * 3600 * 2;

	private void tryLock() {
		BoundValueOperations operations = stringRedisTemplate.boundValueOps(lockName);
		long st = System.currentTimeMillis();
		while (! locked) {
			if (cancel) {
				break;
			}
			long timestamp = System.currentTimeMillis();
			long expireTime = (timeout < 1) ? Long.MAX_VALUE : (timestamp + timeout);
			long oldValue = get(operations);
			if (oldValue > 0 && timestamp > oldValue) {
				stringRedisTemplate.delete(lockName);
			}

			locked = operations.setIfAbsent(String.valueOf(expireTime));
			if (locked) {
				operations.expire(timeout, TimeUnit.MILLISECONDS);
			}
			try {
				Long ttl = operations.getExpire();
				if (ttl != null && ttl > 1000) {
					Thread.sleep(500);
				} else if (timeout > 0) {
					Thread.sleep(10);
				} else {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
			}
			if (timeout > 0 && timestamp - st > timeout) {
				break;
			}
			if (timestamp - st > MAX_TIMEOUT) {
				break;
			}
		}
	}

	@Override
	public void unlock() {
		if (locked) {
			stringRedisTemplate.delete(lockName);
			locked = false;
			cancel = false;
		}
	}

	private long get(BoundValueOperations valueOperations) {
		String value = (String) valueOperations.get();
		if (StringUtils.isEmpty(value)) {
			return 0;
		}
		return Long.parseLong(value);
	}

}
