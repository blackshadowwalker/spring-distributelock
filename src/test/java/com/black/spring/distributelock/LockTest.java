package com.black.spring.distributelock;

import com.black.spring.distributelock.component.MyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextHierarchy({
    @ContextConfiguration(name = "parent", locations = {"classpath:spring-lock.xml" }),
})
public class LockTest extends AbstractJUnit4SpringContextTests {
    private static Logger log = LoggerFactory.getLogger(LockTest.class);

    @Autowired
    MyService myService;

    final CountDownLatch latch = new CountDownLatch(1);
    final java.util.concurrent.locks.Lock lock = new ReentrantLock();
    final Condition next = lock.newCondition();

    @Test
    public void testLock() throws Exception {
        final Long userId = 1L;
        myService.insert(userId, 1);
       final Thread thread = new Thread(){
            @Override
            public void run() {
                lock.lock();
                try {
                    log.info("start updateUserStatus to 2");
                    next.signal();
                    myService.updateUserStatus(userId, 2, 11);
                    log.info("after update 2");
                    latch.countDown();
                } catch (Exception e) {
                    log.error("", e);
                }finally {
                    lock.unlock();
                }
            }
        };

        lock.lock();
        try {
            thread.start();
            log.info("waiting next signal");
            next.await();
            log.info("start updateUserStatus to 3");
            myService.updateUserStatus(userId, 3, 0);
            log.info("after update 3");
            latch.await();
        }finally {
            lock.unlock();
        }
    }

}
