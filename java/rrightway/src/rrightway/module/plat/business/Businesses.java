package rrightway.module.plat.business;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import rrightway.constant.SysParam;
import rrightway.entity.InteractRuntimeException;
import rrightway.util.RrightwayDataSource;

public class Businesses {
	private static Logger logger = Logger.getLogger(Businesses.class);

	public static BigDecimal computeWalletOutable(String userId) throws Exception {
		Connection connection = null;
		try {
			connection = RrightwayDataSource.dataSource.getConnection();
			return computeWalletOutable(userId, connection);
		} catch (Exception e) {
			throw e;
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	public static BigDecimal computeWalletOutable(String userId, Connection connection) throws Exception {
		PreparedStatement pst = null;
		try {
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select right_wallet_unoutable,right_wallet_outable from t_user where id=? for update").toString());
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			BigDecimal rightWalletOutable = null;
			BigDecimal rightWalletUnoutable = null;
			if (rs.next()) {
				rightWalletOutable = rs.getBigDecimal("right_wallet_outable");
				rightWalletUnoutable = rs.getBigDecimal("right_wallet_unoutable");
			} else
				throw new InteractRuntimeException("用户不存在");

			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.amount from t_wallet_bill t where t.amount>0 and t.added_to_outable=0 and (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-t.happen_time)>"
							+ SysParam.walletOutableTime + " and t.user_id=?").toString());
			pst.setObject(1, userId);
			rs = pst.executeQuery();
			BigDecimal totalAmount = BigDecimal.ZERO;
			List<String> billIds = new ArrayList<String>();
			while (rs.next()) {
				String walletBillId = rs.getString("id");
				billIds.add(walletBillId);
				BigDecimal amount = rs.getBigDecimal("amount");
				totalAmount = totalAmount.add(amount);
			}
			pst.close();

			if (billIds.size() > 0) {
				rightWalletOutable = rightWalletOutable.add(totalAmount);
				rightWalletUnoutable = rightWalletUnoutable.subtract(totalAmount);
				if (rightWalletUnoutable.intValue() < 0)
					throw new InteractRuntimeException("操作失败");

				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set right_wallet_unoutable=?,right_wallet_outable=? where id=?").toString());
				pst.setObject(1, rightWalletUnoutable);
				pst.setObject(2, rightWalletOutable);
				pst.setObject(3, userId);
				int cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				pst = connection.prepareStatement(
						new StringBuilder("update t_wallet_bill set added_to_outable=1 where id=?").toString());
				for (int i = 0; i < billIds.size(); i++) {
					pst.setObject(1, billIds.get(i));
					pst.addBatch();
				}
				int[] n = pst.executeBatch();
				if (n.length != billIds.size())
					throw new InteractRuntimeException("操作失败");
				pst.close();
			}
			connection.commit();

			return rightWalletOutable;
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			connection.rollback();
			throw e;
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
		}
	}
}
