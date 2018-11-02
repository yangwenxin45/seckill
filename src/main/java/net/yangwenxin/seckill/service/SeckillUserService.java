package net.yangwenxin.seckill.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.yangwenxin.seckill.dao.SeckillUserDao;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.exception.GlobalException;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.redis.SeckillUserKey;
import net.yangwenxin.seckill.result.CodeMsg;
import net.yangwenxin.seckill.util.MD5Util;
import net.yangwenxin.seckill.util.UUIDUtil;
import net.yangwenxin.seckill.vo.LoginVo;

@Service
public class SeckillUserService {

	public static final String COOKIE_NAME_TOKEN = "token";
	
	@Autowired
	private SeckillUserDao seckillUserDao;
	@Autowired
	private RedisService redisService;
	
	public SeckillUser getById(Long id) {
		// 取缓存
		SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
		if (user != null) {
			return user;
		}
		// 取数据库
		user = seckillUserDao.getById(id);
		if (user != null) {
			redisService.set(SeckillUserKey.getById, ""+id, user);
		}
		return user;
	}
	
	public boolean updatePassword(String token, long id, String formPass) {
		// 取user
		SeckillUser user = getById(id);
		if (user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		// 更新数据库
		SeckillUser toBeUpdate = new SeckillUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDbPass(formPass, user.getSalt()));
		seckillUserDao.update(toBeUpdate);
		// 处理缓存
		redisService.delete(SeckillUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(SeckillUserKey.token, token, user);
		return true;
	}

	public String login(HttpServletResponse response, LoginVo loginVo) {
		if (loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		// 判断手机号是否存在
		SeckillUser user = getById(Long.valueOf(mobile));
		if (user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		// 验证密码
		String dbPass = user.getPassword();
		String saltDb = user.getSalt();
		String calcPass = MD5Util.formPassToDbPass(formPass, saltDb);
		if (!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		// 生成cookie
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return token;
	}

	private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
//		String token = UUIDUtil.uuid();
		redisService.set(SeckillUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public SeckillUser getByToken(HttpServletResponse response, String token) {
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
		// 延长有效期
		if (user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
}
