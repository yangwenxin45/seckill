package net.yangwenxin.seckill.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.yangwenxin.seckill.access.AccessLimit;
import net.yangwenxin.seckill.domain.OrderInfo;
import net.yangwenxin.seckill.domain.SeckillOrder;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.rabbitmq.MQSender;
import net.yangwenxin.seckill.rabbitmq.SeckillMessage;
import net.yangwenxin.seckill.redis.AccessKey;
import net.yangwenxin.seckill.redis.GoodsKey;
import net.yangwenxin.seckill.redis.OrderKey;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.redis.SeckillKey;
import net.yangwenxin.seckill.result.CodeMsg;
import net.yangwenxin.seckill.result.Result;
import net.yangwenxin.seckill.service.GoodsService;
import net.yangwenxin.seckill.service.OrderService;
import net.yangwenxin.seckill.service.SeckillService;
import net.yangwenxin.seckill.util.MD5Util;
import net.yangwenxin.seckill.util.UUIDUtil;
import net.yangwenxin.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

	@Autowired
	private GoodsService goodsService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private SeckillService seckillService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private MQSender sender;
	
	private Map<Long, Boolean> localOverMap = new HashMap<>();
	
	/**
	 * QPS: 73
	 * 1000 * 10
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/{path}/do_seckill", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> seckill(Model model, SeckillUser user,
			@RequestParam("goodsId") long goodsId,
			@PathVariable("path") String path) {
		model.addAttribute("user", user);
		if (user == null) {
//			return "login";
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		// 验证path
		boolean check = seckillService.checkPath(user, goodsId, path);
		if (!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		// 内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
		if (over) {
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		// 预减库存
		long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
		if (stock < 0) {
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		// 判断是否重复秒杀
		SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if (order != null) {
			return Result.error(CodeMsg.REPEATE_SECKILL);
		}
		// 入队
		SeckillMessage mm = new SeckillMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendSeckillMessage(mm);
		return Result.success(0);
		
		/*
		// 是否库存不足
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if (stock <= 0) {
//			model.addAttribute("errmsg", CodeMsg.SECKILL_OVER.getMsg());
//			return "seckill_fail";
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		// 是否重复下单
		SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if (order != null) {
//			model.addAttribute("errmsg", CodeMsg.REPEATE_SECKILL.getMsg());
//			return "seckill_fail";
			return Result.error(CodeMsg.REPEATE_SECKILL);
		}
		// 减库存	下订单	写入秒杀订单
		OrderInfo orderInfo = seckillService.seckill(user, goods);
//		model.addAttribute("orderInfo", orderInfo);
//		model.addAttribute("goods", goods);
//		return "order_detail";
		return Result.success(orderInfo);
		*/
	}

	/**
	 * 系统初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if (goodsList == null) {
			return;
		}
		for (GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
	
	/**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result  = seckillService.getSeckillResult(user.getId(), goodsId);
    	return Result.success(result);
    }
    
    /**
     * 还原默认值
     */
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	for (GoodsVo goods : goodsList) {
    		goods.setStockCount(10);
    		redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), 10);	// 还原库存数量
    		localOverMap.put(goods.getId(), false);	// 还原内存标记
    	}
    	redisService.delete(OrderKey.getSeckillOrderByUidAndGid);	// 还原是否重复秒杀
    	redisService.delete(SeckillKey.isGoodsOver);	// 还原商品是否秒杀完了
    	seckillService.reset(goodsList);	// 还原数据库
    	return Result.success(true);
    }
    
    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request,
    		SeckillUser user,
    		@RequestParam("goodsId") long goodsId,
    		@RequestParam(value="verifyCode") int verifyCode) {
//    		@RequestParam(value="verifyCode", defaultValue="0") int verifyCode) {
    	// TODO 测试需要
    	if (user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
//    	// 查询访问次数
//    	String uri = request.getRequestURI();
//    	String key = uri + "_" + user.getId();
//    	Integer count = redisService.get(AccessKey.access, key, Integer.class);
//    	if (count == null) {
//    		redisService.set(AccessKey.access, key, 1);
//    	} else if (count < 5) {
//    		redisService.incr(AccessKey.access, key);
//    	} else {
//    		return Result.error(CodeMsg.ACCESS_LIMIT);
//    	}
    	boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	String path = seckillService.createSeckillPath(user, goodsId);
    	return Result.success(path);
    }
    
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response,
    		SeckillUser user, @RequestParam("goodsId") long goodsId) {
    	if (user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image = seckillService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.SECKILL_FAIL);
    	}
    }
}
