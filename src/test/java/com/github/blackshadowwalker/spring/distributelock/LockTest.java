package com.github.blackshadowwalker.spring.distributelock;

import com.github.blackshadowwalker.spring.distributelock.component.MyService;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({@ContextConfiguration(name = "parent", locations = {"classpath:spring-lock.xml"}),})
public class
LockTest extends AbstractJUnit4SpringContextTests {
    private static Logger log = LoggerFactory.getLogger(LockTest.class);

    @Autowired
    MyService myService;

    @Autowired
    RedisTemplate redis;

    @Value("${lock.prefix}")
    String lockPrefix;

    @Test(expected = LockException.class)
    public void testLock() throws Exception {
        final Long userId = 1L;
        myService.insert(userId, 1);

        String key = lockPrefix + "updateUserStatus:" + userId;
        String value = (System.currentTimeMillis() + 1000 * 60) + "";
        redis.opsForValue().set(key, value);

        try {
            myService.updateUserStatus(userId, 3);
        } catch (LockException e) {
            log.warn("ERROR:{}-{}", e.getCode(), e.getMessage());
            throw e;
        } finally {
            redis.delete(key);
        }
    }

    @Test
    public void testLocks() throws Exception {
        final Long userId = 1L;
        int status = 5;
        myService.insert(userId, 1);

        String key1 = lockPrefix + "KEY1:" + userId;
        String key2 = lockPrefix + "KEY2:" + status;
        //clear
        redis.delete(key1);
        redis.delete(key2);

        String value = (System.currentTimeMillis() + 1000 * 60) + "";
        redis.opsForValue().set(key1, value);

        LockException lastError = null;
        try {
            myService.updateUserStatus2(userId, status);
        } catch (LockException e) {
            log.warn("ERROR:{}-{}", e.getCode(), e.getMessage());
            lastError = e;
        } finally {
            redis.delete(key1);
        }
        Assert.assertNotNull(lastError);
        Assert.assertEquals("401", lastError.getCode());

        redis.opsForValue().set(key2, value);
        try {
            myService.updateUserStatus2(userId, status);
        } catch (LockException e) {
            log.warn("ERROR:{}-{}", e.getCode(), e.getMessage());
            lastError = e;
        } finally {
            redis.delete(key2);
        }
        Assert.assertNotNull(lastError);
        Assert.assertEquals("402", lastError.getCode());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testThreadSafeDeleteKey() {
        String key = "testThreadSafeDelete";
        String newKey = "DEL_" + key;
        redis.delete(key);
        redis.delete(newKey);

        redis.opsForValue().setIfAbsent(key, String.valueOf(System.currentTimeMillis()));
        redis.expire(key, 10, TimeUnit.MINUTES);

        Boolean renameRet = redis.renameIfAbsent(key, newKey);
        Assert.assertTrue(renameRet);

        Long ttl = redis.getExpire(newKey);
        log.info("newKey:{} ttl:{}", newKey, ttl);
        Assert.assertNotNull("ttl not null", ttl);
        Assert.assertTrue("ttl > 0", ttl > 0);

        renameRet = redis.renameIfAbsent(key, newKey);//InvalidDataAccessApiUsageException: ERR no such key; nested exception is redis.clients.jedis.exceptions.JedisDataException: ERR no such key
        Assert.assertFalse(renameRet);
        redis.delete(newKey);

        renameRet = redis.renameIfAbsent(key, newKey);
        Assert.assertFalse(renameRet);
    }

}
