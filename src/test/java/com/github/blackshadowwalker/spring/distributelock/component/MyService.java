package com.github.blackshadowwalker.spring.distributelock.component;

import com.github.blackshadowwalker.spring.distributelock.annotation.DistributeLock;
import com.github.blackshadowwalker.spring.distributelock.annotation.DistributeLocks;
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

    @DistributeLock(value = "updateUserStatus", key = "#userId", timeout = 10, expire = 60, errorCode = "400", errMsg = "更新失败，请刷新重试")
    public Integer updateUserStatus(Long userId, Integer status) throws Exception {
        log.info("update user {} status to {}", userId, status);
        Integer oldStatus = users.get(userId);
        users.put(userId, status);
        return oldStatus;
    }

    @DistributeLocks({
            @DistributeLock(value = "KEY1", key = "#userId", timeout = 10, expire = 60, errorCode = "401", errMsg = "更新失败，请刷新重试"),
            @DistributeLock(value = "KEY2", key = "#status", timeout = 10, expire = 60, errorCode = "402", errMsg = "更新失败，请刷新重试")
    })
    public Integer updateUserStatus2(Long userId, Integer status) throws Exception {
        log.info("update user {} status to {}", userId, status);
        Integer oldStatus = users.get(userId);
        users.put(userId, status);
        return oldStatus;
    }

    public void insert(Long userId, Integer status) {
        if (users.containsKey(userId)) {
            return;
        }
        users.put(userId, status);
    }

    public Integer getUserStatus(Long userId) {
        return users.get(userId);
    }

}
