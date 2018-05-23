package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
					"select t.buyer_remind_check_if,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status,bt.nickname buyer_nickname,st.nickname seller_nickname from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where 1=1 ")
							.append(tradeStatus == null ? " t.status in (0,3,4,5,6) " : " t.status = ?")
							.append(taobaoOrderid == null ? "" : " and t.taobao_orderid like ? ")
							.append(buyerNickname == null ? "" : " and bt.nickname like ? ")
							.append(sellerNickname == null ? "" : " and st.nickname like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("buyerNickname", rs.getObject("buyer_nickname"));
				item.put("sellerNickname", rs.getObject("seller_nickname"));
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

	@RequestMapping(value = "/buyedorders")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String buyerProtectRightsStatusParam = StringUtils
					.trimToNull(request.getParameter("buyer_protect_rights_status"));
			Integer buyerProtectRightsStatus = buyerProtectRightsStatusParam == null ? null
					: Integer.parseInt(buyerProtectRightsStatusParam);
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
			if (buyerProtectRightsStatus != null)
				sqlParams.add(buyerProtectRightsStatus);
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
					"select t.review_pic,t.buyer_protect_rights_status,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status,bt.nickname buyer_nickname,st.nickname seller_nickname from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.status=1 ")
							.append(reviewPicCommitIf != null && reviewPicCommitIf == 0 ? ""
									: " and (ifnull(t.review_pic) or length(trim(t.review_pic))=0) ")
							.append(buyerProtectRightsStatus == null ? "" : " and t.buyer_protect_rights_status=? ")
							.append(buyerNickname == null ? "" : " and bt.nickname like ? ")
							.append(sellerNickname == null ? "" : " and st.nickname like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("buyerNickname", rs.getObject("buyer_nickname"));
				item.put("sellerNickname", rs.getObject("seller_nickname"));
				item.put("buyerProtectRightsStatus", rs.getObject("buyer_protect_rights_status"));
				String reviewPic = rs.getString("review_pic");
				item.put("review_pic_commit_if", reviewPic == null || reviewPic.isEmpty() ? 0 : 1);
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
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status,bt.nickname buyer_nickname,st.nickname seller_nickname from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.status=2 ")
							.append(buyerNickname == null ? "" : " and bt.nickname like ? ")
							.append(sellerNickname == null ? "" : " and st.nickname like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("buyerNickname", rs.getObject("buyer_nickname"));
				item.put("sellerNickname", rs.getObject("seller_nickname"));
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

	@RequestMapping(value = "/protectingrightsorders")
	public void protectingRightsOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String buyerProtectRightsStatusParam = StringUtils
					.trimToNull(request.getParameter("buyer_protect_rights_status"));
			Integer buyerProtectRightsStatus = buyerProtectRightsStatusParam == null ? null
					: Integer.parseInt(buyerProtectRightsStatusParam);
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
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.buyer_protect_rights_status,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status,bt.nickname buyer_nickname,st.nickname seller_nickname from t_order t left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where 1=1 ")
							.append(buyerProtectRightsStatus == null ? ""
									: " and t.buyer_protect_rights_status like ? ")
							.append(buyerNickname == null ? "" : " and bt.nickname like ? ")
							.append(sellerNickname == null ? "" : " and st.nickname like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
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
				item.put("orderId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("buyerNickname", rs.getObject("buyer_nickname"));
				item.put("sellerNickname", rs.getObject("seller_nickname"));
				item.put("buyerProtectRightsStatus", rs.getObject("buyer_protect_rights_status"));
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
}