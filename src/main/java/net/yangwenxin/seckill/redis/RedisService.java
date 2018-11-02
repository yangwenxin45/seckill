package net.yangwenxin.seckill.redis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

@Service
public class RedisService {

	@Autowired
	private JedisPool jedisPool;

	/**
	 * 获取值
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
		try (Jedis jedis = jedisPool.getResource()) {
			String realKey = prefix.getPrefix() + key;
			String str = jedis.get(realKey);
			T t = stringToBean(str, clazz);
			return t;
		}
	}

	/**
	 * 设置值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public <T> boolean set(KeyPrefix prefix, String key, T value) {
		try (Jedis jedis = jedisPool.getResource()) {
			String str = beanToString(value);
			if (str == null) {
				return false;
			}
			String realKey = prefix.getPrefix() + key;
			int seconds = prefix.expireSeconds();
			if (seconds <= 0) {
				jedis.set(realKey, str);
			} else {
				jedis.setex(realKey, seconds, str);
			}
			return true;
		}
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(KeyPrefix prefix, String key) {
		try (Jedis jedis = jedisPool.getResource()) {
			String realKey  = prefix.getPrefix() + key;
			return jedis.exists(realKey);
		}
	}
	
	/**
	 * 删除
	 * @param prefix
	 * @param key
	 * @return
	 */
	public boolean delete(KeyPrefix prefix, String key) {
		try (Jedis jedis = jedisPool.getResource()) {
			String realKey  = prefix.getPrefix() + key;
			return jedis.del(realKey) > 0;
		}
	}

	/**
	 * 增加值
	 * 
	 * @param key
	 * @return
	 */
	public Long incr(KeyPrefix prefix, String key) {
		try (Jedis jedis = jedisPool.getResource()) {
			String realKey  = prefix.getPrefix() + key;
			return jedis.incr(realKey);
		}
	}

	/**
	 * 减少值
	 * 
	 * @param key
	 * @return
	 */
	public Long decr(KeyPrefix prefix, String key) {
		try (Jedis jedis = jedisPool.getResource()) {
			String realKey  = prefix.getPrefix() + key;
			return jedis.decr(realKey);
		}
	}

	public static <T> String beanToString(T value) {
		// if (value == null) {
		// return null;
		// }
		// Class<?> clazz = value.getClass();
		// if (clazz == int.class || clazz == Integer.class) {
		// return "" + value;
		// } else if (clazz == String.class) {
		// return (String) value;
		// } else if (clazz == long.class || clazz == Long.class) {
		// return "" + value;
		// } else {
		// return JSON.toJSONString(value);
		// }
		return JSON.toJSONString(value);
	}

	public static <T> T stringToBean(String str, Class<T> clazz) {
		// if (str == null || str.length() <= 0 || clazz == null) {
		// return null;
		// }
		// if (clazz == int.class || clazz == Integer.class) {
		// return (T) Integer.valueOf(str);
		// } else if (clazz == String.class) {
		// return (T) str;
		// } else if (clazz == long.class || clazz == Long.class) {
		// return (T) Long.valueOf(str);
		// } else {
		// return JSON.toJavaObject(JSON.parseObject(str), clazz);
		// }
		if (clazz == null) {
			return null;
		} else {
			return JSON.parseObject(str, clazz);
		}
	}

	
	public boolean delete(KeyPrefix prefix) {
		if(prefix == null) {
			return false;
		}
		List<String> keys = scanKeys(prefix.getPrefix());
		if(keys==null || keys.size() <= 0) {
			return true;
		}
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(keys.toArray(new String[0]));
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(jedis != null) {
				jedis.close();
			}
		}
	}
	
	public List<String> scanKeys(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			List<String> keys = new ArrayList<String>();
			String cursor = "0";
			ScanParams sp = new ScanParams();
			sp.match("*"+key+"*");
			sp.count(100);
			do{
				ScanResult<String> ret = jedis.scan(cursor, sp);
				List<String> result = ret.getResult();
				if(result!=null && result.size() > 0){
					keys.addAll(result);
				}
				//再处理cursor
				cursor = ret.getStringCursor();
			}while(!cursor.equals("0"));
			return keys;
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
}
