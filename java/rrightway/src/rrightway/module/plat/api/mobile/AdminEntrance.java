package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
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

@Controller("plat.api.mobile.AdminEntrance")
@RequestMapping(value = "/p/m/adminent")
public class AdminEntrance {

	public static Logger logger = Logger.getLogger(AdminEntrance.class);

	@RequestMapping(value = "/activity/list")
	public void activities(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String buyerMincreditMinParam = StringUtils.trimToNull(request.getParameter("buyer_mincredit_min"));
			Integer buyerMincreditMin = buyerMincreditMinParam == null ? null
					: Integer.parseInt(buyerMincreditMinParam);
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
			}
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (type1Id != null)
				sqlParams.add(type1Id);
			if (type2Id != null)
				sqlParams.add(type2Id);
			if (wayToShop != null)
				sqlParams.add(wayToShop);
			if (buyerMincreditMin != null)
				sqlParams.add(buyerMincreditMin);
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
					"select status,id,gift_cover,gift_name,pay_price,return_money from t_activity where 1=1 ")
							.append(couponIf == null ? ""
									: couponIf == 0 ? " and (isnull(coupon_url) or length(trim(coupon_url))=0) "
											: "and (!isnull(coupon_url) and length(trim(coupon_url))>0) ")
							.append(keyw == null ? "" : " and gift_name like ? ")
							.append(buyWay == null ? "" : " and buy_way=? ")
							.append(type1Id == null ? "" : " and gift_type1_id=? ")
							.append(type2Id == null ? "" : " and gift_type2_id=? ")
							.append(wayToShop == null ? "" : " and way_to_shop=? ")
							.append(buyerMincreditMin == null ? "" : " and buyer_mincredit >= ? ")
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
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activities = new JSONObject();
			activities.put("items", items);
			data.put("activities", activities);
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
	public void detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select  tb.taobao_user_nick,u.phone,u.username,t.gift_pics,t.title,t.publish_time,t.way_to_shop,t.qrcode_to_order,t.search_keys,t.cowry_url,t.cowry_cover,t.buy_way,t.coupon_url,t.pay_price,t.return_money,t.buyer_mincredit_min,t.keep_days,t.stock,t.start_time,t.gift_name,t.gift_type1_name,t.gift_type2_name,t.gift_url,t.gift_cover,t.gift_detail,t.gift_express_co,t.status,t.audit_fail_reason,t.buyer_num from t_activity t inner join t_user u on t.user_id=u.id inner join t_taobaoaccount tb on tb.id=t.taobaoaccount_id where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, activityId);
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
				item.put("buyerMincreditMin", rs.getObject("buyer_mincredit_min"));
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

	@RequestMapping(value = "/auditsuccess")
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

			pst = connection
					.prepareStatement(new StringBuilder("update t_activity set status=1 where id=?").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/auditfail")
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
}