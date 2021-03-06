package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import rrightway.constant.SysParam;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.module.plat.task.PushMessageQueue;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.TradeManageEntrance")
@RequestMapping(value = "/p/m/trademanageent")
public class TradeManageEntrance {

	public static Logger logger = Logger.getLogger(TradeManageEntrance.class);

	@RequestMapping(value = "/ent")
	public void ent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select (select count(id) from t_order where seller_del=0 and seller_id=? and status=0 ) needCheckCount,(select count(id) from t_order where seller_del=0 and seller_id=? and status=1 and rightprotect_status in (0,13)) buyedCount,(select count(id) from t_order where seller_del=0 and seller_id=? and status=2) returnedCount,(select count(id) from t_order where seller_del=0 and seller_id=? and rightprotect_status !=0) rightprotectedCount,(select count(id) from t_order where seller_del=0 and seller_id=? and complain =6) complainCount")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, loginStatus.getUserId());
			pst.setObject(5, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("needCheckCount", rs.getInt("needCheckCount"));
				item.put("buyedCount", rs.getInt("buyedCount"));
				item.put("returnedCount", rs.getInt("returnedCount"));
				item.put("rightprotectedCount", rs.getInt("rightprotectedCount"));
				item.put("complainCount", rs.getInt("complainCount"));
			}

			pst.close();
			JSONObject data = new JSONObject();
			data.putAll(item);
			// 返回结果
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

			pst = connection.prepareStatement(
					new StringBuilder("select t.unwithdraw_money from t_user t  where id=? ").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal forCheckMoney = null;
			if (rs.next()) {
				forCheckMoney = rs.getBigDecimal("unwithdraw_money");
			} else
				throw new InteractRuntimeException("用户不存在");

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
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
					"select t.taobao_orderid,a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buyer_remind_check_if,t.coupon_if,t.buy_way,t.id,t.order_time,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id  where t.seller_del=0 and t.seller_id=?")
							.append(tradeStatus == null ? " and t.status in (0,3,4,5,6) " : " and t.status = ?")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.activity_title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ")
							.append(" order by t.order_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
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
			data.put("forCheckMoney", forCheckMoney);
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
				if ((new Date().getTime() - orderTime) <= SysParam.cancelableOrderTime)
					throw new InteractRuntimeException("未超出2小时，不可取消，如有疑问请联系客服");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set finished=1,status=4,seller_cancel_reason=? where id=?  and status=0")
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

	@RequestMapping(value = "/del")
	public void needCheckOrdersDel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select finished from t_order t where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int finished = rs.getInt("finished");
				if (finished == 0)
					throw new InteractRuntimeException("订单还未结束，不可删除");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set seller_del=1 where id=? and finished=1").toString());
			pst.setObject(1, orderId);
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
			if (loginStatus.getStatus() == 1)
				throw new InteractRuntimeException("您的账户已被冻结，请联系客服。");
			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.buyer_id,t.status,t.taobao_orderid,t.return_money,u.unwithdraw_money,t.order_time from t_order t left join t_user u on t.seller_id=u.id where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			BigDecimal unwithdrawMoney = null;
			BigDecimal returnMoney = null;
			String buyerId = null;
			BigDecimal serviceCharge = new BigDecimal(2);
			if (rs.next()) {
				int status = rs.getInt("status");
				if (status != 0)
					throw new InteractRuntimeException("订单非待核对状态");
				String taobaoOrderid = rs.getString("taobao_orderid");
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
				returnMoney = rs.getBigDecimal("return_money");
				if (unwithdrawMoney.compareTo(returnMoney.add(serviceCharge)) == -1)
					throw new InteractRuntimeException("余额不足，请充值");

				long orderTime = rs.getLong("order_time");
				if ((taobaoOrderid == null || taobaoOrderid.isEmpty())
						&& (new Date().getTime() - orderTime) < 2 * 60 * 60 * 100l)
					throw new InteractRuntimeException("试客未填写淘宝单号的请在下单2小时候再操作");
				buyerId = rs.getString("buyer_id");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_user set unwithdraw_money=unwithdraw_money-? where id=? ").toString());
			pst.setObject(1, returnMoney.add(serviceCharge));
			pst.setObject(2, loginStatus.getUserId());
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set check_time=?,status=1,auto_return_time=? where id=? and status=0").toString());
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, new Date().getTime() + SysParam.unreviewedExpiredReturnTime);
			pst.setObject(3, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, serviceCharge.negate());
			pst.setObject(4, "核对手续费,订单尾号" + orderId.substring(orderId.length() - 5));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, returnMoney.negate());
			pst.setObject(4, "核对订单扣除,订单尾号" + orderId.substring(orderId.length() - 5));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();
			connection.commit();

