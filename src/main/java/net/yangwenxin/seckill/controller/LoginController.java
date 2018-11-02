package net.yangwenxin.seckill.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.yangwenxin.seckill.result.CodeMsg;
import net.yangwenxin.seckill.result.Result;
import net.yangwenxin.seckill.service.SeckillService;
import net.yangwenxin.seckill.service.SeckillUserService;
import net.yangwenxin.seckill.service.UserService;
import net.yangwenxin.seckill.util.ValidatorUtil;
import net.yangwenxin.seckill.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {

	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private SeckillUserService seckillUserService;
	
	@RequestMapping("/to_login")
	public String toLogin() {
		return "login";
	}
	
	@RequestMapping("/do_login")
	@ResponseBody
	public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
		log.info(loginVo.toString());
		// 参数校验
//		String passInput = loginVo.getPassword();
//		String mobile = loginVo.getMobile();
//		if (StringUtils.isEmpty(passInput)) {
//			return Result.error(CodeMsg.PASSWORD_EMPTY);
//		}
//		if (StringUtils.isEmpty(mobile)) {
//			return Result.error(CodeMsg.MOBILE_EMPTY);
//		}
//		if (!ValidatorUtil.isMobile(mobile)) {
//			return Result.error(CodeMsg.MOBILE_ERROR);
//		}
		// 登录
//		CodeMsg cm = seckillUserService.login(loginVo);
//		if (cm.getCode() == 0) {
//			return Result.success(true);
//		} else {
//			return Result.error(cm);
//		}
		String token = seckillUserService.login(response, loginVo);
		return Result.success(token);
	}
}
