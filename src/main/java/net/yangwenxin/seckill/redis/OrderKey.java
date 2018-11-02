package net.yangwenxin.seckill.redis;

public class OrderKey extends BasePrefix {

	public OrderKey(String prefix) {
		super(prefix);
	}

	public static OrderKey getSeckillOrderByUidAndGid = new OrderKey("soug");
}
