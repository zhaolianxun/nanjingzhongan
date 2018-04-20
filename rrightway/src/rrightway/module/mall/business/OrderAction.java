package rrightway.module.mall.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import rrightway.entity.InteractRuntimeException;
import rrightway.util.RrightwayDataSource;

public class OrderAction {

	public static Logger logger = Logger.getLogger(OrderAction.class);

	public static void payComplete(String orderId, String payerId, int payAmount, String payType) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = RrightwayDataSource.dataSource.getConnection();
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
