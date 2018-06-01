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

@Controller("plat.api.mobile.AdminEntrance")
@RequestMapping(value = "/p/m/admin")
public class AdminEntrance {

	public static Logger logger = Logger.getLogger(AdminEntrance.class);

	@RequestMapping(value = "/activity/ent")
	public void activityEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String keyw = StringUtils.trimToNull(request.getParameter("keyw"));
			String type2idParam = StringUtils.trimToNull(request.getParameter("type2_id"));
			Integer type2Id = type2idParam == null ? null : Integer.parseInt(type2idParam);
			String type1idParam = StringUtils.trimToNull(request.getParameter("type1_id"));
			Integer type1Id = type1idParam == null ? null : Integer.parseInt(type1idParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			Integer wayToShop = wayToShopParam == null ? null : Integer.parseInt(wayToShopParam);
			String buyerMincreditParam = StringUtils.trimToNull(request.getParameter("buyer_mincredit"));
			Integer buyerMincredit = buyerMincreditParam == null ? null : Integer.parseInt(buyerMincreditParam);
			String payPriceMinParam = StringUtils.trimToNull(request.getParameter("pay_price_min"));
			BigDecimal payPriceMin = payPriceMinParam == null ? null : new BigDecimal(payPriceMinParam);
			String payPriceMaxParam = StringUtils.trimToNull(request.getParameter("pay_price_max"));
			BigDecimal payPriceMax = payPriceMaxParam == null ? null : new BigDecimal(payPriceMaxParam);
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);

