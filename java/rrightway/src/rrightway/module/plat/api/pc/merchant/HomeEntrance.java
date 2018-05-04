package rrightway.module.plat.api.pc.merchant;

import java.sql.Connection;
import java.sql.PreparedStatement;

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

@Controller("plat.api.merchant.HomeEntrance")
@RequestMapping(value = "/p/homeent")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

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

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("nickname", "rxw3333");
			data.put("auditingGoodCnt", 3);
			data.put("activeGoodCnt", 2);
			data.put("storeGoodCnt", 11);
			data.put("collatingOrderCnt", 1);
			data.put("buyedOrderCnt", 2);
			data.put("returnedOrderCnt", 3);
			data.put("moneyBalance", 33);
			data.put("frozenMoney", 32.3);
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