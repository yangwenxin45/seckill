package net.yangwenxin.seckill.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.yangwenxin.seckill.dao.GoodsDao;
import net.yangwenxin.seckill.domain.SeckillGoods;
import net.yangwenxin.seckill.vo.GoodsVo;

@Service
public class GoodsService {

	@Autowired
	private GoodsDao goodsDao;
	
	public List<GoodsVo> listGoodsVo() {
		return goodsDao.listGoodsVo();
	}
	
	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public boolean reduceStock(GoodsVo goods) {
		SeckillGoods g = new SeckillGoods();
		g.setGoodsId(goods.getId());
		int ret = goodsDao.reduceStock(g);
		return ret > 0;
	}

	public void resetStock(List<GoodsVo> goodsList) {
		for (GoodsVo goods : goodsList) {
			SeckillGoods g = new SeckillGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}
}