			// 1综合 2销量 3最新 4付款金额升序 5付款金额降序 6奖金升序 7奖金降序
			String sortbyParam = StringUtils.trimToNull(request.getParameter("sortby"));
			int sortby = sortbyParam == null ? 1 : Integer.parseInt(sortbyParam);
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
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			List sqlParams = new ArrayList();
			if (keyw != null) {
				keyw = new StringBuilder("%").append(keyw).append("%").toString();
				sqlParams.add(keyw);
				sqlParams.add(keyw);
			}
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (type1Id != null)
				sqlParams.add(type1Id);
			if (type2Id != null)
				sqlParams.add(type2Id);
			if (wayToShop != null)
				sqlParams.add(wayToShop);
			if (buyerMincredit != null)
				sqlParams.add(buyerMincredit);
			if (payPriceMin != null)
				sqlParams.add(payPriceMin);
			if (payPriceMax != null)
				sqlParams.add(payPriceMax);
			if (status != null)
				sqlParams.add(status);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select title,publish_time,status,id,gift_cover,gift_name,pay_price,return_money from t_activity where 1=1 ")
							.append(couponIf == null ? ""
									: couponIf == 0 ? " and (isnull(coupon_url) or length(trim(coupon_url))=0) "
											: "and (!isnull(coupon_url) and length(trim(coupon_url))>0) ")
							.append(keyw == null ? "" : " and (gift_name like ? or title like ?) ")
							.append(buyWay == null ? "" : " and buy_way=? ")
							.append(type1Id == null ? "" : " and gift_type1_id=? ")
							.append(type2Id == null ? "" : " and gift_type2_id=? ")
							.append(wayToShop == null ? "" : " and way_to_shop=? ")
							.append(buyerMincredit == null ? "" : " and buyer_mincredit >= ? ")
							.append(payPriceMin == null ? "" : " and pay_price >= ? ")
							.append(payPriceMax == null ? "" : " and pay_price <= ? ")
							.append(status == null ? "" : " and status = ? ")
							.append(sortby == 1 || sortby == 2 ? " order by buyer_num desc "
									: sortby == 3 ? " order by publish_time desc "
											: sortby == 4 ? " order by pay_price asc "
													: sortby == 5 ? " order by pay_price desc "
															: sortby == 6 ? " order by return_money-pay_price asc "
																	: sortby == 7
																			? " order by return_money-pay_price desc "
																			: " order by buyer_num desc ")
							.append(" limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getInt("id"));
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("status", rs.getObject("status"));
				item.put("title", rs.getObject("title"));
				item.put("publishTime", rs.getObject("publish_time"));
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

	@RequestMapping(value = "/activity/detail")
	public void activityDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("acitivy_id"));
			if (activityIdParam == null)
				throw new InteractRuntimeException("acitivy_id 不能空");
			int activityId = Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select  t.huabei_pay,t.creditcard_pay,tb.taobao_user_nick,u.phone,u.username,t.gift_pics,t.title,t.publish_time,t.way_to_shop,t.qrcode_to_order,t.search_keys,t.cowry_url,t.cowry_cover,t.buy_way,t.coupon_url,t.pay_price,t.return_money,t.buyer_mincredit,t.keep_days,t.stock,t.start_time,t.gift_name,t.gift_type1_name,t.gift_type2_name,t.gift_url,t.gift_cover,t.gift_detail,t.gift_express_co,t.status,t.audit_fail_reason,t.buyer_num from t_activity t inner join t_user u on t.user_id=u.id inner join t_taobaoaccount tb on tb.id=t.taobaoaccount_id where t.id=?")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("taobaoUserNick", rs.getObject("taobao_user_nick"));
				item.put("phone", rs.getObject("phone"));
				item.put("username", rs.getObject("username"));
				item.put("giftPics", rs.getObject("gift_pics"));
				item.put("title", rs.getObject("title"));
				item.put("publishTime", rs.getObject("publish_time"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("qrcodeToOrder", rs.getObject("qrcode_to_order"));
				item.put("searchKeys", rs.getObject("search_keys"));
				item.put("cowryUrl", rs.getObject("cowry_url"));
				item.put("cowryCover", rs.getObject("cowry_cover"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("couponUrl", rs.getObject("coupon_url"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("buyerMincredit", rs.getObject("buyer_mincredit"));
				item.put("keepDays", rs.getObject("keep_days"));
				item.put("stock", rs.getObject("stock"));
				item.put("startTime", rs.getObject("start_time"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftType1Name", rs.getObject("gift_type1_name"));
				item.put("giftType2Name", rs.getObject("gift_type2_name"));
				item.put("giftUrl", rs.getObject("gift_url"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("giftDetail", rs.getObject("gift_detail"));
				item.put("giftExpressCo", rs.getObject("gift_express_co"));
				item.put("status", rs.getObject("status"));
				item.put("auditFailReason", rs.getObject("audit_fail_reason"));
				item.put("buyerNum", rs.getObject("buyer_num"));
				item.put("giftPics", rs.getObject("gift_pics"));
				item.put("huabeiPay", rs.getObject("huabei_pay"));
				item.put("creditcardPay", rs.getObject("creditcard_pay"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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

	@RequestMapping(value = "/activity/auditsuccess")
	public void auditsuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("acitivy_id"));
			if (activityIdParam == null)
				throw new InteractRuntimeException("acitivy_id 不能空");
			int activityId = Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set status=3,start_time=? where id=?").toString());
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, activityId);
			int cnt = pst.executeUpdate();
			pst.close();
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

	@RequestMapping(value = "/activity/auditfail")
	public void auditfail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("acitivy_id"));
			if (activityIdParam == null)
				throw new InteractRuntimeException("acitivy_id 不能空");
			int activityId = Integer.parseInt(activityIdParam);
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不能空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set status=2,audit_fail_reason=? where id=?").toString());
			pst.setObject(1, reason);
			pst.setObject(2, activityId);
			int cnt = pst.executeUpdate();
			pst.close();
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

	@RequestMapping(value = "/order/ent")
	public void orderEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String rightprotectStatusParam = StringUtils.trimToNull(request.getParameter("rightprotect_status"));
			Integer rightprotectStatus = rightprotectStatusParam == null ? null
					: Integer.parseInt(rightprotectStatusParam);
			String complainParam = StringUtils.trimToNull(request.getParameter("complain"));
			Integer complain = complainParam == null ? null : Integer.parseInt(complainParam);
			String finishedParam = StringUtils.trimToNull(request.getParameter("finished"));
			Integer finished = finishedParam == null ? null : Integer.parseInt(finishedParam);
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
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (status != null)
				sqlParams.add(status);
			if (rightprotectStatus != null)
				sqlParams.add(rightprotectStatus);
			if (complain != null)
				sqlParams.add(complain);
			if (finished != null)
				sqlParams.add(finished);
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
					"select t.complain,t.review_pic_audit,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_status,t.order_time,a.huabei_pay,a.creditcard_pay,t.way_to_shop,t.gift_cover,t.buy_way,t.coupon_if,t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status from t_order t left join t_activity a on t.activity_id=a.id left join t_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_taobaoaccount st on t.seller_taobaoaccount_id=st.id where 1=1")
							.append(status == null ? "" : " and t.status=?")
							.append(rightprotectStatus == null ? "" : " and t.rightprotect_status=?")
							.append(complain == null ? "" : " and t.complain=?")
							.append(finished == null ? "" : " and t.finished=?")
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
				item.put("complain", rs.getObject("complain"));
				item.put("reviewPicAudit", rs.getObject("review_pic_audit"));
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

	@RequestMapping(value = "/order/detail")
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
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select t.complain,t.rightprotect_seller_proof,t.rightprotect_buyer_proof,t.rightprotect_seller_addkf,t.rightprotect_buyer_addkf,t.rightprotect_seller_proof_desc,t.rightprotect_buyer_proof_desc,t.rightprotect_seller_proof_pics,t.rightprotect_buyer_proof_pics,t.rightprotect_buyer_addkf_describe,t.rightprotect_buyer_addkf_pics,t.rightprotect_seller_addkf_pics,t.rightprotect_seller_addkf_describe,t.rightprotect_reason,t.rightprotect_status,t.review_pics,t.auto_return_time,t.activity_title,t.buyer_remind_check_if,a.coupon_url,t.gift_express_co,t.buyer_cancel_reason,t.seller_cancel_reason,t.way_to_shop,t.activity_id,t.review_pic_audit,t.review_pic_commit_time,t.check_time,t.status,t.id,t.order_time,t.pay_price,t.return_money,t.gift_name,t.gift_cover,t.buy_way,t.coupon_if,t.buyer_taobaoaccount_name,t.seller_taobaoaccount_name,t.gift_express_co,t.buyer_mincredit,t.taobao_orderid from t_order t  left join t_activity a on t.activity_id=a.id where t.id=?")
							.toString());
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
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
				item.put("giftExpressCo", rs.getObject("gift_express_co"));
				item.put("buyerMincredit", rs.getObject("buyer_mincredit"));
				item.put("taobaoOrderid", rs.getObject("taobao_orderid"));
				item.put("status", rs.getObject("status"));
				item.put("sellerCancelReason", rs.getObject("seller_cancel_reason"));
				item.put("buyerCancelReason", rs.getObject("buyer_cancel_reason"));
				item.put("autoReturnTime", rs.getObject("auto_return_time"));
				item.put("complain", rs.getObject("complain"));
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

	@RequestMapping(value = "/user/ent")
	public void userEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
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
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (status != null)
				sqlParams.add(status);
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (username != null)
				sqlParams.add(new StringBuilder("%").append(username).append("%").toString());
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			if (realname != null)
				sqlParams.add(new StringBuilder("%").append(realname).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);

			String sql = new StringBuilder(
					"select distinct t.id,t.status,(t.right_wallet_unoutable+t.right_wallet_outable) wallet,(t.withdrawable_money+t.unwithdraw_money+t.frozen_money) money,t.username,t.phone,t.realname from t_user t left join t_taobaoaccount bt on t.id=bt.user_id left join t_taobaoaccount st on t.id=st.user_id where 1=1")
							.append(status == null ? "" : " and t.status=?")
							.append(buyerNickname == null ? "" : " and bt.taobao_user_nick like ? ")
							.append(sellerNickname == null ? "" : " and st.taobao_user_nick like ? ")
							.append(username == null ? "" : " and t.username like ? ")
							.append(phone == null ? "" : " and t.phone like ? ")
							.append(realname == null ? "" : " and t.realname like ? ")
							.append(" order by t.register_time desc limit ?,? ").toString();
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
				item.put("id", rs.getObject("id"));
				item.put("status", rs.getObject("status"));
				item.put("money", rs.getObject("money"));
				item.put("wallet", rs.getObject("wallet"));
				item.put("username", rs.getObject("username"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
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

	@RequestMapping(value = "/user/detail")
	public void userdetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.status,(t.right_wallet_unoutable+t.right_wallet_outable) wallet,(t.withdrawable_money+t.unwithdraw_money+t.frozen_money) money,t.username,t.phone,t.realname,t.qq,t.withdrawable_money,t.unwithdraw_money,t.frozen_money,t.right_wallet_unoutable,t.right_wallet_outable,t.wallet_profit,t.register_time from t_user t where t.id=?")
							.toString());
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("id", rs.getObject("id"));
				item.put("status", rs.getObject("status"));
				item.put("wallet", rs.getObject("wallet"));
				item.put("money", rs.getObject("money"));
				item.put("username", rs.getObject("username"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("qq", rs.getObject("qq"));
				item.put("withdrawable_money", rs.getObject("withdrawable_money"));
				item.put("unwithdraw_money", rs.getObject("unwithdraw_money"));
				item.put("frozen_money", rs.getObject("frozen_money"));
				item.put("right_wallet_unoutable", rs.getObject("right_wallet_unoutable"));
				item.put("right_wallet_outable", rs.getObject("right_wallet_outable"));
				item.put("wallet_profit", rs.getObject("wallet_profit"));
				item.put("register_time", rs.getObject("register_time"));
			} else
				throw new InteractRuntimeException("用户不存在");

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

	@RequestMapping(value = "/user/freeze")
	public void userFreeze(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder("update t_user set status=1 where id=?").toString());
			pst.setObject(1, userId);
			int n = pst.executeUpdate();

			pst.close();
			if (n != 1)
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

	@RequestMapping(value = "/user/unfreeze")
	public void userUnfreeze(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAdminIf() != 1)
				throw new InteractRuntimeException("您不是管理员");

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder("update t_user set status=0 where id=?").toString());
			pst.setObject(1, userId);
			int n = pst.executeUpdate();

			pst.close();
			if (n != 1)
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
}