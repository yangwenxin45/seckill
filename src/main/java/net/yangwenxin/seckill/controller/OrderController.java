package net.yangwenxin.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.yangwenxin.seckill.domain.OrderInfo;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.result.CodeMsg;
import net.yangwenxin.seckill.result.Result;
import net.yangwenxin.seckill.service.GoodsService;
import net.yangwenxin.seckill.service.OrderService;
import net.yangwenxin.seckill.vo.GoodsVo;
import net.yangwenxin.seckill.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;
	@Autowired
	private GoodsService goodsService;
	
	@RequestMapping("/detail")
	@ResponseBody
	public Result<OrderDetailVo> info(Model model, SeckillUser user,
			@RequestParam("orderId") long orderId) {
		if (user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		OrderInfo order = orderService.getOrderById(orderId);
		if (order == null) {
			return Result.error(CodeMsg.ORDER_NOT_EXIST);
		}
		long goodsId = order.getGoodsId();
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		OrderDetailVo vo = new OrderDetailVo();
		vo.setOrder(order);
		vo.setGoods(goods);
		return Result.success(vo);
	}
}
