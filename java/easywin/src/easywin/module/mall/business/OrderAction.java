package easywin.module.mall.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import easywin.entity.InteractRuntimeException;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

public class OrderAction {

	public static Logger logger = Logger.getLogger(OrderAction.class);

	public static void payComplete(String orderId, String payerId, int payAmount, String payType) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select t.user_id,t.mall_id,t.amount,t.status from t_mall_order t where t.id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			int amount = 0;
			String status = null;
			String mallId = null;
			if (rs.next()) {
				amount = rs.getInt("amount");
				status = rs.getString("status");
				mallId = rs.getString("mall_id");
				if (StringUtils.isEmpty(payerId))
					payerId = rs.getString("user_id");
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

			// 更新销量和购买人数
			pst = connection.prepareStatement(
					"update t_mall_good t left join t_mall_order_detail od on od.good_id=t.id set t.saled_count=t.saled_count+od.count,t.buyer_count=t.buyer_count+1 where od.order_id=?");
			pst.setObject(1, orderId);
			n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(
					"select u.register_from_user_id,ifnull(insert((if(isnull(u.phone),if(isnull(u.nickname),null,u.nickname),u.phone)),2,3,'**'),'匿名') user_logo from t_mall_user u where u.id=?");
			pst.setObject(1, payerId);
			rs = pst.executeQuery();
			String registerFromUserId = null;
			String userLogo = null;
			if (rs.next()) {
				registerFromUserId = rs.getString("register_from_user_id");
				userLogo = rs.getString("user_logo");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 分销奖励
			if (registerFromUserId != null) {
				pst = connection.prepareStatement(
						"select od.name,od.id order_detail_id,od.good_id,g.share_reward from t_mall_order_detail od left join t_mall_good g on od.good_id=g.id where od.order_id=?");
				pst.setObject(1, orderId);
				rs = pst.executeQuery();
				List<Map<String, Object>> sqlRsList = new ArrayList<Map<String, Object>>();
				while (rs.next()) {
					Map<String, Object> sqlRs = new HashMap<String, Object>();
					sqlRs.put("goodName", rs.getString("name"));
					sqlRs.put("shareReward", (Integer) rs.getObject("share_reward"));
					sqlRs.put("orderDetailId", (Integer) rs.getObject("order_detail_id"));
					sqlRsList.add(sqlRs);
				}
				pst.close();

				Integer totalShareReward = 0;
				pst = connection.prepareStatement(
						"insert into t_mall_user_bill (user_id,amount,note,happen_time,link,type,mall_id) values(?,?,?,?,?,1,?)");
				for (int i = 0; i < sqlRsList.size(); i++) {
					String goodName = (String) sqlRsList.get(i).get("goodName");
					Integer shareReward = (Integer) sqlRsList.get(i).get("shareReward");
					Integer orderDetailId = (Integer) sqlRsList.get(i).get("orderDetailId");
					if (shareReward != null && shareReward > 0) {
						totalShareReward = totalShareReward + shareReward;
						pst.setObject(1, registerFromUserId);
						pst.setObject(2, shareReward);
						pst.setObject(3, new StringBuilder("分享奖励。受您推荐的'").append(userLogo).append("'购买了'")
								.append(goodName).append("'").toString());
						pst.setObject(4, new Date().getTime());
						pst.setObject(5, orderDetailId);
						pst.setObject(6, mallId);
						n = pst.executeUpdate();
						if (n != 1)
							throw new InteractRuntimeException("操作失败");
					}
				}
				pst.close();

				pst = connection.prepareStatement("update t_mall_user set money=money+? where id=?");
				pst.setObject(1, totalShareReward);
				pst.setObject(2, registerFromUserId);
				n = pst.executeUpdate();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();
			}

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

	public static void sign(String orderId, Connection connection) throws Exception {
		PreparedStatement pst = null;
		try {
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select id from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("订单号不存在");
			}
			pst.close();

			pst = connection.prepareStatement(
					"update t_mall_order set finish_time=?,finished=1,status=3,receive_time=? where id=? and status='2'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, orderId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("卖家还没发货或已签收");
			pst.close();

			connection.commit();
			// 返回结果
		} catch (Exception e) {
			// 处理异常
			if (connection != null)
				connection.rollback();
			throw e;
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
		}
	}

	public static void sign(String orderId) throws Exception {
		Connection connection = null;
		try {
			connection = EasywinDataSource.dataSource.getConnection();
			sign(orderId, connection);
		} catch (Exception e) {
			throw e;
		} finally {
			if (connection != null)
				connection.close();
		}
	}

}
