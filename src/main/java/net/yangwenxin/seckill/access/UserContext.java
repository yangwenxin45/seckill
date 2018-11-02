package net.yangwenxin.seckill.access;

import net.yangwenxin.seckill.domain.SeckillUser;

public class UserContext {

	private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<>();
	
	public static void setUser(SeckillUser user) {
		userHolder.set(user);
	}
	
	public static SeckillUser getUser() {
		return userHolder.get();
	}
}
