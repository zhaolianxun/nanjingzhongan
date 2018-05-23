package easywin.module.mall.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import easywin.entity.InteractRuntimeException;
import easywin.util.EasywinDataSource;

public class OrderAction {

	public static Logger logger = Logger.getLogger(OrderAction.class);

	public static void payComplete(String orderId, String payerId, int payAmount, String payType) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢主轮播图
			pst = connection.prepareStatement("select amount,status from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			int amount = 0;
			String status = null;
			if (rs.next()) {
				amount = rs.getInt("amount");
				status = rs.getString("status");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			if (amount != payAmount)
				throw new InteractRuntimeException("支付金额有误");
			if (!status.equals("0"))
				throw new InteractRuntimeException("订单状态有误");

			pst = connection.prepareStatement("update t_mall_order set status=1,pay_type=?,pay_time=? where id=?");
			pst.setObject(1, payType);
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, orderId);
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			//更新销量和购买人数
			pst = connection.prepareStatement(
					"update t_mall_good t left join t_mall_order_detail od on od.good_id=t.id set t.saled_count=t.saled_count+od.count,t.buyer_count=t.buyer_count+1 where od.order_id=?");
			pst.setObject(1, orderId);
			n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			throw e;
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

}
