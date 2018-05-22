package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

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

@Controller("plat.api.mobile.ActivityDetailEntrance")
@RequestMapping(value = "/p/m/actdetent")
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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select (select if(count(id)>0,1,0) from t_order where user_id=? and activity_id=t.id and status=0) apply_if,t.buyer_num,t.gift_cover,t.gift_name,t.pay_price,t.return_money,(t.publish_time+t.keep_days*24*60*60*1000) end_time,t.stock,t.buy_way,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buyer_mincredit_min,t.gift_express_co"
							+ (loginStatus == null ? ",null gift_detail" : ",t.gift_detail")
							+ " from t_activity t where t.id=?").toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, activityId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("endTime", rs.getLong("end_time"));
				item.put("stock", rs.getInt("stock"));
				item.put("buyerNum", rs.getInt("buyer_num"));
				item.put("buyWay", rs.getInt("buy_way"));
				item.put("couponIf", rs.getInt("coupon_if"));
				item.put("buyerMincreditMin", rs.getInt("buyer_mincredit_min"));
				item.put("giftExpressCo", rs.getString("gift_express_co"));
				item.put("giftDetail", rs.getString("gift_detail"));
				item.put("applyIf", rs.getString("apply_if"));
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

			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.taobao_user_nick from t_taobaoaccount t where t.user_id=? and t.type=1").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("taobaoAccountId", rs.getInt("id"));
				item.put("taobaoUserNick", rs.getString("taobao_user_nick"));
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
					"select (select if(count(id)>0,1,0) from t_order where buyer_id=? and activity_id=t.id and status=0) apply_if,t.start_time,t.keep_days,t.gift_express_co,t.buyer_mincredit_min,t.gift_cover,(select taobao_user_nick from t_taobaoaccount where id=? and type=1 and user_id=?) buyer_taobao_usernick,tbs.taobao_user_nick,t.taobaoaccount_id,t.user_id,t.gift_name,t.title,t.pay_price,t.return_money,t.buy_way,if((isnull(t.coupon_url)||length(trim(t.coupon_url))=0),0,1) coupon_if from t_activity t inner join t_taobaoaccount tbs on t.taobaoaccount_id=tbs.id where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, buyerTaobaoaccountId);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, activityId);
			ResultSet rs = pst.executeQuery();
			String orderId = null;
			if (rs.next()) {
				long startTime = rs.getLong("start_time");
				int keepDays = rs.getInt("keep_days");
				if (new Date().getTime() > (startTime + keepDays * 24 * 60 * 60 * 1000l))
					throw new InteractRuntimeException("活动已结束");
				int applyIf = rs.getInt("apply_if");
				if (applyIf == 1)
					throw new InteractRuntimeException("您已申请过");
				int sellerTaobaoAccountId = rs.getInt("taobaoaccount_id");
				int buyerMincreditMin = rs.getInt("buyer_mincredit_min");
				String giftExpressCo = rs.getString("gift_express_co");
				int couponIf = rs.getInt("coupon_if");
				int buyWay = rs.getInt("buy_way");
				String sellerUserId = rs.getString("user_id");
				String giftName = rs.getString("gift_name");
				String title = rs.getString("title");
				String giftCover = rs.getString("gift_cover");
				String sellerTaobaoUserNick = rs.getString("taobao_user_nick");
				String buyerTaobaoUsernick = rs.getString("buyer_taobao_usernick");
				BigDecimal payPrice = rs.getBigDecimal("pay_price");
				BigDecimal returnMoney = rs.getBigDecimal("return_money");
				pst.close();

				if (StringUtils.isEmpty(buyerTaobaoUsernick))
					throw new InteractRuntimeException("买家淘宝号错误");

				orderId = RandomStringUtils.randomNumeric(12);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_order (id,seller_id,seller_taobaoaccount_id,buyer_id,buyer_taobaoaccount_id,activity_id,gift_name,activity_title,pay_price,return_money,order_time,seller_taobaoaccount_name,buyer_taobaoaccount_name,buy_way,coupon_if,gift_cover,gift_express_co,buyer_mincredit_min) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
								.toString());
				pst.setObject(1, orderId);
				pst.setObject(2, sellerUserId);
				pst.setObject(3, sellerTaobaoAccountId);
				pst.setObject(4, loginStatus.getUserId());
				pst.setObject(5, buyerTaobaoaccountId);
				pst.setObject(6, activityId);
				pst.setObject(7, giftName);
				pst.setObject(8, title);
				pst.setObject(9, payPrice);
				pst.setObject(10, returnMoney);
				pst.setObject(11, new Date().getTime());
				pst.setObject(12, sellerTaobaoUserNick);
				pst.setObject(13, buyerTaobaoUsernick);
				pst.setObject(14, buyWay);
				pst.setObject(15, couponIf);
				pst.setObject(16, giftCover);
				pst.setObject(17, giftExpressCo);
				pst.setObject(18, buyerMincreditMin);

				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			} else
				throw new InteractRuntimeException("活动不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("orderId", orderId);
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

	@RequestMapping(value = "/getcouponurl")
	public void getcouponurl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityId = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityId == null)
				throw new InteractRuntimeException("activity_id 不能空");
			String sellerTaobaoname = StringUtils.trimToNull(request.getParameter("seller_taobaoname"));
			if (sellerTaobaoname == null)
				throw new InteractRuntimeException("seller_taobaoname 不能空");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select tb.taobao_user_nick,t.coupon_url from t_activity t inner join t_taobaoaccount tb on tb.id=t.taobaoaccount_id where t.id=?")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			String couponUrl = null;
			if (rs.next()) {
				String sellerTaobaoaccountName = rs.getString("taobao_user_nick");
				if (!sellerTaobaoname.equals(sellerTaobaoaccountName))
					throw new InteractRuntimeException("卖家名校验失败");
				couponUrl = rs.getString("coupon_url");
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("couponUrl", couponUrl);
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