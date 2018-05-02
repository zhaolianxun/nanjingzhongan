package rrightway.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

@Controller("plat.api.ActivityDetailEntrance")
@RequestMapping(value = "/p/actdetent")
public class ActivityDetailEntrance {

	public static Logger logger = Logger.getLogger(ActivityDetailEntrance.class);

	@RequestMapping(value = "/detail")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.gift_cover,t.gift_name,t.pay_price,t.return_money,t.publish_time+t.keep_days*24*60*60*1000 end_time,t.stock,t.buy_way,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buyer_mincredit_min,t.gift_express_co,t.gift_detail from t_activity t where t.id=?")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("activityId", rs.getInt("id"));
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("endTime", rs.getLong("end_time"));
				item.put("stock", rs.getInt("stock"));
				item.put("buyWay", rs.getInt("buy_way"));
				item.put("couponIf", rs.getInt("coupon_if"));
				item.put("buyerMincreditMin", rs.getInt("buyer_mincredit_min"));
				item.put("giftExpressCo", rs.getString("gift_express_co"));
				item.put("giftDetail", rs.getString("gift_detail"));
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

	@RequestMapping(value = "/buyertaobaoaccounts")
	public void buyertaobaoaccounts(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("select t.id,t.nickname from t_buyer_taobaoaccount t where t.user_id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("taobaoAccountId", rs.getInt("id"));
				item.put("nickname", rs.getString("nickname"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject taobaoAccounts = new JSONObject();
			taobaoAccounts.put("items", items);
			data.put("taobaoAccounts", taobaoAccounts);
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

	@RequestMapping(value = "/apply")
	public void apply(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("acitivy_id"));
			if (activityIdParam == null)
				throw new InteractRuntimeException("acitivy_id 不能空");
			int activityId = Integer.parseInt(activityIdParam);
			String buyerTaobaoaccountIdParam = StringUtils.trimToNull(request.getParameter("buyer_taobaoaccount_id"));
			if (buyerTaobaoaccountIdParam == null)
				throw new InteractRuntimeException("buyer_taobaoaccount_id 不能空");
			int buyerTaobaoaccountId = Integer.parseInt(buyerTaobaoaccountIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.gift_cover,t.gift_name,t.pay_price,t.return_money,t.publish_time+t.keep_days*24*60*60*1000 end_time,t.stock,t.buy_way,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buyer_mincredit_min,t.gift_express_co,t.gift_detail from t_activity t where t.id=?")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("activityId", rs.getInt("id"));
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("endTime", rs.getLong("end_time"));
				item.put("stock", rs.getInt("stock"));
				item.put("buyWay", rs.getInt("buy_way"));
				item.put("couponIf", rs.getInt("coupon_if"));
				item.put("buyerMincreditMin", rs.getInt("buyer_mincredit_min"));
				item.put("giftExpressCo", rs.getString("gift_express_co"));
				item.put("giftDetail", rs.getString("gift_detail"));
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
}