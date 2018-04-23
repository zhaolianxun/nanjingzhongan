package rrightway.module.plat.module.api.merchant;

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
			String goodName = StringUtils.trimToNull(request.getParameter("good_name"));
			if (goodName == null)
				throw new InteractRuntimeException("good_name 不能空");
			String activityTitle = StringUtils.trimToNull(request.getParameter("activity_title"));
			if (activityTitle == null)
				throw new InteractRuntimeException("activity_title 不能空");
			String activityTitle = StringUtils.trimToNull(request.getParameter("buyway"));
			if (activityTitle == null)
				throw new InteractRuntimeException("activity_title 不能空");
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