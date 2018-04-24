package rrightway.module.plat.module.api.merchant;

import java.math.BigDecimal;
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

@Controller("plat.api.merchant.PublishGoodEntrance")
@RequestMapping(value = "/p/pubgoodent")
public class PublishGoodEntrance {

	public static Logger logger = Logger.getLogger(PublishGoodEntrance.class);

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
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			if (buyWayParam == null)
				throw new InteractRuntimeException("buy_way 不能空");
			int buyWay = Integer.parseInt(buyWayParam);
			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			if (wayToShopParam == null)
				throw new InteractRuntimeException("way_to_shop 不能空");
			int wayToShop = Integer.parseInt(wayToShopParam);
			String couponUrl = StringUtils.trimToNull(request.getParameter("coupon_url"));
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
			if (wayToShopParam == null)
				throw new InteractRuntimeException("buyer_mincredit 不能空");
			int buyerMincredit = Integer.parseInt(buyerMincreditParam);

			String keepDaysParam = StringUtils.trimToNull(request.getParameter("keep_days"));
			if (keepDaysParam == null)
				throw new InteractRuntimeException("keep_days 不能空");
			int keepDays = Integer.parseInt(keepDaysParam);
			String goodName = StringUtils.trimToNull(request.getParameter("good_name"));
			if (goodName == null)
				throw new InteractRuntimeException("good_name 不能空");
			String goodType1IdParam = StringUtils.trimToNull(request.getParameter("good_type1_id"));
			if (goodType1IdParam == null)
				throw new InteractRuntimeException("good_type1_id 不能空");
			int goodType1Id = Integer.parseInt(goodType1IdParam);
			String goodType2IdParam = StringUtils.trimToNull(request.getParameter("good_type2_id"));
			if (goodType2IdParam == null)
				throw new InteractRuntimeException("good_type2_id 不能空");
			int goodType2Id = Integer.parseInt(goodType2IdParam);
			String goodType1Name = StringUtils.trimToNull(request.getParameter("good_type1_name"));
			if (goodType1Name == null)
				throw new InteractRuntimeException("good_type1_name 不能空");
			String goodType2Name = StringUtils.trimToNull(request.getParameter("good_type2_name"));
			if (goodType2Name == null)
				throw new InteractRuntimeException("good_type2_name 不能空");
			String srcurl = StringUtils.trimToNull(request.getParameter("srcurl"));

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
}