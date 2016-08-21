package com.black.spring.distributelock.redis;

import com.black.spring.distributelock.Lock;
import com.black.spring.distributelock.LockManager;
import com.black.spring.distributelock.LockOperation;
import com.black.spring.distributelock.LockOperationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS on 2016/8/16.
 */
public class RedisLockManager implements LockManager {

	private StringRedisTemplate redisTemplate;
	private RedisLockExecutorService executorService = new RedisLockExecutorService();

	public RedisLockManager(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Lock getLock(LockOperationContext context, String key) {
		LockOperation operation = context.getMetadata().getOperation();
		String name = operation.getName();
		long timeout = operation.getTimeout();
		RedisLock lock = lockCache.get(operation);
		if (lock == null) {
			lock = new RedisLock(name, key, timeout, operation.getMsg(), redisTemplate, executorService);
			lockCache.put(new LockOperation(operation.getName(), operation.getKey(), operation.getTimeout(), operation.getMsg()), lock);
		}
		return lock;
	}

	private Map<LockOperation, RedisLock> lockCache = new HashMap<LockOperation, RedisLock>();

}
