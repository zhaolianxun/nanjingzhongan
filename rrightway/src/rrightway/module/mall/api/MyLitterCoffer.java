package rrightway.module.mall.api;

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

import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.module.mall.business.GetLoginStatus;
import rrightway.module.mall.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("mall.api.MyLitterCoffer")
@RequestMapping(value = "/m/e/mlc")
public class MyLitterCoffer {

	public static Logger logger = Logger.getLogger(MyLitterCoffer.class);

	@RequestMapping(value = "/mylittercofferhome")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 检查手机号
			pst = connection.prepareStatement(
					"select t.money,(select count(id) from t_mall_user where from_user_id=t.id) my_fans_count from t_mall_user t where t.id=? and t.mall_id=?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			ResultSet rs = pst.executeQuery();
			int money = 0;
			int myFansCount = 0;
			if (rs.next()) {
				money = rs.getInt("money");
				myFansCount = rs.getInt("my_fans_count");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			JSONObject data = new JSONObject();
			data.put("money", money);
			data.put("withdrewMoney", 0);
			data.put("canWithdrawMoney", 0);
			data.put("rewardMoney", 0);
			data.put("myFansCount", myFansCount);
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

}
