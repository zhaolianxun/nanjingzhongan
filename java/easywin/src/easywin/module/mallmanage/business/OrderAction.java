package easywin.module.mallmanage.business;

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

	public static void refundComplete(String orderId, int refundAmount) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢主轮播图
			pst = connection.prepareStatement("select amount,refund_status from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			int amount = 0;
			String refundStatus = null;
			if (rs.next()) {
				amount = rs.getInt("amount");
				refundStatus = rs.getString("refund_status");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			if (amount != refundAmount)
				throw new InteractRuntimeException("退款金额有误");
			if (!refundStatus.equals("1"))
				throw new InteractRuntimeException("退款状态状态有误");

			pst = connection
					.prepareStatement("update t_mall_order set finished=1,refund_status=2,refund_time=? where id=?");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, orderId);
			int n = pst.executeUpdate();
			if (n != 1)
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

	public static void refundFail(String orderId, String reason) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢主轮播图
			pst = connection.prepareStatement("select refund_status from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			String refundStatus = null;
			if (rs.next()) {
				refundStatus = rs.getString("refund_status");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			if (!refundStatus.equals("1"))
				throw new InteractRuntimeException("退款状态状态有误");

			pst = connection
					.prepareStatement("update t_mall_order set refund_status=3,refund_fail_reason=? where id=?");
			pst.setObject(1, reason);
			pst.setObject(2, orderId);
			int n = pst.executeUpdate();
			if (n != 1)
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
