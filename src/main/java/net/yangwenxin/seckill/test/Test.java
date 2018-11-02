package net.yangwenxin.seckill.test;

import redis.clients.jedis.Jedis;

public class Test {

	public static void main(String[] args) {
		Jedis jedis = new Jedis("47.94.144.168", 6379);
		jedis.auth("123456");
		jedis.set("foo", "bar");
		String value = jedis.get("foo");
		System.out.println(value);
	}
}
