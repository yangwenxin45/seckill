package net.yangwenxin.seckill.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.redis.GoodsKey;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.result.Result;
import net.yangwenxin.seckill.service.GoodsService;
import net.yangwenxin.seckill.service.SeckillUserService;
import net.yangwenxin.seckill.vo.GoodsDetailVo;
import net.yangwenxin.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	private SeckillUserService seckillUserService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	private ApplicationContext applicationContext;
	
	private static Logger log = LoggerFactory.getLogger(GoodsController.class);
	
	/**
	 * QPS: 60
	 * 1000 * 10
	 * @param model
	 * @param user
	 * @return
	 */
	@RequestMapping(value="/to_list", produces="text/html")
	@ResponseBody
	public String toList(Model model,
			HttpServletRequest request,
			HttpServletResponse response,
//			@CookieValue(value=SeckillUserService.COOKIE_NAME_TOKEN, required=false) String cookieToken,
//			@RequestParam(value=SeckillUserService.COOKIE_NAME_TOKEN, required=false) String paramToken) {
			SeckillUser user) {
//		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//			return "login";
//		}
//		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//		SeckillUser user = seckillUserService.getByToken(response, token);
		model.addAttribute("user", user);
		// 取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}
		// 查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
//		return "goods_list";
		// 手动渲染
		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if (!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping(value="/to_detail2/{goodsId}", produces="text/html")
	@ResponseBody
	public String detail2(Model model, SeckillUser user,
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("goodsId") long goodsId) {
		// snowflake
		model.addAttribute("user", user);
		
		// 取缓存
    	String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
    	if(!StringUtils.isEmpty(html)) {
    		return html;
    	}
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int seckillStatus = 0;
		int remainSeconds = 0;
		
		if (now < startAt) {	// 秒杀还没开始，倒计时
			seckillStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		} else if (now > endAt) {	// 秒杀已经结束
			seckillStatus = 2;
			remainSeconds = -1;
		} else {	// 秒杀进行中
			seckillStatus = 1;
			remainSeconds = 0;
		}
		
		model.addAttribute("seckillStatus", seckillStatus);
		model.addAttribute("remainSeconds", remainSeconds);
//		return "goods_detail";
		// 手动渲染
		SpringWebContext ctx = new SpringWebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
    	html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
    	}
    	return html;
	}
	
	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(SeckillUser user,
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("goodsId") long goodsId) {
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int seckillStatus = 0;
		int remainSeconds = 0;
		
		if (now < startAt) {	// 秒杀还没开始，倒计时
			seckillStatus = 0;
			remainSeconds = (int) ((startAt - now) / 1000);
		} else if (now > endAt) {	// 秒杀已经结束
			seckillStatus = 2;
			remainSeconds = -1;
		} else {	// 秒杀进行中
			seckillStatus = 1;
			remainSeconds = 0;
		}
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setUser(user);
		vo.setRemainSeconds(remainSeconds);
		vo.setSeckillStatus(seckillStatus);
    	return Result.success(vo);
	}
}
