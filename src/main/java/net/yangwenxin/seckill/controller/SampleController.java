package net.yangwenxin.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.yangwenxin.seckill.domain.User;
import net.yangwenxin.seckill.rabbitmq.MQSender;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.redis.UserKey;
import net.yangwenxin.seckill.result.CodeMsg;
import net.yangwenxin.seckill.result.Result;
import net.yangwenxin.seckill.service.UserService;

@Controller
@RequestMapping("/demo")
public class SampleController {

	@Autowired
	private UserService userService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private MQSender sender;
	
	@RequestMapping("/mq/header")
	@ResponseBody
	public Result<String> header() {
		sender.sendHeader("rabbitmq");
		return Result.success("Hello World");
	}
	
	// swagger
	@RequestMapping("/mq/fanout")
	@ResponseBody
	public Result<String> fanout() {
		sender.sendFanout("rabbitmq");
		return Result.success("Hello World");
	}
	
	@RequestMapping("/mq/topic")
	@ResponseBody
	public Result<String> topic() {
		sender.sendTopic("rabbitmq");
		return Result.success("Hello World");
	}
	
	@RequestMapping("/mq")
	@ResponseBody
	public Result<String> mq() {
		sender.send("rabbitmq");
		return Result.success("Hello World");
	}
	
	@RequestMapping("/")
	@ResponseBody
	public String home() {
		return "Hello World!";
	}
	
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello() {
//		Result.success(data);
//		return new Result(0, "success", "hello,imooc");
		return Result.success("hello,imooc");
	}
	
	@RequestMapping("/helloError")
	@ResponseBody
	public Result<String> helloError() {
		return Result.error(CodeMsg.SERVER_ERROR);
//		Result.error(CodeMsg);
//		return new Result(500100, "session失效");
//		return new Result(500101, "XXX");
//		return new Result(500102, "XXX");
	}
	
	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model model) {
		model.addAttribute("name", "yang");
		return "hello";
	}
	
	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User> doGet() {
		User user = userService.getById(1);
		return Result.success(user);
	}
	
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx() {
		userService.tx();
		return Result.success(true);
	}
	
	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> redisGet() {
		User user = redisService.get(UserKey.getById, "" + 1, User.class);
		return Result.success(user);
	}
	
	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet() {
		User user = new User();
		user.setId(1);
		user.setName("1111");
		redisService.set(UserKey.getById, "" + 1, user);
		return Result.success(true);
	}
}
