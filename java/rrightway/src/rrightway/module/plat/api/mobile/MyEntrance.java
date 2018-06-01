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
					"select (select count(*) from t_message where  user_id=t.id  and read_if=0) newmsg_count,t.username,(t.right_wallet_unoutable+t.right_wallet_outable) right_wallet,(t.withdrawable_money+t.unwithdraw_money+t.frozen_money) money,(select ifnull(sum(amount),0) from t_widthdraw where user_id=t.id and status=0) withdrawing_money,(select count(id) from t_order where del=0 and buyer_id=t.id and status in (0,4,5,6)) applyed_count,(select count(id) from t_order where del=0 and buyer_id=t.id and status=1 and rightprotect_status=0) checked_count,(select count(id) from t_order where del=0 and buyer_id=t.id and status=2) returned_count,(select count(id) from t_order where del=0 and buyer_id=t.id and rightprotect_status !=0) protected_count,(select count(id) from t_order where del=0 and buyer_id=t.id and complain in (1,2,6)) complain_count from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int applyedCount;
			int checkedCount;
			int newmsgCount;
			int returnedCount;
			int protectedCount;
			int complainCount;
			BigDecimal money;
			BigDecimal rightWallet;
			BigDecimal withdrawingMoney;
			String username;
			if (rs.next()) {
				applyedCount = rs.getInt("applyed_count");
				newmsgCount = rs.getInt("newmsg_count");
				checkedCount = rs.getInt("checked_count");
				complainCount = rs.getInt("complain_count");
				returnedCount = rs.getInt("returned_count");
				protectedCount = rs.getInt("protected_count");
				money = rs.getBigDecimal("money");
				rightWallet = rs.getBigDecimal("right_wallet");
				withdrawingMoney = rs.getBigDecimal("withdrawing_money");
				username = rs.getString("username");

			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("newmsgCount", newmsgCount);
			data.put("complainCount", complainCount);
			data.put("applyedCount", applyedCount);
			data.put("checkedCount", checkedCount);
			data.put("returnedCount", returnedCount);
			data.put("protectedCount", protectedCount);
			data.put("money", money);
			data.put("withdrawingMoney", withdrawingMoney);
			data.put("rightWallet", rightWallet);
			data.put("username", username);
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
					"select (select count(*) from t_message where user_id=t.id  and read_if=0) newmsg_count,t.frozen_money,t.username,(t.withdrawable_money+t.unwithdraw_money+t.frozen_money) money,t.unwithdraw_money,(select count(id) from t_order where seller_del=0 and seller_id=t.id and status in (0,3,5,6)) uncheck_count,(select count(id) from t_order where seller_del=0 and seller_id=t.id and status=1 and rightprotect_status=0) buyed_count,(select count(id) from t_order where seller_del=0 and seller_id=t.id and rightprotect_status !=0) rightprotected_count,(select count(id) from t_order where seller_del=0 and seller_id=t.id and complain =6) complain_count,(select count(id) from t_activity where user_id=t.id and status=0) auditcowry_count,(select count(id) from t_activity where user_id=t.id and status=1) inactcowry_count from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int buyedCount;
			int newmsgCount;
			int uncheckCount;
			int rightprotectedCount;
			int complainCount;
			int auditcowryCount;
			int inactcowryCount;
			BigDecimal money;
			BigDecimal unwithdrawMoney;
			BigDecimal frozenMoney;
			String username;
			if (rs.next()) {
				buyedCount = rs.getInt("buyed_count");
				newmsgCount = rs.getInt("newmsg_count");
				uncheckCount = rs.getInt("uncheck_count");
				money = rs.getBigDecimal("money");
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
				frozenMoney = rs.getBigDecimal("frozen_money");
				username = rs.getString("username");
				rightprotectedCount = rs.getInt("rightprotected_count");
				complainCount = rs.getInt("complain_count");
				auditcowryCount = rs.getInt("auditcowry_count");
				inactcowryCount = rs.getInt("inactcowry_count");

			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("newmsgCount", newmsgCount);
			data.put("rightprotectedCount", rightprotectedCount);
			data.put("complainCount", complainCount);
			data.put("auditcowryCount", auditcowryCount);
			data.put("inactcowryCount", inactcowryCount);
			data.put("buyedCount", buyedCount);
			data.put("uncheckCount", uncheckCount);
			data.put("money", money);
			data.put("unwithdrawMoney", unwithdrawMoney);
			data.put("username", username);
			data.put("frozenMoney", frozenMoney);
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