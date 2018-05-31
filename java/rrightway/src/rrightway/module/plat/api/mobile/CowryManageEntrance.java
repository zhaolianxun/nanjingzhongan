package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.CowryManageEntrance")
@RequestMapping(value = "/p/m/cowrymanageent")
public class CowryManageEntrance {

	public static Logger logger = Logger.getLogger(CowryManageEntrance.class);

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
					"select (select count(id) from t_activity where user_id=? and status in (0,2)) auditingCount,(select count(id) from t_activity where user_id=? and status=1) inactCount,(select count(id) from t_activity where user_id=? and status=3) instoreCount")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("auditingCount", rs.getInt("auditingCount"));
				item.put("inactCount", rs.getInt("inactCount"));
				item.put("instoreCount", rs.getInt("instoreCount"));
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

	@RequestMapping(value = "/publishent/ent")
	public void publishentEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id,name from t_good_type where level=1");
			ResultSet rs = pst.executeQuery();
			JSONArray type1s = new JSONArray();
			while (rs.next()) {
				JSONObject type1 = new JSONObject();
				type1.put("id", rs.getObject("id"));
				type1.put("name", rs.getObject("name"));
				type1s.add(type1);
			}
			pst.close();

			pst = connection
					.prepareStatement("select id,taobao_user_nick from t_taobaoaccount where user_id=? and type=2");
			pst.setObject(1, loginStatus.getUserId());
			rs = pst.executeQuery();
			JSONArray sellerTaobaoUserNicks = new JSONArray();
			while (rs.next()) {
				JSONObject sellerTaobaoUserNick = new JSONObject();
				sellerTaobaoUserNick.put("id", rs.getObject("id"));
				sellerTaobaoUserNick.put("sellerTaobaoUserNick", rs.getObject("taobao_user_nick"));
				sellerTaobaoUserNicks.add(sellerTaobaoUserNick);
			}
			pst.close();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1s", type1s);
			data.put("sellerTaobaoUserNicks", sellerTaobaoUserNicks);
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

	@RequestMapping(value = "/publishent/gettaobaocrowycover")
	public void publishentEntGettaobaocrowycover(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			// 获取请求参数
			String crowyUrl = StringUtils.trimToNull(request.getParameter("crowy_url"));
			if (crowyUrl == null)
				throw new InteractRuntimeException("crowy_url 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			List<String> detailPics = new ArrayList<String>();
			String cover = null;
			try {
				Document mydoc = Jsoup.parse(new URL(crowyUrl), 30000);
				List<String> pics = mydoc.getElementById("J_UlThumb").getElementsByTag("img").eachAttr("data-src");
				if (!pics.isEmpty()) {
					cover = pics.get(0).replaceAll("50x50", "400x400");
					pics.remove(0);
					if (pics.size() > 1) {
						for (int i = 0; i < pics.size(); i++) {
							detailPics.add(pics.get(i).replaceAll("50x50", "400x400"));
						}
					}
				}

			} catch (Exception e) {

			}
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("cover", cover);
			data.put("detailPics", detailPics);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/publishent/publish")
	public void publish(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityTitle = StringUtils.trimToNull(request.getParameter("activity_title"));
			if (activityTitle == null)
				throw new InteractRuntimeException("activity_title 不能空");
			String taobaoaccountIdParam = StringUtils.trimToNull(request.getParameter("taobaoaccount_id"));
			if (taobaoaccountIdParam == null)
				throw new InteractRuntimeException("taobaoaccount_id 不能空");
			int taobaoaccountId = Integer.parseInt(taobaoaccountIdParam);
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			if (buyWayParam == null)
				throw new InteractRuntimeException("buy_way 不能空");
			int buyWay = Integer.parseInt(buyWayParam);
			String stockParam = StringUtils.trimToNull(request.getParameter("stock"));
			if (stockParam == null)
				throw new InteractRuntimeException("stock 不能空");
			int stock = Integer.parseInt(stockParam);
			if (stock <= 0 || stock > 100) {
				throw new InteractRuntimeException("参与人数只能在1-100之间");
			}
			String couponUrl = StringUtils.trimToNull(request.getParameter("coupon_url"));
			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			if (wayToShopParam == null)
				throw new InteractRuntimeException("way_to_shop 不能空");
			int wayToShop = Integer.parseInt(wayToShopParam);
			String qrcodeToOrder = StringUtils.trimToNull(request.getParameter("qrcode_to_order"));
			if (wayToShop == 3 && qrcodeToOrder == null) {
				throw new InteractRuntimeException("qrcode_to_order 不能空");
			}
			if (wayToShop == 3 && buyWay == 1)
				throw new InteractRuntimeException("扫码只能选择手机下单");

			String searchKeys = StringUtils.trimToNull(request.getParameter("search_keys"));
			String cowryUrl = StringUtils.trimToNull(request.getParameter("cowry_url"));
			String cowryCover = StringUtils.trimToNull(request.getParameter("cowry_cover"));
			if ((wayToShop == 1 || wayToShop == 4) && searchKeys == null)
				throw new InteractRuntimeException("search_keys 不能空");
			if ((wayToShop == 1 || wayToShop == 2 || wayToShop == 4) && cowryUrl == null)
				throw new InteractRuntimeException("cowry_url 不能空");
			if ((wayToShop == 1 || wayToShop == 4) && cowryCover == null)
				throw new InteractRuntimeException("cowry_cover 不能空");
			String payPriceParam = StringUtils.trimToNull(request.getParameter("pay_price"));
			if (payPriceParam == null)
				throw new InteractRuntimeException("pay_price 不能空");
			BigDecimal payPrice = new BigDecimal(payPriceParam);
			if (payPrice.floatValue() > 800) {
				throw new InteractRuntimeException("付款金额不可大于800");
			}
			String returnMoneyParam = StringUtils.trimToNull(request.getParameter("return_money"));
			if (returnMoneyParam == null)
				throw new InteractRuntimeException("return_money 不能空");
			BigDecimal returnMoney = new BigDecimal(returnMoneyParam);
			if (returnMoney.compareTo(payPrice) == -1
					|| returnMoney.subtract(payPrice).compareTo(new BigDecimal(20)) == 1)
				throw new InteractRuntimeException("不得低于‘付款金额’，不得高于‘付款金额’20元");

			String buyerMincreditParam = StringUtils.trimToNull(request.getParameter("buyer_mincredit"));
			if (buyerMincreditParam == null)
				throw new InteractRuntimeException("buyer_mincredit 不能空");
			int buyerMincredit = Integer.parseInt(buyerMincreditParam);

			String keepDaysParam = StringUtils.trimToNull(request.getParameter("keep_days"));
			if (keepDaysParam == null)
				throw new InteractRuntimeException("keep_days 不能空");
			int keepDays = Integer.parseInt(keepDaysParam);
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			if (giftName == null)
				throw new InteractRuntimeException("gift_name 不能空");
			String giftType1IdParam = StringUtils.trimToNull(request.getParameter("gift_type1_id"));
			if (giftType1IdParam == null)
				throw new InteractRuntimeException("gift_type1_id 不能空");
			int giftType1Id = Integer.parseInt(giftType1IdParam);
			String giftType2IdParam = StringUtils.trimToNull(request.getParameter("gift_type2_id"));
			if (giftType2IdParam == null)
				throw new InteractRuntimeException("gift_type2_id 不能空");
			int giftType2Id = Integer.parseInt(giftType2IdParam);
			String giftType1Name = StringUtils.trimToNull(request.getParameter("gift_type1_name"));
			if (giftType1Name == null)
				throw new InteractRuntimeException("gift_type1_name 不能空");
			String giftType2Name = StringUtils.trimToNull(request.getParameter("gift_type2_name"));
			if (giftType2Name == null)
				throw new InteractRuntimeException("gift_type2_name 不能空");
			String giftUrl = StringUtils.trimToNull(request.getParameter("gift_url"));
			String giftCover = StringUtils.trimToNull(request.getParameter("gift_cover"));
			if (giftCover == null)
				throw new InteractRuntimeException("gift_cover 不能空");
			String giftPics = StringUtils.trimToNull(request.getParameter("gift_pics"));
			String giftDetail = StringUtils.trimToNull(request.getParameter("gift_detail"));
			if (giftDetail == null && giftPics == null)
				throw new InteractRuntimeException("gift_detail和giftPics 至少传1个");
			String giftExpressCo = StringUtils.trimToNull(request.getParameter("gift_express_co"));
			if (giftExpressCo == null)
				throw new InteractRuntimeException("gift_express_co 不能空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"INSERT INTO `t_activity` (`user_id`, `taobaoaccount_id`, `title`, `publish_time`, `way_to_shop`, `qrcode_to_order`, `search_keys`, `cowry_url`, `cowry_cover`,`buy_way`, `coupon_url`,`pay_price`, `return_money`, `buyer_mincredit`, `keep_days`, `gift_name`, `gift_type1_id`, `gift_type1_name`, `gift_type2_id`, `gift_type2_name`, `gift_url`, `gift_cover`, `gift_detail`, `gift_express_co`,`stock`,`gift_pics`,`start_time`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);",
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, taobaoaccountId);
			pst.setObject(3, activityTitle);
			pst.setObject(4, new Date().getTime());
			pst.setObject(5, wayToShop);
			pst.setObject(6, qrcodeToOrder);
			pst.setObject(7, searchKeys);
			pst.setObject(8, cowryUrl);
			pst.setObject(9, cowryCover);
			pst.setObject(10, buyWay);
			pst.setObject(11, couponUrl);
			pst.setObject(12, payPrice);
			pst.setObject(13, returnMoney);
			pst.setObject(14, buyerMincredit);
			pst.setObject(15, keepDays);
			pst.setObject(16, giftName);
			pst.setObject(17, giftType1Id);
			pst.setObject(18, giftType1Name);
			pst.setObject(19, giftType2Id);
			pst.setObject(20, giftType2Name);
			pst.setObject(21, giftUrl);
			pst.setObject(22, giftCover);
			pst.setObject(23, giftDetail);
			pst.setObject(24, giftExpressCo);
			pst.setObject(25, stock);
			pst.setObject(26, giftPics);
			pst.setObject(27, new Date().getTime());

			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int activityId = rs.getInt(1);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("activityId", activityId);
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

	@RequestMapping(value = "/republish")
	public void republish(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityIdParam == null)
				throw new InteractRuntimeException("activity_id 不能空");
			int activityId = Integer.parseInt(activityIdParam);
			String activityTitle = StringUtils.trimToNull(request.getParameter("activity_title"));
			String taobaoaccountIdParam = StringUtils.trimToNull(request.getParameter("taobaoaccount_id"));
			Integer taobaoaccountId = taobaoaccountIdParam == null ? null : Integer.parseInt(taobaoaccountIdParam);
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);
			String stockParam = StringUtils.trimToNull(request.getParameter("stock"));
			Integer stock = stockParam == null ? null : Integer.parseInt(stockParam);
			String couponUrl = StringUtils.trimToNull(request.getParameter("coupon_url"));

			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			Integer wayToShop = wayToShopParam == null ? null : Integer.parseInt(wayToShopParam);
			String qrcodeToOrder = StringUtils.trimToNull(request.getParameter("qrcode_to_order"));
			if (wayToShop != null && wayToShop == 3 && qrcodeToOrder == null)
				throw new InteractRuntimeException("qrcode_to_order 不能空");
			String searchKeys = StringUtils.trimToNull(request.getParameter("search_keys"));
			String cowryUrl = StringUtils.trimToNull(request.getParameter("cowry_url"));
			String cowryCover = StringUtils.trimToNull(request.getParameter("cowry_cover"));
			if (wayToShop != null && (wayToShop == 1 || wayToShop == 4) && searchKeys == null)
				throw new InteractRuntimeException("search_keys 不能空");
			if (wayToShop != null && (wayToShop == 1 || wayToShop == 2 || wayToShop == 4) && cowryUrl == null)
				throw new InteractRuntimeException("cowry_url 不能空");
			if (wayToShop != null && (wayToShop == 1 || wayToShop == 4) && cowryCover == null)
				throw new InteractRuntimeException("cowry_cover 不能空");

			String payPriceParam = StringUtils.trimToNull(request.getParameter("pay_price"));
			BigDecimal payPrice = payPriceParam == null ? null : new BigDecimal(payPriceParam);

			String returnMoneyParam = StringUtils.trimToNull(request.getParameter("return_money"));
			BigDecimal returnMoney = returnMoneyParam == null ? null : new BigDecimal(returnMoneyParam);
			if (returnMoney != null && (returnMoney.compareTo(payPrice) == -1
					|| returnMoney.subtract(payPrice).compareTo(new BigDecimal(20)) == 1))
				throw new InteractRuntimeException("不得低于‘付款金额’，不得高于‘付款金额’20元");

			String buyerMincreditParam = StringUtils.trimToNull(request.getParameter("buyer_mincredit"));
			Integer buyerMincredit = buyerMincreditParam == null ? null : Integer.parseInt(buyerMincreditParam);

			String keepDaysParam = StringUtils.trimToNull(request.getParameter("keep_days"));
			Integer keepDays = keepDaysParam == null ? null : Integer.parseInt(keepDaysParam);

			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));

			String giftType1IdParam = StringUtils.trimToNull(request.getParameter("gift_type1_id"));
			Integer giftType1Id = giftType1IdParam == null ? null : Integer.parseInt(giftType1IdParam);

			String giftType2IdParam = StringUtils.trimToNull(request.getParameter("gift_type2_id"));
			Integer giftType2Id = giftType2IdParam == null ? null : Integer.parseInt(giftType2IdParam);

			String giftType1Name = StringUtils.trimToNull(request.getParameter("gift_type1_name"));
			if (giftType1Id != null && giftType1Name == null)
				throw new InteractRuntimeException("gift_type1_name 不能空");
			String giftType2Name = StringUtils.trimToNull(request.getParameter("gift_type2_name"));
			if (giftType2Id != null && giftType2Name == null)
				throw new InteractRuntimeException("gift_type2_name 不能空");
			String giftUrl = StringUtils.trimToNull(request.getParameter("gift_url"));
			String giftCover = StringUtils.trimToNull(request.getParameter("gift_cover"));
			String giftPics = StringUtils.trimToNull(request.getParameter("gift_pics"));
			String giftDetail = StringUtils.trimToNull(request.getParameter("gift_detail"));
			String giftExpressCo = StringUtils.trimToNull(request.getParameter("gift_express_co"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (activityTitle != null)
				sqlParams.add(activityTitle);
			if (taobaoaccountId != null)
				sqlParams.add(taobaoaccountId);
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (stock != null)
				sqlParams.add(stock);
			if (couponUrl != null)
				sqlParams.add(couponUrl);
			if (wayToShop != null)
				sqlParams.add(wayToShop);
			if (qrcodeToOrder != null)
				sqlParams.add(qrcodeToOrder);
			if (searchKeys != null)
				sqlParams.add(searchKeys);
			if (cowryUrl != null)
				sqlParams.add(cowryUrl);
			if (cowryCover != null)
				sqlParams.add(cowryCover);
			if (payPrice != null)
				sqlParams.add(payPrice);
			if (returnMoney != null)
				sqlParams.add(returnMoney);
			if (buyerMincredit != null)
				sqlParams.add(buyerMincredit);
			if (keepDays != null)
				sqlParams.add(keepDays);
			if (giftName != null)
				sqlParams.add(giftName);
			if (giftType1Id != null)
				sqlParams.add(giftType1Id);
			if (giftType2Id != null)
				sqlParams.add(giftType2Id);
			if (giftType1Name != null)
				sqlParams.add(giftType1Name);
			if (giftType2Name != null)
				sqlParams.add(giftType2Name);
			if (giftUrl != null)
				sqlParams.add(giftUrl);
			if (giftCover != null)
				sqlParams.add(giftCover);
			if (giftPics != null)
				sqlParams.add(giftPics);
			if (giftDetail != null)
				sqlParams.add(giftDetail);
			if (giftExpressCo != null)
				sqlParams.add(giftExpressCo);
			sqlParams.add(activityId);
			pst = connection.prepareStatement(new StringBuilder("update t_activity set status=0,audit_fail_reason=null")
					.append(activityTitle == null ? "" : ",title=?")
					.append(taobaoaccountId == null ? "" : ",taobaoaccount_id=?")
					.append(buyWay == null ? "" : ",buy_way=?").append(stock == null ? "" : ",stock=?")
					.append(couponUrl == null ? "" : ",coupon_url=?").append(wayToShop == null ? "" : ",way_to_shop=?")
					.append(qrcodeToOrder == null ? "" : ",qrcode_to_order=?")
					.append(searchKeys == null ? "" : ",search_keys=?").append(cowryUrl == null ? "" : ",cowry_url=?")
					.append(cowryCover == null ? "" : ",cowry_cover=?").append(payPrice == null ? "" : ",pay_price=?")
					.append(returnMoney == null ? "" : ",return_money=?")
					.append(buyerMincredit == null ? "" : ",buyer_mincredit=?")
					.append(keepDays == null ? "" : ",keep_days=?").append(giftName == null ? "" : ",gift_name=?")
					.append(giftType1Id == null ? "" : ",gift_type1_id=?")
					.append(giftType2Id == null ? "" : ",gift_type2_id=?")
					.append(giftType1Name == null ? "" : ",gift_type1_name=?")
					.append(giftType2Name == null ? "" : ",gift_type2_name=?")
					.append(giftUrl == null ? "" : ",gift_url=?").append(giftCover == null ? "" : ",gift_cover=?")
					.append(giftPics == null ? "" : ",gift_pics=?").append(giftDetail == null ? "" : ",gift_detail=?")
					.append(giftExpressCo == null ? "" : ",gift_express_co=?").append(" where id=?").toString());
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			int n = pst.executeUpdate();
			if (n != 1)
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

	@RequestMapping(value = "/inactcowries/offline")
	public void inactcowriesOffline(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set status=3 where id=? and status=1").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/inactcowries/alterstock")
	public void inactcowriesAlterstock(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);
			String countParam = StringUtils.trimToNull(request.getParameter("count"));
			Integer count = countParam == null ? null : Integer.parseInt(countParam);
			if (count < 0 || count > 100)
				throw new InteractRuntimeException("人数必须在1-100之间");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set stock=? where id=? and status=1").toString());
			pst.setObject(1, count);
			pst.setObject(2, activityId);
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

	@RequestMapping(value = "/inactcowries/alterdays")
	public void inactcowriesAlterdays(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);
			String daysParam = StringUtils.trimToNull(request.getParameter("days"));
			Integer days = daysParam == null ? null : Integer.parseInt(daysParam);
			if (days <= 0 || days > 7)
				throw new InteractRuntimeException("天数必须在1-7之间");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set keep_days=?,start_time=? where id=? and status=1")
							.toString());
			pst.setObject(1, days);
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, activityId);
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

	@RequestMapping(value = "/inactcowries")
	public void inactCowries(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String publishTimeStartParam = StringUtils.trimToNull(request.getParameter("publish_time_start"));
			Long publishTimeStart = publishTimeStartParam == null ? null : Long.parseLong(publishTimeStartParam);
			String publishTimeEndParam = StringUtils.trimToNull(request.getParameter("publish_time_end"));
			Long publishTimeEnd = publishTimeEndParam == null ? null : Long.parseLong(publishTimeEndParam);
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
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (publishTimeStart != null)
				sqlParams.add(publishTimeStart);
			if (publishTimeEnd != null)
				sqlParams.add(publishTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.gift_cover,t.publish_time,t.stock,t.way_to_shop,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buy_way,t.id,t.gift_name,t.pay_price,t.return_money,t.title,t.buyer_num,(t.start_time+t.keep_days*24*60*60*1000-rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')) remain_time from t_activity t where t.status=1 and t.user_id=?")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(buyWay == null ? "" : " and t.buy_way = ? ")
							.append(couponIf == null ? ""
									: couponIf == 1 ? " and (!ISNULL(t.coupon_url) and LENGTH(trim(t.coupon_url))>1) "
											: " and (ISNULL(t.coupon_url) or LENGTH(trim(t.coupon_url))=0) ")
							.append(publishTimeStart == null ? "" : " and t.publish_time >= ? ")
							.append(publishTimeEnd == null ? "" : " and t.publish_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("activityId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("title", rs.getObject("title"));
				item.put("buyerNum", rs.getObject("buyer_num"));
				item.put("remainTime", rs.getObject("remain_time"));
				item.put("publishTime", rs.getObject("publish_time"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("couponIf", rs.getObject("coupon_if"));
				item.put("stock", rs.getObject("stock"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activies = new JSONObject();
			activies.put("items", items);
			data.put("activies", activies);
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

	@RequestMapping(value = "/auditingcowries")
	public void auditingCowries(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String publishTimeStartParam = StringUtils.trimToNull(request.getParameter("publish_time_start"));
			Long publishTimeStart = publishTimeStartParam == null ? null : Long.parseLong(publishTimeStartParam);
			String publishTimeEndParam = StringUtils.trimToNull(request.getParameter("publish_time_end"));
			Long publishTimeEnd = publishTimeEndParam == null ? null : Long.parseLong(publishTimeEndParam);
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
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (publishTimeStart != null)
				sqlParams.add(publishTimeStart);
			if (publishTimeEnd != null)
				sqlParams.add(publishTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.gift_cover,t.way_to_shop,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buy_way,t.id,t.gift_name,t.pay_price,t.return_money,t.title,t.stock,t.publish_time,t.status,t.audit_fail_reason from t_activity t where 1=1 and t.user_id=?")
							.append(status == null ? " and t.status in (0,2,4) " : "")
							.append((status != null && status == 1) ? " and t.status=0 " : "")
							.append((status != null && status == 2) ? " and t.status=2 " : "")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(buyWay == null ? "" : " and t.buy_way = ? ")
							.append(couponIf == null ? ""
									: couponIf == 1 ? " and (!ISNULL(t.coupon_url) and LENGTH(trim(t.coupon_url))>1) "
											: " and (ISNULL(t.coupon_url) or LENGTH(trim(t.coupon_url))=0) ")
							.append(publishTimeStart == null ? "" : " and t.publish_time >= ? ")
							.append(publishTimeEnd == null ? "" : " and t.publish_time <= ? ")
							.append(" order by t.publish_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("activityId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("title", rs.getObject("title"));
				item.put("stock", rs.getObject("stock"));
				item.put("publishTime", rs.getObject("publish_time"));
				item.put("status", rs.getObject("status"));
				item.put("auditFailReason", rs.getObject("audit_fail_reason"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("couponIf", rs.getObject("coupon_if"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activies = new JSONObject();
			activies.put("items", items);
			data.put("activies", activies);
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

	@RequestMapping(value = "/auditingcowries/cancelaudit")
	public void auditingCowriesCancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set status=4 where id=? and status=0").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/del")
	public void del(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder("select status from t_activity where id=?").toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			int status = 0;
			if (rs.next()) {
				status = rs.getInt("status");
			} else
				throw new InteractRuntimeException("活动不存在");

			if (status == 1)
				throw new InteractRuntimeException("活动正在审核中");

			pst = connection.prepareStatement(new StringBuilder("update t_activity set del=1 where id=?").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/storingcowries/online")
	public void storingcowriesOnline(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);
			String stockParam = StringUtils.trimToNull(request.getParameter("stock"));
			Integer stock = stockParam == null ? null : Integer.parseInt(stockParam);
			if (stock <= 0)
				throw new InteractRuntimeException("人数必须大于0");
			String daysParam = StringUtils.trimToNull(request.getParameter("days"));
			Integer days = daysParam == null ? null : Integer.parseInt(daysParam);
			if (days <= 0 || days > 7)
				throw new InteractRuntimeException("天数必须在1-7之间");
			String returnMoneyParam = StringUtils.trimToNull(request.getParameter("return_money"));
			BigDecimal returnMoney = new BigDecimal(returnMoneyParam);
			if (returnMoney.intValue() <= 0)
				throw new InteractRuntimeException("返现金额只能增加或不变");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"update t_activity set status=1,return_money=?,stock=?,keep_days=?,start_time=? where id=? and status=3 and return_money<=?")
							.toString());
			pst.setObject(1, returnMoney);
			pst.setObject(2, stock);
			pst.setObject(3, days);
			pst.setObject(4, new Date().getTime());
			pst.setObject(5, activityId);
			pst.setObject(6, returnMoney);
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

	@RequestMapping(value = "/storingcowries/opencreditcardpay")
	public void storingcowriesOpencreditcardpay(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement(new StringBuilder(
					"select t.title,u.unwithdraw_money,t.creditcard_pay from t_activity t left join t_user u where t.id=?  for update")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			BigDecimal unwithdrawMoney = null;
			int creditcardPay = 0;
			String title = null;
			if (rs.next()) {
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
				creditcardPay = rs.getInt("creditcard_pay");
				title = rs.getString("title");
			} else
				throw new InteractRuntimeException("活动不存在");
			pst.close();
			if (creditcardPay == 0) {
				if (unwithdrawMoney.intValue() < 1)
					throw new InteractRuntimeException("余额不足");
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set unwithdraw_money=unwithdraw_money-1 where id=? and (unwithdraw_money-1)>0")
								.toString());
				pst.setObject(1, loginStatus.getUserId());
				int cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, loginStatus.getUserId());
				pst.setObject(3, -1);
				pst.setObject(4, "活动'" + title + "'开通信用卡支付");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, activityId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();
			}

			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set creditcard_pay=1 where id=? and status=3").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/storingcowries/closecreditcardpay")
	public void storingcowriesClosecreditcardpay(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set creditcard_pay=3 where id=? and status=3").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/storingcowries/openhuabeipay")
	public void storingcowriesOpenhuabeipay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement(new StringBuilder(
					"select t.title,u.unwithdraw_money,t.huabei_pay from t_activity t left join t_user u where t.id=? for update")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			BigDecimal unwithdrawMoney = null;
			int huabeiPay = 0;
			String title = null;
			if (rs.next()) {
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
				huabeiPay = rs.getInt("huabei_pay");
				title = rs.getString("title");
			} else
				throw new InteractRuntimeException("活动不存在");
			pst.close();
			if (huabeiPay == 0) {
				if (unwithdrawMoney.intValue() < 1)
					throw new InteractRuntimeException("余额不足");
				pst = connection.prepareStatement(new StringBuilder(
						"update t_user set unwithdraw_money=unwithdraw_money-1 where id=? and (unwithdraw_money-1)>0")
								.toString());
				pst.setObject(1, loginStatus.getUserId());
				int cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();

				String billId = new Date().getTime() + RandomStringUtils.randomNumeric(3);
				pst = connection.prepareStatement(new StringBuilder(
						"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,4)")
								.toString());
				pst.setObject(1, billId);
				pst.setObject(2, loginStatus.getUserId());
				pst.setObject(3, -1);
				pst.setObject(4, "活动'" + title + "'开通花呗支付");
				pst.setObject(5, new Date().getTime());
				pst.setObject(6, activityId);
				cnt = pst.executeUpdate();
				if (cnt != 1)
					throw new InteractRuntimeException("操作失败");
				pst.close();
			}

			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set huabei_pay=1 where id=? and status=3").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/storingcowries/closehuabeipay")
	public void storingcowriesClosehuabeipay(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-待审核 2-审核失败
			String activityIdParam = StringUtils.trimToNull(request.getParameter("activity_id"));
			Integer activityId = activityIdParam == null ? null : Integer.parseInt(activityIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					new StringBuilder("update t_activity set huabei_pay=3 where id=? and status=3").toString());
			pst.setObject(1, activityId);
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

	@RequestMapping(value = "/storingcowries")
	public void storingCowries(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// status : 空-全部 1-仓库中 2-重新审核失败
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String publishTimeStartParam = StringUtils.trimToNull(request.getParameter("publish_time_start"));
			Long publishTimeStart = publishTimeStartParam == null ? null : Long.parseLong(publishTimeStartParam);
			String publishTimeEndParam = StringUtils.trimToNull(request.getParameter("publish_time_end"));
			Long publishTimeEnd = publishTimeEndParam == null ? null : Long.parseLong(publishTimeEndParam);
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
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (publishTimeStart != null)
				sqlParams.add(publishTimeStart);
			if (publishTimeEnd != null)
				sqlParams.add(publishTimeEnd);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.gift_cover,t.way_to_shop,if(isnull(t.coupon_url)||length(t.coupon_url)=0,0,1) coupon_if,t.buy_way,t.id,t.gift_name,t.pay_price,t.return_money,t.title,t.stock,t.publish_time,t.status,t.audit_fail_reason from t_activity t where 1=1 and t.user_id=?")
							.append(status == null ? " and t.status in (2,3) "
									: (status == 1 ? " and t.status=3 "
											: (status == 2 ? " and t.status=2 " : " and t.status in (2,3) ")))
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(buyWay == null ? "" : " and t.buy_way = ? ")
							.append(couponIf == null ? ""
									: couponIf == 1 ? " and (!ISNULL(t.coupon_url) and LENGTH(trim(t.coupon_url))>1) "
											: " and (ISNULL(t.coupon_url) or LENGTH(trim(t.coupon_url))=0) ")
							.append(publishTimeStart == null ? "" : " and t.publish_time >= ? ")
							.append(publishTimeEnd == null ? "" : " and t.publish_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("activityId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("giftCover", rs.getObject("gift_cover"));
				item.put("title", rs.getObject("title"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("title", rs.getObject("title"));
				item.put("stock", rs.getObject("stock"));
				item.put("publishTime", rs.getObject("publish_time"));
				item.put("status", rs.getObject("status"));
				item.put("auditFailReason", rs.getObject("audit_fail_reason"));
				item.put("buyWay", rs.getObject("buy_way"));
				item.put("wayToShop", rs.getObject("way_to_shop"));
				item.put("couponIf", rs.getObject("coupon_if"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activies = new JSONObject();
			activies.put("items", items);
			data.put("activies", activies);
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

	@RequestMapping(value = "/cowrydetail")
	public void cowrydetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.gift_url,t.keep_days,tb.taobao_user_nick,t.taobaoaccount_id,t.gift_type1_id,t.gift_type1_name,t.gift_type2_id,t.gift_type2_name,t.title,t.huabei_pay,t.creditcard_pay,t.status,t.way_to_shop,t.qrcode_to_order,t.search_keys,t.cowry_cover,t.cowry_url,t.gift_pics,t.gift_cover,t.gift_name,t.pay_price,t.return_money,t.keep_days,t.stock,t.buy_way,t.coupon_url,t.buyer_mincredit,t.gift_express_co,t.gift_detail from t_activity t left join t_taobaoaccount tb on t.taobaoaccount_id=tb.id  where t.id=?")
							.toString());
			pst.setObject(1, activityId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("giftType1Id", rs.getInt("gift_type1_id"));
				item.put("giftType1Name", rs.getString("gift_type1_name"));
				item.put("giftType2Id", rs.getInt("gift_type2_id"));
				item.put("giftType2Name", rs.getString("gift_type2_name"));
				item.put("title", rs.getString("title"));
				item.put("taobaoUserNick", rs.getString("taobao_user_nick"));
				item.put("taobaoaccountId", rs.getInt("taobaoaccount_id"));
				item.put("keepDays", rs.getInt("keep_days"));
				item.put("giftUrl", rs.getString("gift_url"));
				item.put("huabeiPay", rs.getInt("huabei_pay"));
				item.put("creditcardPay", rs.getInt("creditcard_pay"));
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("stock", rs.getInt("stock"));
				item.put("buyWay", rs.getInt("buy_way"));
				item.put("status", rs.getInt("status"));
				item.put("couponUrl", rs.getString("coupon_url"));
				item.put("buyerMincredit", rs.getInt("buyer_mincredit"));
				item.put("giftExpressCo", rs.getString("gift_express_co"));
				item.put("giftDetail", rs.getString("gift_detail"));
				item.put("giftPics", rs.getString("gift_pics"));
				Integer wayToShop = rs.getInt("way_to_shop");
				String qrcodeToOrder = null;
				String searchKeys = null;
				String cowryCover = null;
				String cowryUrl = null;
				if (wayToShop == 1) {
					searchKeys = rs.getString("search_keys");
					cowryCover = rs.getString("cowry_cover");
					cowryUrl = rs.getString("cowry_url");
				} else if (wayToShop == 2) {
					cowryUrl = rs.getString("cowry_url");
				} else if (wayToShop == 3) {
					qrcodeToOrder = rs.getString("qrcode_to_order");
				} else if (wayToShop == 4) {
					searchKeys = rs.getString("search_keys");
					cowryCover = rs.getString("cowry_cover");
					cowryUrl = rs.getString("cowry_url");
				}
				item.put("wayToShop", wayToShop);
				item.put("qrcodeToOrder", qrcodeToOrder);
				item.put("searchKeys", searchKeys);
				item.put("cowryCover", cowryCover);
				item.put("cowryUrl", cowryUrl);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (

		Exception e) {
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