package net.yangwenxin.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.yangwenxin.seckill.domain.SeckillOrder;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.redis.RedisService;
import net.yangwenxin.seckill.service.GoodsService;
import net.yangwenxin.seckill.service.OrderService;
import net.yangwenxin.seckill.service.SeckillService;
import net.yangwenxin.seckill.vo.GoodsVo;

@Service
public class MQReceiver {
	
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private SeckillService seckillService;

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	
	@RabbitListener(queues=MQConfig.SECKILL_QUEUE)
	public void receiveSeckillMessage(String message) {
		log.info("receive message " + message);
		SeckillMessage mm = RedisService.stringToBean(message, SeckillMessage.class);
		SeckillUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return;
    	}
    	//判断是否已经秒杀到了
    	SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return;
    	}
    	//减库存 下订单 写入秒杀订单
    	seckillService.seckill(user, goods);
	}
	
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message) {
		log.info("receive message " + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info("topic queue1 message " + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info("topic queue2 message " + message);
	}
	
	@RabbitListener(queues=MQConfig.HEADERS_QUEUE)
	public void receiveHeadersQueue(byte[] message) {
		log.info("headers queue message " + new String(message));
	}
}
