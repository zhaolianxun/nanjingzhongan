package rrightway.module.plat.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import rrightway.constant.SysParam;
import rrightway.entity.InteractRuntimeException;
import rrightway.util.RrightwayDataSource;

@Component
public class GatherTask {

	private static Logger logger = Logger.getLogger(GatherTask.class);

	public GatherTask() {
		logger.info("启动定时任务 GatherTask");
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void run() {
		logger.debug("执行 task.GatherTask 定时任务");
		Connection connection = null;
		PreparedStatement pst = null;
		try {

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 下架过期或卖完的活动
			pst = connection.prepareStatement(
					"update t_activity set status=3 where status=1 and  ((stock=0) or (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-(keep_days*24*60*60*1000+start_time))>0)");

			int cnt = pst.executeUpdate();
			pst.close();
			connection.commit();
			logger.debug("下架了" + cnt + "个过期活动");

			// 核对后超过15天未主动返现或试客提交评价图后超过2天未主动返现，自动返现
			pst = connection.prepareStatement(
					"select id from t_order where rightprotect_status in (13,0) and status=1 and ( ((rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-order_time)>? and review_pic_audit=0 ) or ((rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-order_time)>? and review_pic_audit=3 ))");
			pst.setObject(1, SysParam.reviewedExpiredReturnTime);
			pst.setObject(2, SysParam.unreviewedExpiredReturnTime);
			List<String> orderIds = new ArrayList<String>();
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				orderIds.add(rs.getString("id"));
			}
			pst.close();
			logger.debug("发现需要自动返现的订单 : " + orderIds);
			for (int i = 0; i < orderIds.size(); i++) {
				String orderId = orderIds.get(i);
				try {
					// 锁定订单
					logger.debug("锁定订单");
					pst = connection.prepareStatement(
							"select buyer_id,seller_id,return_money,rightprotect_status,status,review_pic_audit from t_order where id=? for update");
					pst.setObject(1, orderId);
					rs = pst.executeQuery();
					if (rs.next()) {
						int rightprotectStatus = rs.getInt("rightprotect_status");
						int status = rs.getInt("status");
						int reviewPicAudit = rs.getInt("review_pic_audit");
						BigDecimal returnMoney = rs.getBigDecimal("return_money");
						String sellerId = rs.getString("seller_id");
						String buyerId = rs.getString("buyer_id");
						pst.close();
						// 如果状态有变，跳过本次处理
						if (rightprotectStatus != 13 && rightprotectStatus != 0) {
							logger.debug("跳过1");
							connection.commit();
							continue;
						}
						if (status != 1) {
							logger.debug("跳过3");

							connection.commit();
							continue;
						}
						if (reviewPicAudit != 3 && reviewPicAudit != 0) {
							logger.debug("跳过3");
							connection.commit();
							continue;
						}
						// 锁定买家账户
						logger.debug("锁定买家账户");
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
						logger.debug("修改订单状态");
						pst = connection.prepareStatement(new StringBuilder(
								"update t_order set status=2,finished=1 where id=? and rightprotect_status in (13,0) and status=1 and review_pic_audit in (3,0)")
										.toString());
						pst.setObject(1, orderId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");

						// 买家收到返现
						logger.debug("买家收到返现");
						BigDecimal toWalletMoney = returnMoney.multiply(new BigDecimal(0.2)).setScale(2,
								RoundingMode.DOWN);
						BigDecimal addMoney = returnMoney.subtract(toWalletMoney);
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

						logger.debug("生成账单");
						String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
						pst = connection.prepareStatement(new StringBuilder(
								"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
										.toString());
						pst.setObject(1, billId);
						pst.setObject(2, buyerId);
						pst.setObject(3, returnMoney);
						pst.setObject(4, "收到返现" + returnMoney + "元，其中转入右钱包" + toWalletMoney);
						pst.setObject(5, new Date().getTime());
						pst.setObject(6, orderId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");

						logger.debug("生成钱包账单");
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

						logger.debug("结束");
						connection.commit();

						PushMessageQueue.buyerMsg(buyerId, null,
								new StringBuilder("订单（尾号：").append(orderId.substring(orderId.length() - 5))
										.append("）已返现到您的账户").toString(),
								new StringBuilder("您的订单已返现，金额").append(returnMoney.toString()).append("，转入钱包")
										.append(toWalletMoney.toString()).append("元，订单ID：").append(orderId).toString());

						PushMessageQueue.sellerMsg(sellerId, null,
								new StringBuilder("订单（尾号：").append(orderId.substring(orderId.length() - 5))
										.append("）已自动返现给试客").toString(),
								new StringBuilder("您的订单处理已超时，自动返现给试客，金额").append(returnMoney.toString())
										.append("元，订单ID：").append(orderId).toString());
					}
					pst.close();
				} catch (Exception e) {
					logger.info(ExceptionUtils.getStackTrace(e));
					if (connection != null)
						connection.rollback();
					continue;
				} finally {
					if (pst != null)
						pst.close();
				}

			}

			// 买家超过2天未处理，自动同意维权
			// 查询需要处理的订单
			pst = connection.prepareStatement(
					"select id from t_order where rightprotect_status=7 and status=1 and rightprotect_time  is not null  and (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-(rightprotect_time))>?");
			pst.setObject(1, SysParam.autoConfirmRightProjectPeriod);
			orderIds = new ArrayList<String>();
			rs = pst.executeQuery();
			while (rs.next()) {
				orderIds.add(rs.getString("id"));
			}
			pst.close();

			for (int i = 0; i < orderIds.size(); i++) {
				String orderId = orderIds.get(i);
				try {
					// 锁定订单
					pst = connection.prepareStatement(
							"select buyer_id,seller_id,return_money,rightprotect_status,status from t_order where id=? for update");
					pst.setObject(1, orderId);
					rs = pst.executeQuery();
					if (rs.next()) {
						int rightprotectStatus = rs.getInt("rightprotect_status");
						int status = rs.getInt("status");
						BigDecimal returnMoney = rs.getBigDecimal("return_money");
						String sellerId = rs.getString("seller_id");
						String buyerId = rs.getString("buyer_id");
						pst.close();
						// 如果状态有变，跳过本次处理
						if (rightprotectStatus != 7 || status != 1) {
							connection.commit();
							continue;
						}

						// 锁定卖家账户
						BigDecimal unwithdrawMoney = null;
						pst = connection.prepareStatement(
								new StringBuilder("select t.unwithdraw_money from t_user t where t.id=? for update")
										.toString());
						pst.setObject(1, sellerId);
						rs = pst.executeQuery();
						if (rs.next()) {
							unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
						} else
							throw new InteractRuntimeException("用户不存在");
						pst.close();

						// 更新订单状态。维权成功，订单结束。
						pst = connection.prepareStatement(
								new StringBuilder("update t_order set rightprotect_status=12,finished=1 where id=?")
										.toString());
						pst.setObject(1, orderId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");

						// 返还卖家核对金额（返现+核对手续费）
						pst = connection.prepareStatement(
								new StringBuilder("update t_user set unwithdraw_money=unwithdraw_money+? where id=? ")
										.toString());
						pst.setObject(1, returnMoney.add(new BigDecimal(2)));
						pst.setObject(2, sellerId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");

						// 更新账单
						String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
						pst = connection.prepareStatement(new StringBuilder(
								"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
										.toString());
						pst.setObject(1, billId);
						pst.setObject(2, sellerId);
						pst.setObject(3, returnMoney);
						pst.setObject(4, "试客超时，自动维权成功，退还返现金额。订单尾号" + orderId.substring(orderId.length() - 5));
						pst.setObject(5, new Date().getTime());
						pst.setObject(6, orderId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");
						// 更新账单
						billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
						pst = connection.prepareStatement(new StringBuilder(
								"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
										.toString());
						pst.setObject(1, billId);
						pst.setObject(2, sellerId);
						pst.setObject(3, new BigDecimal(2));
						pst.setObject(4, "试客超时，自动维权成功，退还核对手续费。订单尾号" + orderId.substring(orderId.length() - 5));
						pst.setObject(5, new Date().getTime());
						pst.setObject(6, orderId);
						cnt = pst.executeUpdate();
						pst.close();
						if (cnt != 1)
							throw new InteractRuntimeException("操作失败");
						connection.commit();

						PushMessageQueue.buyerMsg(buyerId, null,
								new StringBuilder("订单（尾号：").append(orderId.substring(orderId.length() - 5))
										.append("）操作超时，商家对您的维权成功").toString(),
								new StringBuilder("您的订单操作超时，商家维权成功，订单ID：").append(orderId).toString());

						PushMessageQueue.sellerMsg(sellerId, null,
								new StringBuilder("订单（尾号：").append(orderId.substring(orderId.length() - 5))
										.append("）试客操作超时，您维权成功").toString(),
								new StringBuilder("您的订单试客操作超时，维权成功，退款").append(returnMoney.toString()).append("元，订单ID：")
										.append(orderId).toString());
					}

					// 删除仓库中超过30天的活动
					// 返还卖家核对金额（返现+核对手续费）
					pst = connection.prepareStatement(new StringBuilder(
							"update t_activity set del=1 where status=3 and !isnull(offline_time) and (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-offline_time) > 30*24*60*60*1000")
									.toString());
					pst.executeUpdate();
					pst.close();
					connection.commit();
				} catch (Exception e) {
					logger.info(ExceptionUtils.getStackTrace(e));
					if (connection != null)
						connection.rollback();
					continue;
				} finally {
					if (pst != null)
						pst.close();
				}
			}

		} catch (Exception e) {
			if (connection != null)
				try {
					connection.rollback();
				} catch (SQLException e1) {
					logger.info(ExceptionUtils.getStackTrace(e1));
				}
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			logger.debug("执行结束 task.GatherTask 定时任务");
		}
	}

}
