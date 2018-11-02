package net.yangwenxin.seckill.redis;

public interface KeyPrefix {

	public int expireSeconds();
	
	public String getPrefix();
}
