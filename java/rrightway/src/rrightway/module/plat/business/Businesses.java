package rrightway.module.plat.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;

import rrightway.entity.InteractRuntimeException;

public class Businesses {

	public static void returnMoney(Connection connection, String orderId) {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(
					"select finished,buyer_id,seller_id,return_money,rightprotect_status,status,review_pic_audit from t_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int rightprotectStatus = rs.getInt("rightprotect_status");
				int status = rs.getInt("status");
				int finished = rs.getInt("finished");
				int reviewPicAudit = rs.getInt("review_pic_audit");
				BigDecimal returnMoney = rs.getBigDecimal("return_money");
				String sellerId = rs.getString("seller_id");
				String buyerId = rs.getString("buyer_id");
				pst.close();
				// 如果状态有变，跳过本次处理
				if (finished == 1) {
					connection.commit();
				}
				if (rightprotectStatus != 13 && status != 0) {
					connection.commit();
				}
				if (status != 1) {
					connection.commit();
				}
				if (reviewPicAudit != 3 && reviewPicAudit != 0) {
					connection.commit();
				}
				// 锁定买家账户
				BigDecimal withdrawableMoney = null;
				pst = connection.prepareStatement(
						new StringBuilder("select t.withdrawable_money from t_user t where t.id=? for update")
								.toString());
				pst.setObject(1, buyerId);
				rs = pst.executeQuery();
				if (rs.next()) {
					withdrawableMoney = rs.getBigDecimal("withdrawable_money");
				} else
					throw new InteractRuntimeException("用户不存在");
				pst.close();

				// 修改订单状态
				pst = connection.prepareStatement(new StringBuilder(
						"update t_order set status=2,finished=1 where id=? and rightprotect_status in (13,0) and status=1 and reviewPicAudit in (3,0)")
								.toString());
				pst.setObject(1, orderId);
				int cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");

				// 买家收到返现
				BigDecimal addMoney = returnMoney.multiply(new BigDecimal(0.2)).setScale(2, RoundingMode.DOWN);
				BigDecimal toWalletMoney = returnMoney.subtract(addMoney);
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set right_wallet_unoutable=right_wallet_unoutable+?,withdrawable_money=withdrawable_money+? where id=? ")
								.toString());
				pst.setObject(1, toWalletMoney);
				pst.setObject(2, addMoney);
				pst.setObject(3, buyerId);
				cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");

				String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, buyerId);
				pst.setObject(3, returnMoney);
				pst.setObject(4, "收到返现，其中转入右钱包" + toWalletMoney);
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, orderId);
				cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");

				String walletBillId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_wallet_bill (id,user_id,amount,note,happen_time,added_to_outable,order_id) values(?,?,?,?,?,?,?)")
								.toString());
				pst.setObject(1, walletBillId);
				pst.setObject(2, buyerId);
				pst.setObject(3, toWalletMoney);
				pst.setObject(4, "从返现转入");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, 0);
				pst.setObject(7, orderId);

				cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
			}
			connection.commit();
		} catch (Exception e) {

		}
	}
}
