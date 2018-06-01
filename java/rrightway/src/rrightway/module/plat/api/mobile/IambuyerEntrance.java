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

import rrightway.constant.SysParam;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.IambuyerEntrance")
@RequestMapping(value = "/p/m/iambuyerent")
public class IambuyerEntrance {

	public static Logger logger = Logger.getLogger(IambuyerEntrance.class);

	@RequestMapping(value = "/applyedorders/remindcheck")
	public void applyedordersRemindcheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.taobao_orderid,t.status from t_order t where t.id=?").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				String taobaoOrderid = rs.getString("taobao_orderid");
				int status = rs.getInt("status");
				if (status == 2)
					throw new InteractRuntimeException("已返现");
				if (status == 3 || status == 4)
					throw new InteractRuntimeException("已取消");
				if (status == 1)
					throw new InteractRuntimeException("已核对");
				if (status != 0)
					throw new InteractRuntimeException("非待核对状态");
				if (taobaoOrderid != null || taobaoOrderid.isEmpty())
					throw new InteractRuntimeException("淘宝订单号未上传");
			} else
				throw new InteractRuntimeException("订单号不存在");

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

	@RequestMapping(value = "/applyedorders")
	public void applyedOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String tradeStatusParam = StringUtils.trimToNull(request.getParameter("trade_status"));
			Integer tradeStatus = tradeStatusParam == null ? 1 : Integer.parseInt(tradeStatusParam);
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
					"select t.seller_cancel_reason,t.buyer_cancel_reason,t.way_to_shop,t.coupon_if,t.buy_way,t.taobao_orderid,t.status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover from t_order t where t.del=0 and t.buyer_id=? ")
							.append(tradeStatus == 1 ? " and t.status in (0,3,4,5) " : "")
							.append(tradeStatus == 2 ? " and t.status=0 " : "")
							.append(tradeStatus == 3
									? " and (t.status=0 and (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-t.order_time) > 48*60*60*1000) "
									: "")
							.append(buyerNickname == null ? "" : " and t.buyer_taobaoaccount_name like ? ")
							.append(sellerNickname == null ? "" : " and t.seller_taobaoaccount_name like ? ")
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
				item.put("sellerCancelReason", rs.getObject("seller_cancel_reason"));
				item.put("buyerCancelReason", rs.getObject("buyer_cancel_reason"));
				item.put("orderId", rs.getObject("id"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("status", rs.getObject("status"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject orders = new JSONObject();
			orders.put("items", items);
			data.put("orders", orders);
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

	@RequestMapping(value = "/applyedorders/complete")
	public void applyedOrdersComplete(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			if (taobaoOrderid == null)
				throw new InteractRuntimeException("taobao_orderid 不能空");
			if (taobaoOrderid.length() != 18 || !StringUtils.isNumeric(taobaoOrderid))
				throw new InteractRuntimeException("请填写18位纯数字淘宝订单号");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection
					.prepareStatement(new StringBuilder("update t_order set taobao_orderid=? where id=?").toString());
			pst.setObject(1, taobaoOrderid);
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

	@RequestMapping(value = "/applyedorders/checkseller")
	public void applyedOrdersCheckseller(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String sellerTaobaoname = StringUtils.trimToNull(request.getParameter("seller_taobaoname"));
			if (sellerTaobaoname == null)
				throw new InteractRuntimeException("seller_taobaoname 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("select t.seller_taobaoaccount_name from t_order t  where t.id=?").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			int checkSuccess = 0;
			if (rs.next()) {
				String sellerTaobaoaccountName = rs.getString("seller_taobaoaccount_name");
				if (sellerTaobaoname.equals(sellerTaobaoaccountName))
					checkSuccess = 1;
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("checkSuccess", checkSuccess);
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

	@RequestMapping(value = "/applyedorders/cancel")
	public void applyedOrdersCancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.status from t_order t  where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int status = rs.getInt("status");
				if (status == 1)
					throw new InteractRuntimeException("已核对订单不可取消");
				if (status == 2)
					throw new InteractRuntimeException("已返现订单不可取消");
				if (status == 3 || status == 4)
					throw new InteractRuntimeException("请不要重复取消");
			}
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set finished=1,status=3,buyer_cancel_reason=? where id=?")
							.toString());
			pst.setObject(1, reason);
			pst.setObject(2, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/applyedorders/del")
	public void applyedOrdersDel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.finished from t_order t  where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int finished = rs.getInt("finished");
				if (finished == 0)
					throw new InteractRuntimeException("订单还未结束不可删除");
			}
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set del=1 where id=? and finished=1").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/checkedorders")
	public void checkedOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String taobaoOrderid = StringUtils.trimToNull(request.getParameter("taobao_orderid"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String tradeStatusParam = StringUtils.trimToNull(request.getParameter("trade_status"));
			Integer tradeStatus = tradeStatusParam == null ? 1 : Integer.parseInt(tradeStatusParam);
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
					"select t.activity_id,t.way_to_shop,t.coupon_if,t.buy_way,t.complain,t.review_pic_audit,t.review_pics,t.rightprotect_status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover from t_order t where del=0 and t.buyer_id=?")
							.append(tradeStatus == 1 ? " and t.status=1 " : "")
							.append(tradeStatus == 2 ? " and t.status=1 and t.rightprotect_status in (7,8,9,10,11) "
									: "")
							.append(buyerNickname == null ? "" : " and t.buyer_taobaoaccount_name like ? ")
							.append(sellerNickname == null ? "" : " and t.seller_taobaoaccount_name like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("activityId", rs.getObject("activity_id"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("rightprotectStatus", rs.getObject("rightprotect_status"));
				item.put("reviewPics", rs.getObject("review_pics"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
				item.put("complain", rs.getObject("complain"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject orders = new JSONObject();
			orders.put("items", items);
			data.put("orders", orders);
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

	@RequestMapping(value = "/checkedorders/submitreviewpic")
	public void checkedordersSubmitreviewpic(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String reviewPics = StringUtils.trimToNull(request.getParameter("review_pics"));
			if (reviewPics == null)
				throw new InteractRuntimeException("review_pics 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select t.status from t_order t  where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int status = rs.getInt("status");
				if (status == 0)
					throw new InteractRuntimeException("还未核对,不可提交");
				if (status == 2)
					throw new InteractRuntimeException("已返现,不可提交");
				if (status == 3 || status == 4)
					throw new InteractRuntimeException("已取消,不可提交");
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set auto_return_time=?,review_pic_audit=0,review_pics=?,review_pic_commit_time=? where id=?")
							.toString());
			pst.setObject(1, new Date().getTime() + SysParam.reviewedExpiredReturnTime);
			pst.setObject(2, reviewPics);
			pst.setObject(3, new Date().getTime());
			pst.setObject(4, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/checkedorders/delreviewpic")
	public void checkedordersDelreviewpic(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.review_pic_audit from t_order t  where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int audit = rs.getInt("review_pic_audit");
				if (audit != 0)
					throw new InteractRuntimeException("评价图审核状态有误");
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set review_pic_audit=3,review_pics=null,review_pic_commit_time=null where id=?")
							.toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/complain")
	public void complain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不能空");
			String explanation = StringUtils.trimToNull(request.getParameter("explanation"));
			if (explanation == null)
				throw new InteractRuntimeException("explanation 不能空");
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不能空");
			String proofPicsParam = StringUtils.trimToNull(request.getParameter("proof_pics"));
			if (proofPicsParam == null)
				throw new InteractRuntimeException("请上传图片");
			String[] proofPics = proofPicsParam.split(",");
			if (proofPics.length == 0)
				throw new InteractRuntimeException("请上传图片");
			if (proofPics.length > 2)
				throw new InteractRuntimeException("最多两张图片");

			// 处理图片参数
			proofPicsParam = "";
			for (int i = 0; i < proofPics.length; i++) {
				if (StringUtils.isNotEmpty(proofPics[i])) {
					if (i == 0)
						proofPicsParam = proofPics[i];
					else
						proofPicsParam = proofPicsParam + "," + proofPics[i];
				}
			}

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select t.complain,t.status from t_order t where t.id=? for update").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int complain = rs.getInt("complain");
				int status = rs.getInt("status");
				if (status != 1)
					throw new InteractRuntimeException("核对后的订单才可以投诉");
				if (complain != 0 && complain != 2)
					throw new InteractRuntimeException("该订单已经投诉过");
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_order set complain_time=?,complain_proof_pics=?,complain=1,complain_explanation=?,complain_reason=? where id=?")
							.toString());
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, proofPicsParam);
			pst.setObject(3, explanation);
			pst.setObject(4, reason);
			pst.setObject(5, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/complainorders/cancel")
	public void complainordersCancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("update t_order set complain=5 where id=? and finished=0 and complain in (1,6) ")
							.toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
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

	@RequestMapping(value = "/checkedorders/remindreturn")
	public void remindReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.review_pic_audit,t.status from t_order t where t.id=?").toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int audit = rs.getInt("review_pic_audit");
				int status = rs.getInt("status");
				if (status == 0)
					throw new InteractRuntimeException("卖家还未核对");
				if (status == 2)
					throw new InteractRuntimeException("已返现");
				if (status == 3 || status == 4 || status == 5)
					throw new InteractRuntimeException("已取消");
				if (status != 1)
					throw new InteractRuntimeException("订单不是已核对状态");
				if (audit == 3)
					throw new InteractRuntimeException("评价图未提交");
				if (audit == 2)
					throw new InteractRuntimeException("评价图有误，请重新提交");
			}
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set buyer_remind_return_if=1 where id=?").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
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

	@RequestMapping(value = "/returnedorders")
	public void returnedOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
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
					"select t.way_to_shop,t.coupon_if,t.buy_way,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover from t_order t where t.del=0 and t.status=2 and t.buyer_id=?")
							.append(buyerNickname == null ? "" : " and t.buyer_taobaoaccount_name like ? ")
							.append(sellerNickname == null ? "" : " and t.seller_taobaoaccount_name like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject orders = new JSONObject();
			orders.put("items", items);
			data.put("orders", orders);
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
					"select t.complain_fail_reason,t.activity_title,a.coupon_url,t.auto_return_time,t.rightprotect_time,t.review_pics,t.complain_proof_pics,t.complain,t.complain_explanation,t.complain_reason,t.complain_time,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_seller_proof_desc,t.rightprotect_buyer_proof_desc,t.rightprotect_seller_proof_pics,t.rightprotect_buyer_proof_pics,t.rightprotect_buyer_addkf_describe,t.rightprotect_buyer_addkf_pics,t.rightprotect_seller_addkf_pics,t.rightprotect_seller_addkf_describe,t.rightprotect_reason,t.rightprotect_status,t.buyer_cancel_reason,t.seller_cancel_reason,t.way_to_shop,t.activity_id,t.review_pic_audit,t.review_pic_commit_time,t.check_time,t.status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover,t.buy_way,t.coupon_if,t.buyer_taobaoaccount_name,insert(t.seller_taobaoaccount_name,2,3,'***') seller_taobaoaccount_name,t.gift_express_co,t.buyer_mincredit,t.taobao_orderid from t_order t left join t_activity a on t.activity_id=a.id where t.id=?")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("complainFailReason", rs.getObject("complain_fail_reason"));
				item.put("couponUrl", rs.getObject("coupon_url"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("autoReturnTime", rs.getObject("auto_return_time"));
				item.put("rightprotectTime", rs.getObject("rightprotect_time"));
				item.put("complainProofPics", rs.getObject("complain_proof_pics"));
				item.put("complain", rs.getObject("complain"));
				item.put("complainExplanation", rs.getObject("complain_explanation"));
				item.put("complainReason", rs.getObject("complain_reason"));
				item.put("complainTime", rs.getObject("complain_time"));
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
				item.put("reviewPics", rs.getObject("review_pics"));
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

	@RequestMapping(value = "/complainorders")
	public void complainorders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
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
			if (complainStatus != null)
				sqlParams.add(complainStatus);
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
					"select t.complain_fail_reason,t.finished,t.complain_time,t.way_to_shop,t.coupon_if,t.buy_way,t.complain,t.id,t.pay_price,t.return_money,t.gift_name,t.gift_cover from t_order t where t.del=0 and t.buyer_id=?")
							.append(complainStatus == null ? " and t.complain in (1,2,6) " : " and t.complain=? ")
							.append(buyerNickname == null ? "" : " and t.buyer_taobaoaccount_name like ? ")
							.append(sellerNickname == null ? "" : " and t.seller_taobaoaccount_name like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(complainTimeStart == null ? "" : " and t.complain_time >= ? ")
							.append(complainTimeEnd == null ? "" : " and t.complain_time <= ? ")
							.append(" order by t.order_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("finished", rs.getObject("finished"));
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
				item.put("complainFailReason", rs.getObject("complain_fail_reason"));
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
					"select t.activity_id,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_status,t.rightprotect_reason,t.order_time,a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buy_way,t.coupon_if,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where 1=1 and t.buyer_id=?")
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
							.append(" order by t.order_time desc limit ?,? ").toString();
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
				item.put("activityId", rs.getObject("activity_id"));
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
				item.put("rightprotectReason", rs.getObject("rightprotect_reason"));
				item.put("rightprotectSellerProof", rs.getObject("rightprotect_seller_proof"));
				item.put("rightprotectBuyerProof", rs.getObject("rightprotect_buyer_proof"));
				item.put("rightprotectSellerAddkf", rs.getObject("rightprotect_seller_addkf"));
				item.put("rightprotectBuyerAddkf", rs.getObject("rightprotect_buyer_addkf"));
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

	@RequestMapping(value = "/rightprotectsorders/confirm")
	public void rightprotectsordersConfirm(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.rightprotect_status,t.return_money,t.seller_id from t_order t  where t.id=? and t.buyer_id=?  for update")
							.toString());
			pst.setObject(1, orderId);
			pst.setObject(2, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal returnMoney = null;
			String sellerId = null;
			if (rs.next()) {
				int rightprotectStatus = rs.getInt("rightprotect_status");
				if (rightprotectStatus == 0)
					throw new InteractRuntimeException("订单未维权");
				if (rightprotectStatus != 7)
					throw new InteractRuntimeException("状态错误不可确认");
				returnMoney = rs.getBigDecimal("return_money");
				sellerId = rs.getString("seller_id");
			} else
				throw new InteractRuntimeException("订单不存在");

			pst.close();

			BigDecimal unwithdrawMoney = null;
			pst = connection.prepareStatement(
					new StringBuilder("select t.unwithdraw_money from t_user t where t.id=? for update").toString());
			pst.setObject(1, sellerId);
			rs = pst.executeQuery();
			if (rs.next()) {
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set rightprotect_status=12,finished=1 where id=?").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_user set unwithdraw_money=unwithdraw_money+? where id=? ").toString());
			pst.setObject(1, returnMoney.add(new BigDecimal(2)));
			pst.setObject(2, sellerId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, sellerId);
			pst.setObject(3, returnMoney);
			pst.setObject(4, "维权成功，退还返现金额。订单尾号" + orderId.substring(orderId.length() - 5));
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
			pst.setObject(2, sellerId);
			pst.setObject(3, new BigDecimal(2));
			pst.setObject(4, "维权成功，退还核对手续费。订单尾号" + orderId.substring(orderId.length() - 5));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, orderId);
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

	@RequestMapping(value = "/rightprotectsorders/refuse")
	public void rightprotectsordersRefuse(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					new StringBuilder("select t.rightprotect_status from t_order t  where t.id=? for update")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int rightprotectStatus = rs.getInt("rightprotect_status");
				if (rightprotectStatus == 0)
					throw new InteractRuntimeException("订单未维权");
				if (rightprotectStatus != 7)
					throw new InteractRuntimeException("状态错误不可确认");
			}
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("update t_order set rightprotect_status=10 where id=?").toString());
			pst.setObject(1, orderId);
			int cnt = pst.executeUpdate();
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
					"update t_order set rightprotect_buyer_addkf_pics=?,rightprotect_buyer_addkf_describe=?,rightprotect_buyer_addkf=1 where id=? and buyer_id=? and rightprotect_status=10")
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
					"update t_order set rightprotect_buyer_proof_pics=?,rightprotect_buyer_proof_desc=?,rightprotect_buyer_proof=1 where id=? and buyer_id=? and rightprotect_status=10 and rightprotect_seller_addkf=1")
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
}