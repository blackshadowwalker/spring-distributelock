package com.black.spring.distributelock.interceptor;

/**
 * Created by ASUS on 2016/8/16.
 */
public class LockOperation {

	String name;
	String key;
	long timeout;//seconds
	long expire;//seconds
	String msg;

	public LockOperation() {
	}

	public LockOperation(String name, String key, long timeout, long expire, String msg) {
		this.name = name;
		this.key = key;
		this.timeout = timeout;
		this.expire = expire;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		LockOperation operation = (LockOperation) o;

		if (timeout != operation.timeout)
			return false;
		if (name != null ? !name.equals(operation.name) : operation.name != null)
			return false;
		if (key != null ? !key.equals(operation.key) : operation.key != null)
			return false;
		return msg != null ? msg.equals(operation.msg) : operation.msg == null;

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (key != null ? key.hashCode() : 0);
		result = 31 * result + (int) (timeout ^ (timeout >>> 32));
		result = 31 * result + (msg != null ? msg.hashCode() : 0);
		return result;
	}
}
