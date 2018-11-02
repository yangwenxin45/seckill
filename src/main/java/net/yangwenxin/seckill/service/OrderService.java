package net.yangwenxin.seckill.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.yangwenxin.seckill.dao.OrderDao;
import net.yangwenxin.seckill.domain.OrderInfo;
import net.yangwenxin.seckill.domain.SeckillOrder;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.redis.OrderKey;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.vo.GoodsVo;

@Service
public class OrderService {

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private RedisService redisService;
	
	public SeckillOrder getSeckillOrderByUserIdAndGoodsId(Long userId, long goodsId) {
//		return orderDao.getSeckillOrderByUserIdAndGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getSeckillOrderByUidAndGid, ""+userId+"_"+goodsId, SeckillOrder.class);
	}

	@Transactional
	public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getSeckillPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(user.getId());
		orderDao.insert(orderInfo);
		
		SeckillOrder seckillOrder = new SeckillOrder();
		seckillOrder.setGoodsId(goods.getId());
		seckillOrder.setOrderId(orderInfo.getId());
		seckillOrder.setUserId(user.getId());
		orderDao.insertSeckillOrder(seckillOrder);
		
		redisService.set(OrderKey.getSeckillOrderByUidAndGid, ""+user.getId()+"_"+goods.getId(), seckillOrder);
		 
		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}

	public void deleteOrders() {
		orderDao.deleteOrders();
		orderDao.deleteSeckillOrders();
	}

}
