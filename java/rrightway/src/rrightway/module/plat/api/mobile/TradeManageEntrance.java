package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.TradeManageEntrance")
@RequestMapping(value = "/p/m/trademanageent")
public class TradeManageEntrance {

	public static Logger logger = Logger.getLogger(TradeManageEntrance.class);

	@RequestMapping(value = "/needcheckorders")
	public void needCheckOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// 空-全部 0待核对 3买家取消 4卖家取消 5系统取消 6涉嫌返利
			String tradeStatusParam = StringUtils.trimToNull(request.getParameter("trade_status"));
			Integer tradeStatus = tradeStatusParam == null ? null : Integer.parseInt(tradeStatusParam);
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("select t.money from t_user t  where id=? ").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal userMoney = null;
			if (rs.next()) {
				userMoney = rs.getBigDecimal("money");
			} else
				throw new InteractRuntimeException("用户不存在");

			List sqlParams = new ArrayList();
			if (tradeStatus != null)
				sqlParams.add(tradeStatus);
			if (taobaoOrderid != null)
				sqlParams.add(new StringBuilder("%").append(taobaoOrderid).append("%").toString());
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buyer_remind_check_if,t.coupon_if,t.buy_way,t.id,t.order_time,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id  where 1=1 ")
							.append(tradeStatus == null ? " and t.status in (0,3,4,5,6) " : " and t.status = ?")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
				item.put("buyerRemindCheckIf", rs.getObject("buyer_remind_check_if"));

				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			data.put("userMoney", userMoney);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/needcheckorders/sellercancel")
	public void needCheckOrdersSellercancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select status,order_time from t_order t where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int status = rs.getInt("status");
				if (status != 0)
					throw new InteractRuntimeException("订单非待核对状态");
				long orderTime = rs.getLong("order_time");
				if ((new Date().getTime() - orderTime) <= (+48 * 60 * 60 * 1000l))
					throw new InteractRuntimeException("未超出48小时，不可取消，如有疑问请联系客服");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set status=4,seller_cancel_reason=? where id=?  and status=0")
							.toString());
			pst.setObject(1, reason);
			pst.setObject(2, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/needcheckorders/check")
	public void needcheckordersCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.status,t.taobao_orderid,u.money,t.return_money from t_order t left join t_user u on t.seller_id=u.id where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			BigDecimal money = null;
			BigDecimal returnMoney = null;
			if (rs.next()) {
				int status = rs.getInt("status");
				if (status != 0)
					throw new InteractRuntimeException("订单非待核对状态");
				String taobaoOrderid = rs.getString("taobao_orderid");
				money = rs.getBigDecimal("money");
				returnMoney = rs.getBigDecimal("return_money");
				if (money.compareTo(returnMoney) == -1)
					throw new InteractRuntimeException("余额不足，请充值");
				if (taobaoOrderid == null || taobaoOrderid.isEmpty())
					throw new InteractRuntimeException("买家还未填写淘宝单号");

			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set unwithdraw_money=unwithdraw_money-?,money=money-?,frozen_money=frozen_money+? where id=? ")
							.toString());
			pst.setObject(1, returnMoney);
			pst.setObject(2, returnMoney);
			pst.setObject(3, returnMoney);
			pst.setObject(4, loginStatus.getUserId());

			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set status=1 where id=? and status=0").toString());
			pst.setObject(1, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/orderdetail")
	public void orderdetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select t.buyer_cancel_reason,t.seller_cancel_reason,t.way_to_shop,t.activity_id,t.review_pic_audit,t.review_pic_commit_time,t.check_time,t.status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover,t.buy_way,t.coupon_if,t.buyer_taobaoaccount_name,t.seller_taobaoaccount_name,t.gift_express_co,t.buyer_mincredit,t.taobao_orderid from t_order t where t.id=?")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("orderId", rs.getObject("id"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("activityId", rs.getObject("activity_id"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
				item.put("reviewPicCommitTime", rs.getObject("review_pic_commit_time"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("checkTime", rs.getObject("check_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("buyerTaobaoaccountName", rs.getObject("buyer_taobaoaccount_name"));
				item.put("sellerTaobaoaccountName", rs.getObject("seller_taobaoaccount_name"));
				item.put("giftExpressCo", rs.getObject("gift_express_co"));
				item.put("buyerMincredit", rs.getObject("buyer_mincredit"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
				item.put("status", rs.getObject("status"));
				item.put("sellerCancelReason", rs.getObject("seller_cancel_reason"));
				item.put("buyerCancelReason", rs.getObject("buyer_cancel_reason"));
			} else
				throw new InteractRuntimeException("订单不存在");

			pst.close();
			JSONObject data = new JSONObject();
			data.putAll(item);
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/buyedorders/rightprotect")
	public void buyedordersRightprotect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set status=7,rightprotect_reason=? where id=? and status=1")
							.toString());
			pst.setObject(1, reason);
			pst.setObject(2, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/buyedorders/unrightprotect")
	public void buyedordersUnRightprotect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.order_time,t.review_pic_audit,t.review_pic_commit_time from t_order t where t.id=? for update")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			Long orderTime = null;
			Long reviewPicCommitTime = null;
			Integer reviewPicAudit = null;
			if (rs.next()) {
				orderTime = rs.getLong("order_time");
				reviewPicCommitTime = rs.getLong("review_pic_commit_time");
				reviewPicAudit = rs.getInt("review_pic_audit");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();
			int returnMoneyIf = 0;
			if (reviewPicAudit == 0 && (new Date().getTime() - reviewPicCommitTime) > 2 * 60 * 60 * 1000l) {
				returnMoneyIf = 1;
			} else if ((new Date().getTime() - orderTime) > 15 * 60 * 60 * 1000l) {
				returnMoneyIf = 1;
			}
			if (returnMoneyIf == 0) {
				// 未超过时间，取消维权
				pst = connection.prepareStatement(
						new StringBuilder("update t_order set status=1 where id=? and status in (7,8,9,10,11)")
								.toString());
				pst.setObject(1, orderId);
				int cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();
			} else {
				// 超过时间，返现
				pst = connection.prepareStatement(
						new StringBuilder("select t.frozen_money from t_user t where t.id=? for update").toString());
				pst.setObject(1, loginStatus.getUserId());
				rs = pst.executeQuery();
				BigDecimal frozenMoney = null;
				if (rs.next()) {
					frozenMoney = rs.getBigDecimal("frozen_money");
				} else
					throw new InteractRuntimeException("用户不存在");
				pst.close();

				pst = connection.prepareStatement(
						new StringBuilder("select t.return_money,t.buyer_id from t_order t where t.id=? for update")
								.toString());
				pst.setObject(1, orderId);
				rs = pst.executeQuery();
				BigDecimal returnMoney = null;
				String buyerId = null;
				if (rs.next()) {
					returnMoney = rs.getBigDecimal("return_money");
					buyerId = rs.getString("buyer_id");
				} else
					throw new InteractRuntimeException("订单不存在");
				pst.close();

				// 修改订单状态
				pst = connection.prepareStatement(
						new StringBuilder("update t_order set status=2 where id=? and status=1").toString());
				pst.setObject(1, orderId);
				int cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				// 卖家扣除返现
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set frozen_money=frozen_money-?,money=money-? where id=? and frozen_money-? >=0 and money-?>=0")
								.toString());
				pst.setObject(1, returnMoney);
				pst.setObject(2, returnMoney);
				pst.setObject(3, loginStatus.getUserId());
				pst.setObject(4, returnMoney);
				pst.setObject(5, returnMoney);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				String billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, loginStatus.getUserId());
				pst.setObject(3, returnMoney.negate());
				pst.setObject(4, "扣除返现");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, orderId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				// 卖家主动核对奖励
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set money=money+0.5,withdrawable_money=withdrawable_money+0.5 where id=? ")
								.toString());
				pst.setObject(1, loginStatus.getUserId());
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();
				connection.commit();

				billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, loginStatus.getUserId());
				pst.setObject(3, new BigDecimal(0.5));
				pst.setObject(4, "主动返现奖励");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, orderId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				// 买家收到返现
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set money=money+?,withdrawable_money=withdrawable_money+? where id=? ")
								.toString());
				pst.setObject(1, returnMoney);
				pst.setObject(2, returnMoney);
				pst.setObject(3, buyerId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, buyerId);
				pst.setObject(3, returnMoney);
				pst.setObject(4, "收到返现");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, orderId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
			}
			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/buyedorders/reviewpicfail")
	public void buyedordersReviewpicfail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("update t_order set review_pic_audit=2 where id=?").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/buyedorders/returnmoney")
	public void buyedordersReturnmoney(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select t.frozen_money from t_user t where t.id=? for update").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal frozenMoney = null;
			if (rs.next()) {
				frozenMoney = rs.getBigDecimal("frozen_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("select t.return_money,t.buyer_id from t_order t where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			rs = pst.executeQuery();
			BigDecimal returnMoney = null;
			String buyerId = null;
			if (rs.next()) {
				returnMoney = rs.getBigDecimal("return_money");
				buyerId = rs.getString("buyer_id");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			// 修改订单状态
			pst = connection.prepareStatement(
					new StringBuilder("update t_order set status=2 where id=? and status=1").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 卖家扣除返现
			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set frozen_money=frozen_money-?,money=money-? where id=? and frozen_money-? >=0 and money-?>=0")
							.toString());
			pst.setObject(1, returnMoney);
			pst.setObject(2, returnMoney);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, returnMoney);
			pst.setObject(5, returnMoney);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, returnMoney.negate());
			pst.setObject(4, "扣除返现");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 卖家主动核对奖励
			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set money=money+0.5,withdrawable_money=withdrawable_money+0.5 where id=? ")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();
			connection.commit();

			billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, new BigDecimal(0.5));
			pst.setObject(4, "主动返现奖励");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 买家收到返现
			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set money=money+?,withdrawable_money=withdrawable_money+? where id=? ").toString());
			pst.setObject(1, returnMoney);
			pst.setObject(2, returnMoney);
			pst.setObject(3, buyerId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, buyerId);
			pst.setObject(3, returnMoney);
			pst.setObject(4, "收到返现");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/buyedorders")
	public void buyedorders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));

			// trade_status ： 空-全部 1-维权中 2-未提交评价图 3-已提交评价图
			String tradeStatusParam = StringUtils.trimToNull(request.getParameter("trade_status"));
			Integer tradeStatus = tradeStatusParam == null ? null : Integer.parseInt(tradeStatusParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (taobaoOrderid != null)
				sqlParams.add(new StringBuilder("%").append(taobaoOrderid).append("%").toString());
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.way_to_shop,t.gift_cover,act.huabei_pay,act.creditcard_pay,t.coupon_if,t.buy_way,t.order_time,t.review_pic_audit,t.review_pics,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id left join t_activity act on t.activity_id=act.id where 1=1 ")
							.append(tradeStatus == null ? " and t.status in (1,7,10,9,8,11) "
									: (tradeStatus == 1 ? " and t.status in (7,10,9,8,11)  "
											: (tradeStatus == 2 ? " and t.status=1 and t.review_pic_audit=3 "
													: (tradeStatus == 3
															? " and  t.status=1 and t.review_pic_audit in (0,1,2) "
															: ""))))
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
				item.put("reviewPics", rs.getObject("review_pics"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/returnedorders")
	public void returnedOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (taobaoOrderid != null)
				sqlParams.add(new StringBuilder("%").append(taobaoOrderid).append("%").toString());
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.order_time,t.way_to_shop,t.gift_cover,t.review_pic_audit,a.huabei_pay,a.creditcard_pay,t.coupon_if,t.buy_way,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.status=2 ")
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("orderTime", rs.getObject("order_time"));

				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/rightprotectsorders")
	public void rightprotectsorders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			// 空:全部 1维权中 2维权成功 3维权失败
			String tradeStatusParam = StringUtils.trimToNull(request.getParameter("trade_status"));
			Integer tradeStatus = tradeStatusParam == null ? null : Integer.parseInt(tradeStatusParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (taobaoOrderid != null)
				sqlParams.add(new StringBuilder("%").append(taobaoOrderid).append("%").toString());
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.order_time,a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buy_way,t.coupon_if,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where 1=1 ")
							.append(tradeStatus == null ? " and t.status in (7,8,9,10,11,12)"
									: (tradeStatus == 1 ? " and t.status in (7,8,9,10,12)" : ""))
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("id", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}
}