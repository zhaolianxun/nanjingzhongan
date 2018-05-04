package rrightway.module.plat.api.pc.merchant;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

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

@Controller("plat.api.merchant.PublishActivityCowryEntrance")
@RequestMapping(value = "/p/pubactcryent")
public class PublishActivityCowryEntrance {

	public static Logger logger = Logger.getLogger(PublishActivityCowryEntrance.class);

	@RequestMapping(value = "/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			pst = connection.prepareStatement("select id,nickname from t_seller_taobaoaccount where user_id=?");
			rs = pst.executeQuery();
			JSONArray sellerNicknames = new JSONArray();
			while (rs.next()) {
				JSONObject sellerNickname = new JSONObject();
				sellerNickname.put("id", rs.getObject("id"));
				sellerNickname.put("nickname", rs.getObject("nickname"));
				sellerNicknames.add(sellerNickname);
			}
			pst.close();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1s", type1s);
			data.put("sellerNicknames", sellerNicknames);
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

	@RequestMapping(value = "/publish")
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
			String couponUrl = StringUtils.trimToNull(request.getParameter("coupon_url"));
			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			if (wayToShopParam == null)
				throw new InteractRuntimeException("way_to_shop 不能空");
			int wayToShop = Integer.parseInt(wayToShopParam);
			String qrcodeToOrder = StringUtils.trimToNull(request.getParameter("qrcode_to_order"));
			if (wayToShop == 3 && qrcodeToOrder == null)
				throw new InteractRuntimeException("qrcode_to_order 不能空");
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
			String giftDetail = StringUtils.trimToNull(request.getParameter("gift_detail"));
			if (giftDetail == null)
				throw new InteractRuntimeException("gift_detail 不能空");
			String giftExpressCo = StringUtils.trimToNull(request.getParameter("gift_express_co"));
			if (giftExpressCo == null)
				throw new InteractRuntimeException("gift_express_co 不能空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"INSERT INTO `t_activity` (`user_id`, `taobaoaccount_id`, `title`, `publish_time`, `way_to_shop`, `qrcode_to_order`, `search_keys`, `cowry_url`, `cowry__cover`,`buy_way`, `coupon_url`,`pay_price`, `return_money`, `buyer_mincredit`, `keep_days`, `gift_name`, `gift_type1_id`, `gift_type1_name`, `gift_type2_id`, `gift_type2_name`, `gift_url`, `gift_cover`, `gift_detail`, `gift_express_co`,`stock`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);",
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
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
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
}