			PushMessageQueue.orderBeCheckedToBuyer(buyerId, null, orderId);
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
					"select t.complain,t.complain_time,t.complain_proof_pics,t.complain_explanation,t.complain_reason,t.rightprotect_time,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_seller_proof_desc,t.rightprotect_buyer_proof_desc,t.rightprotect_seller_proof_pics,t.rightprotect_buyer_proof_pics,t.rightprotect_buyer_addkf_describe,t.rightprotect_buyer_addkf_pics,t.rightprotect_seller_addkf_pics,t.rightprotect_seller_addkf_describe,t.rightprotect_reason,t.rightprotect_status,t.review_pics,t.auto_return_time,t.activity_title,t.buyer_remind_check_if,a.coupon_url,t.gift_express_co,t.buyer_cancel_reason,t.seller_cancel_reason,t.way_to_shop,t.activity_id,t.review_pic_audit,t.review_pic_commit_time,t.check_time,t.status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover,t.buy_way,t.coupon_if,t.buyer_taobaoaccount_name,t.seller_taobaoaccount_name,t.gift_express_co,t.buyer_mincredit,t.taobao_orderid from t_order t  left join t_activity a on t.activity_id=a.id where t.id=?")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("complainProofPics", rs.getObject("complain_proof_pics"));
				item.put("complainExplanation", rs.getObject("complain_explanation"));
				item.put("complainReason", rs.getObject("complain_reason"));
				item.put("complainTime", rs.getObject("complain_time"));
				item.put("complain", rs.getObject("complain"));
				item.put("rightprotectSellerProof", rs.getObject("rightprotect_seller_proof"));
				item.put("rightprotectBuyerProof", rs.getObject("rightprotect_buyer_proof"));
				item.put("rightprotectSellerAddkf", rs.getObject("rightprotect_seller_addkf"));
				item.put("rightprotectBuyerAddkf", rs.getObject("rightprotect_buyer_addkf"));
				item.put("rightprotectSellerProofDesc", rs.getObject("rightprotect_seller_proof_desc"));
				item.put("rightprotectBuyerProofDesc", rs.getObject("rightprotect_buyer_proof_desc"));
				item.put("rightprotectSellerProofPics", rs.getObject("rightprotect_seller_proof_pics"));
				item.put("rightprotectBuyerProofPics", rs.getObject("rightprotect_buyer_proof_pics"));
				item.put("rightprotectBuyerAddkfPics", rs.getObject("rightprotect_buyer_addkf_pics"));
				item.put("rightprotectBuyerAddkfDescribe", rs.getObject("rightprotect_buyer_addkf_describe"));
				item.put("rightprotectSellerAddkfPics", rs.getObject("rightprotect_seller_addkf_pics"));
				item.put("rightprotectSellerAddkfDescribe", rs.getObject("rightprotect_seller_addkf_describe"));
				item.put("rightprotectReason", rs.getObject("rightprotect_reason"));
				item.put("rightprotectStatus", rs.getObject("rightprotect_status"));
				item.put("orderId", rs.getObject("id"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("buyerRemindCheckIf", rs.getObject("buyer_remind_check_if"));
				item.put("giftExpressCo", rs.getObject("gift_express_co"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("activityId", rs.getObject("activity_id"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
				item.put("reviewPics", rs.getObject("review_pics"));
				item.put("reviewPicCommitTime", rs.getObject("review_pic_commit_time"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("checkTime", rs.getObject("check_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("couponUrl", rs.getObject("coupon_url"));
				item.put("buyerTaobaoaccountName", rs.getObject("buyer_taobaoaccount_name"));
				item.put("sellerTaobaoaccountName", rs.getObject("seller_taobaoaccount_name"));
				item.put("buyerMincredit", rs.getObject("buyer_mincredit"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
				item.put("status", rs.getObject("status"));
				item.put("sellerCancelReason", rs.getObject("seller_cancel_reason"));
				item.put("buyerCancelReason", rs.getObject("buyer_cancel_reason"));
				item.put("autoReturnTime", rs.getObject("auto_return_time"));
				item.put("rightprotectTime", rs.getObject("rightprotect_time"));
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
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.finished,t.rightprotect_status,t.status from t_order t where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int finished = rs.getInt("finished");
				int rightprotectStatus = rs.getInt("rightprotect_status");
				int status = rs.getInt("status");
				if (finished == 1)
					throw new InteractRuntimeException("订单已结束");
				if (rightprotectStatus == 12)
					throw new InteractRuntimeException("维权已成功");
				if (rightprotectStatus == 7 || rightprotectStatus == 10)
					throw new InteractRuntimeException("正在维权中");
				if (status == 0)
					throw new InteractRuntimeException("订单还未核对");
				if (status == 2)
					throw new InteractRuntimeException("订单已返现");
				if (status == 3 || status == 4 || status == 5)
					throw new InteractRuntimeException("订单已取消");
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set rightprotect_time=?,rightprotect_status=7,rightprotect_reason=?,rightprotect_seller_addkf=0,rightprotect_buyer_addkf=0,rightprotect_seller_addkf_pics=null,rightprotect_buyer_addkf_pics=null,rightprotect_seller_addkf_describe=null,rightprotect_buyer_addkf_describe=null,rightprotect_seller_proof=0,rightprotect_buyer_proof=0,rightprotect_seller_proof_pics=null,rightprotect_buyer_proof_pics=null,rightprotect_seller_proof_desc=null,rightprotect_buyer_proof_desc=null where id=? and status=1 and rightprotect_status in (0,13) and finished=0")
							.toString());
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, reason);
			pst.setObject(3, orderId);
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
					"select t.finished,t.rightprotect_status,t.order_time,t.review_pic_audit,t.review_pic_commit_time from t_order t where t.id=? and t.seller_id=? for update")
							.toString());
			pst.setObject(1, orderId);
			pst.setObject(2, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			Long orderTime = null;
			Long reviewPicCommitTime = null;
			int reviewPicAudit = 0;
			int rightprotectStatus = 0;
			int finished = 0;
			if (rs.next()) {
				orderTime = rs.getLong("order_time");
				reviewPicCommitTime = rs.getLong("review_pic_commit_time");
				reviewPicAudit = rs.getInt("review_pic_audit");
				rightprotectStatus = rs.getInt("rightprotect_status");
				finished = rs.getInt("finished");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			if (finished == 1)
				throw new InteractRuntimeException("订单已结束");
			if (rightprotectStatus == 0)
				throw new InteractRuntimeException("订单未维权");
			if (rightprotectStatus == 12)
				throw new InteractRuntimeException("订单维权已成功");
			if (rightprotectStatus == 13)
				throw new InteractRuntimeException("订单维权已失败");

			int returnMoneyIf = 0;
			if (reviewPicAudit == 0
					&& (new Date().getTime() - reviewPicCommitTime) >= SysParam.reviewedExpiredReturnTime) {
				returnMoneyIf = 1;
			} else if (reviewPicAudit == 3
					&& (new Date().getTime() - orderTime) >= SysParam.unreviewedExpiredReturnTime) {
				returnMoneyIf = 1;
			}
			if (returnMoneyIf == 0) {
				// 未超过时间，取消维权
				pst = connection.prepareStatement(new StringBuilder(
						"update t_order set rightprotect_status=0 where id=? and rightprotect_status in (7,10) and status=1")
								.toString());
				pst.setObject(1, orderId);
				int cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");

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
				pst = connection.prepareStatement(new StringBuilder(
						"update t_order set status=2,rightprotect_status=0,finished=1 where id=? and status=1 and rightprotect_status in (7,8,9,10,11)")
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
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

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
			pst = connection.prepareStatement(
					new StringBuilder("select t.status,t.buyer_id,t.review_pic_audit from t_order t  where t.id=?")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			String buyerId = null;
			int reviewPicAudit = 0;

			if (rs.next()) {
				int status = rs.getInt("status");
				if (status != 1)
					throw new InteractRuntimeException("订单目前不可提交评价图，状态非已核对");
				buyerId = rs.getString("buyer_id");
				reviewPicAudit = rs.getInt("review_pic_audit");
				if (reviewPicAudit == 3)
					throw new InteractRuntimeException("试客还未提交评价图");
			}
			pst.close();

			pst = connection
					.prepareStatement(new StringBuilder("update t_order set review_pic_audit=2 where id=?").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			if (reviewPicAudit == 0)
				PushMessageQueue.buyerMsg(buyerId, null,
						new StringBuilder("您的订单（尾号").append(orderId.substring(orderId.length() - 5))
								.append("）提交的评价图被商家判为无效。").toString(),
						new StringBuilder("您的订单提交的评价图被商家判为无效，请重新提交。订单号：").append(orderId).toString());
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

			pst = connection.prepareStatement(new StringBuilder(
					"select t.return_money,t.buyer_id,t.rightprotect_status from t_order t where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			rs = pst.executeQuery();
			BigDecimal returnMoney = null;
			String buyerId = null;
			if (rs.next()) {
				returnMoney = rs.getBigDecimal("return_money");
				buyerId = rs.getString("buyer_id");
				int rightprotectStatus = rs.getInt("rightprotect_status");
				if (rightprotectStatus == 7 || rightprotectStatus == 10)
					throw new InteractRuntimeException("正在维权，不可操作");
				if (rightprotectStatus == 12)
					throw new InteractRuntimeException("维权成功，不可操作");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			// 修改订单状态
			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set status=2,finished=1 where id=? and status=1 and rightprotect_status in (0,13)")
							.toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 卖家主动返现奖励
			pst = connection.prepareStatement(
					new StringBuilder("update t_user set unwithdraw_money=unwithdraw_money+0.5 where id=? ")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
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
			BigDecimal toWalletMoney = returnMoney.multiply(new BigDecimal(0.2)).setScale(2, RoundingMode.DOWN);
			BigDecimal addMoney = returnMoney.subtract(toWalletMoney);
			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set right_wallet_unoutable=right_wallet_unoutable+?,withdrawable_money=withdrawable_money+? where id=? ")
							.toString());
			pst.setObject(1, toWalletMoney);
			pst.setObject(2, addMoney);
			pst.setObject(3, buyerId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, buyerId);
			pst.setObject(3, addMoney);
			pst.setObject(4, "收到返现" + returnMoney + "元，其中转入右钱包" + toWalletMoney);
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
			cnt = pst.executeUpdate();
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
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();

			PushMessageQueue.buyerMsg(buyerId, null,
					new StringBuilder("订单（尾号").append(orderId.substring(orderId.length() - 5)).append("）已返现")
							.toString(),
					new StringBuilder("已返现到您的账户，金额").append(returnMoney.toString()).append("，转入钱包")
							.append(toWalletMoney.toString()).append("元，订单号：").append(orderId).toString());
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
			sqlParams.add(loginStatus.getUserId());
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
					"select t.seller_taobaoaccount_name,t.buyer_taobaoaccount_name,t.taobao_orderid,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.buyer_remind_check_if,t.rightprotect_status,t.way_to_shop,t.gift_cover,act.huabei_pay,act.creditcard_pay,t.coupon_if,t.buy_way,t.order_time,t.review_pic_audit,t.review_pics,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id left join t_activity act on t.activity_id=act.id where t.seller_del=0 and t.seller_id=?")
							.append(tradeStatus == null ? " and t.status=1 and t.rightprotect_status in (0,7,10,13)  "
									: (tradeStatus == 1 ? " and t.status=1 and t.rightprotect_status in (7,10)  "
											: (tradeStatus == 2
													? " and t.status=1 and t.review_pic_audit=3 and t.rightprotect_status in (0,13)"
													: (tradeStatus == 3
															? " and  t.status=1 and t.review_pic_audit in (0,2) and t.rightprotect_status in (0,13)"
															: ""))))
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.activity_title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ")
							.append(" order by t.order_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("sellerTaobaoaccountName", rs.getObject("seller_taobaoaccount_name"));
				item.put("buyerTaobaoaccountName", rs.getObject("buyer_taobaoaccount_name"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
				item.put("rightprotectSellerProof", rs.getObject("rightprotect_seller_proof"));
				item.put("rightprotectBuyerProof", rs.getObject("rightprotect_buyer_proof"));
				item.put("rightprotectSellerAddkf", rs.getObject("rightprotect_seller_addkf"));
				item.put("rightprotectBuyerAddkf", rs.getObject("rightprotect_buyer_addkf"));
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
				item.put("rightprotectStatus", rs.getObject("rightprotect_status"));
				item.put("buyerRemindCheckIf", rs.getObject("buyer_remind_check_if"));
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
			sqlParams.add(loginStatus.getUserId());
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
					"select t.review_pics,t.order_time,t.way_to_shop,t.gift_cover,t.review_pic_audit,a.huabei_pay,a.creditcard_pay,t.coupon_if,t.buy_way,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.seller_del=0 and t.status=2 and t.seller_id=?")
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.activity_title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ")
							.append(" order by t.order_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("reviewPics", rs.getObject("review_pics"));
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
			sqlParams.add(loginStatus.getUserId());
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

			String sql = new StringBuilder(
					"select t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_status,t.order_time,a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buy_way,t.coupon_if,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.seller_del=0 and t.seller_id=?")
							.append(tradeStatus == null ? " and t.rightprotect_status != 0 "
									: (tradeStatus == 1 ? " and t.rightprotect_status in (7,10)"
											: (tradeStatus == 2 ? " and t.rightprotect_status=12"
													: (tradeStatus == 3 ? " and t.rightprotect_status=13" : ""))))
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(title == null ? "" : " and t.activity_title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ")
							.append(" order by t.rightprotect_time desc limit ?,? ").toString();
			logger.debug(sql);
			logger.debug(sqlParams);
			pst = connection.prepareStatement(sql);
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("rightprotectSellerProof", rs.getObject("rightprotect_seller_proof"));
				item.put("rightprotectBuyerProof", rs.getObject("rightprotect_buyer_proof"));
				item.put("rightprotectSellerAddkf", rs.getObject("rightprotect_seller_addkf"));
				item.put("rightprotectBuyerAddkf", rs.getObject("rightprotect_buyer_addkf"));
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
				item.put("rightprotectStatus", rs.getObject("rightprotect_status"));
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

	@RequestMapping(value = "/rightprotectsorders/addkf")
	public void rightprotectsordersAddkf(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String describe = StringUtils.trimToNull(request.getParameter("describe"));
			if (describe == null)
				throw new InteractRuntimeException("describe 不能空");
			String picsParam = StringUtils.trimToNull(request.getParameter("pics"));
			if (picsParam == null)
				throw new InteractRuntimeException("请上传图片");
			String[] pics = picsParam.split(",");
			if (pics.length == 0)
				throw new InteractRuntimeException("请上传图片");
			if (pics.length > 3)
				throw new InteractRuntimeException("最多三张图片");

			// 处理图片参数
			picsParam = "";
			for (int i = 0; i < pics.length; i++) {
				if (StringUtils.isNotEmpty(pics[i])) {
					if (i == 0)
						picsParam = pics[i];
					else
						picsParam = picsParam + "," + pics[i];
				}
			}

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set rightprotect_seller_addkf_pics=?,rightprotect_seller_addkf_describe=?,rightprotect_seller_addkf=1 where id=? and seller_id=? and rightprotect_status=10")
							.toString());
			pst.setObject(1, picsParam);
			pst.setObject(2, describe);
			pst.setObject(3, orderId);
			pst.setObject(4, loginStatus.getUserId());
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

	@RequestMapping(value = "/rightprotectsorders/addproof")
	public void rightprotectsordersAddproof(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String describe = StringUtils.trimToNull(request.getParameter("describe"));
			if (describe == null)
				throw new InteractRuntimeException("describe 不能空");
			String picsParam = StringUtils.trimToNull(request.getParameter("pics"));
			if (picsParam == null)
				throw new InteractRuntimeException("请上传图片");
			String[] pics = picsParam.split(",");
			if (pics.length == 0)
				throw new InteractRuntimeException("请上传图片");
			if (pics.length > 3)
				throw new InteractRuntimeException("最多三张图片");

			// 处理图片参数
			picsParam = "";
			for (int i = 0; i < pics.length; i++) {
				if (StringUtils.isNotEmpty(pics[i])) {
					if (i == 0)
						picsParam = pics[i];
					else
						picsParam = picsParam + "," + pics[i];
				}
			}

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set rightprotect_seller_proof_pics=?,rightprotect_seller_proof_desc=?,rightprotect_seller_proof=1 where id=? and seller_id=? and rightprotect_status=10 and rightprotect_buyer_addkf=1")
							.toString());
			pst.setObject(1, picsParam);
			pst.setObject(2, describe);
			pst.setObject(3, orderId);
			pst.setObject(4, loginStatus.getUserId());
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

	@RequestMapping(value = "/complainorders")
	public void complainorders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String complainTimeStartParam = StringUtils.trimToNull(request.getParameter("complain_time_start"));
			Long complainTimeStart = complainTimeStartParam == null ? null : Long.parseLong(complainTimeStartParam);
			String complainTimeEndParam = StringUtils.trimToNull(request.getParameter("complain_time_end"));
			Long complainTimeEnd = complainTimeEndParam == null ? null : Long.parseLong(complainTimeEndParam);
			String complainStatusParam = StringUtils.trimToNull(request.getParameter("complain_status"));
			Integer complainStatus = complainStatusParam == null ? null : Integer.parseInt(complainStatusParam);
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
			sqlParams.add(loginStatus.getUserId());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (taobaoOrderid != null)
				sqlParams.add(new StringBuilder("%").append(taobaoOrderid).append("%").toString());
			if (complainTimeStart != null)
				sqlParams.add(complainTimeStart);
			if (complainTimeEnd != null)
				sqlParams.add(complainTimeEnd);

			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.activity_title,t.complain,t.status,t.finished,a.huabei_pay,a.creditcard_pay,t.buy_way,t.finished,t.complain_time,t.way_to_shop,t.coupon_if,t.buy_way,t.complain,t.id,t.pay_price,t.return_money,t.gift_name,t.gift_cover from t_order t left join t_activity a on t.activity_id=t.id  where t.del=0 and t.complain=6 and t.seller_id=?")
							.append(title == null ? "" : " and t.activity_title like ? ")
							.append(buyerNickname == null ? "" : " and t.buyer_taobaoaccount_name like ? ")
							.append(sellerNickname == null ? "" : " and t.seller_taobaoaccount_name like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(complainTimeStart == null ? "" : " and t.complain_time >= ? ")
							.append(complainTimeEnd == null ? "" : " and t.complain_time <= ? ")
							.append(" order by t.complain_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("complain", rs.getObject("complain"));
				item.put("id", rs.getObject("id"));
				item.put("complainTime", rs.getObject("complain_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("complain", rs.getObject("complain"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
				item.put("finished", rs.getObject("finished"));
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