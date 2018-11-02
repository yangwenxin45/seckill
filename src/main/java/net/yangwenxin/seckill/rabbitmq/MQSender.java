package net.yangwenxin.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.yangwenxin.seckill.redis.RedisService;

@Service
public class MQSender {

	private static Logger log = LoggerFactory.getLogger(MQSender.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void sendSeckillMessage(SeckillMessage message) {
		String msg = RedisService.beanToString(message);
		log.info("send message " + msg);
		rabbitTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, msg);
	}
	
	public void send(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send message " + msg);
		rabbitTemplate.convertAndSend(MQConfig.QUEUE, msg);
	}
	
	public void sendTopic(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send topic message " + msg);
		rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg + "1");
		rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg + "2");
	}
	
	public void sendFanout(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send fanout message " + msg);
		rabbitTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
	}
	
	public void sendHeader(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send header message" + msg);
		MessageProperties properties = new MessageProperties();
		properties.setHeader("header1", "value1");
		properties.setHeader("header2", "value2");
		Message obj = new Message(msg.getBytes(), properties);
		rabbitTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
	}
}
