package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.MyEntrance")
@RequestMapping(value = "/p/m/myent")
public class MyEntrance {

	public static Logger logger = Logger.getLogger(MyEntrance.class);

	@RequestMapping(value = "/iambuyer")
	public void iambuyer(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.right_wallet,t.money,(select sum(money) from t_widthdraw where user_id=t.id) withdrawing_money,(select count(id) from t_order where buyer_id=t.id and status in (0)) buyed_count,(select count(id) from t_order where buyer_id=t.id and status in (1)) checked_count,(select count(id) from t_order where buyer_id=t.id and status in (2)) returned_count,(select count(id) from t_order where buyer_id=t.id and buyer_protect_rights_status != 0) protected_count from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int buyedCount;
			int checkedCount;
			int returnedCount;
			int protectedCount;
			BigDecimal money;
			BigDecimal rightWallet;
			BigDecimal withdrawingMoney;
			if (rs.next()) {
				buyedCount = rs.getInt("buyed_count");
				checkedCount = rs.getInt("checked_count");
				returnedCount = rs.getInt("returned_count");
				protectedCount = rs.getInt("protected_count");
				money = rs.getBigDecimal("money");
				rightWallet = rs.getBigDecimal("right_wallet");
				withdrawingMoney = rs.getBigDecimal("withdrawing_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("buyedCount", buyedCount);
			data.put("checkedCount", checkedCount);
			data.put("returnedCount", returnedCount);
			data.put("money", money);
			data.put("rightWallet", rightWallet);
			data.put("withdrawingMoney", withdrawingMoney);
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

	@RequestMapping(value = "/iamseller")
	public void iamseller(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.money,(select count(id) from t_order where buyer_id=t.id and status in (0)) uncheck_count,(select count(id) from t_order where buyer_id=t.id and status in (1)) buyed_count from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int buyedCount;
			int checkedCount;
			BigDecimal money;
			if (rs.next()) {
				buyedCount = rs.getInt("buyed_count");
				checkedCount = rs.getInt("uncheck_count");
				money = rs.getBigDecimal("money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("buyedCount", buyedCount);
			data.put("checkedCount", checkedCount);
			data.put("money", money);
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