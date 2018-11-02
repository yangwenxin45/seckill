package net.yangwenxin.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.result.Result;

@Controller
@RequestMapping("/user")
public class UserController {

	@RequestMapping("/info")
	@ResponseBody
	public Result<SeckillUser> info(Model model, SeckillUser user) {
		return Result.success(user);
	}
}
