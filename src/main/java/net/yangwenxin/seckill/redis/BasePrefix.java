package net.yangwenxin.seckill.redis;

public abstract class BasePrefix implements KeyPrefix {

	/**
	 * 0代表永不过期
	 */
	private int expireSeconds;
	private String prefix;

	public BasePrefix(String prefix) {
		this(0, prefix);
	}

	public BasePrefix(int expireSeconds, String prefix) {
		super();
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}

	@Override
	public int expireSeconds() {
		return expireSeconds;
	}

	@Override
	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className + ":" + prefix;
	}

}
