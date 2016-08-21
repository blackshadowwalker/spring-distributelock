package com.black.spring.distributelock.component;

import com.black.spring.distributelock.annotation.DistributeLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by karl on 2016/8/21.
 */
@ComponentScan
public class MyService {
    private static Logger log = LoggerFactory.getLogger(MyService.class);
    private Map<Long, Integer> users = new ConcurrentHashMap<Long, Integer>();

    @DistributeLock(value = "updateUserStatus", key = "#userId", timeout = 5 * 1000)
    public Integer updateUserStatus(Long userId, Integer status, Integer sleepTime) throws Exception {
        for (int i=0; i<sleepTime; i ++) {
            this.sleep(1);
        }
        log.info("update user {} status to {}", userId, status);
        Integer oldStatus = users.get(userId);
        for (int i=0; i<sleepTime; i ++) {
            this.sleep(1);
        }
        users.put(userId, status);
        return oldStatus;
    }

    public void sleep(int ms) {
        try {
            Thread.sleep(ms * 1000);
        } catch (Exception e){ }
    }

    public void insert(Long userId, Integer status) {
        if (users.containsKey(userId)) {
            return ;
        }
        users.put(userId, status);
    }

    public Integer getUserStatus(Long userId) {
        return users.get(userId);
    }

}
