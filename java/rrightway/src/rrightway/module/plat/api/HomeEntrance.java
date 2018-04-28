package rrightway.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.HomeEntrance")
@RequestMapping(value = "/p/homee")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String account = StringUtils.trimToNull(request.getParameter("account"));
			if (account == null)
				throw new InteractRuntimeException("account不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select id,cover,name from t_good_type where level=1 order by sort_no asc,name asc");
			ResultSet rs = pst.executeQuery();
			JSONArray type1s = new JSONArray();
			while (rs.next()) {
				JSONObject type1 = new JSONObject();
				type1.put("id", rs.getInt("id"));
				type1.put("cover", rs.getString("cover"));
				type1.put("name", rs.getString("name"));
				type1s.add(type1);
			}
			pst.close();

			pst = connection
					.prepareStatement("select cover,type,link from t_mainrotation order by sort_no asc,id desc");
			rs = pst.executeQuery();
			JSONArray mainrotations = new JSONArray();
			while (rs.next()) {
				JSONObject mainrotation = new JSONObject();
				mainrotation.put("cover", rs.getString("cover"));
				mainrotation.put("link", rs.getString("link"));
				mainrotation.put("type", rs.getString("type"));
				mainrotations.add(mainrotation);
			}
			pst.close();

			pst = connection.prepareStatement(
					"select id,title,if(length(content)<1000,content,null) content from t_notice order by add_time desc");
			rs = pst.executeQuery();
			JSONArray notices = new JSONArray();
			while (rs.next()) {
				JSONObject notice = new JSONObject();
				notice.put("id", rs.getInt("id"));
				notice.put("title", rs.getString("title"));
				notice.put("content", rs.getString("content"));
				notices.add(notice);
			}
			pst.close();

			pst = connection.prepareStatement("select id,gift_cover,gift_name,pay_price from t_activity limit 0,10");
			rs = pst.executeQuery();
			JSONArray featuredActivities = new JSONArray();
			while (rs.next()) {
				JSONObject featuredActivity = new JSONObject();
				featuredActivity.put("id", rs.getInt("id"));
				featuredActivity.put("giftCover", rs.getInt("gift_cover"));
				featuredActivity.put("giftName", rs.getString("gift_name"));
				featuredActivity.put("payPrice", rs.getBigDecimal("pay_price"));
				featuredActivities.add(featuredActivity);
			}
			pst.close();
			
			
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1s", type1s);
			data.put("mainrotations", mainrotations);
			data.put("notices", notices);
			data.put("featuredActivities", featuredActivities);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

}