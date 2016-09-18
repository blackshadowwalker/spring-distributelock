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

	@DistributeLock(value = "updateUserStatus", key = "#userId", timeout = 10, expire = 60, errMsg = "更新失败，请刷新重试")
	public Integer updateUserStatus(Long userId, Integer status) throws Exception {
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
