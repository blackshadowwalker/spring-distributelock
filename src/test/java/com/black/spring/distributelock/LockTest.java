package com.black.spring.distributelock;

import com.black.spring.distributelock.component.MyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by karl on 2016/8/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({ @ContextConfiguration(name = "parent", locations = { "classpath:spring-lock.xml" }), })
public class LockTest extends AbstractJUnit4SpringContextTests {
	private static Logger log = LoggerFactory.getLogger(LockTest.class);

	@Autowired
	MyService myService;

	@Autowired
	StringRedisTemplate redis;

	@Test(expected = LockException.class)
	public void testLock() throws Exception {
		final Long userId = 1L;
		myService.insert(userId, 1);

		String key = "Lock_updateUserStatus:1";
		String value = (System.currentTimeMillis() + 1000 * 60) + "";
		redis.opsForValue().set(key, value);

		try {
			myService.updateUserStatus(userId, 3);
		} finally {
			redis.delete(key);
		}
	}

}
