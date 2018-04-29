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
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

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
			data.put("hotTrials", featuredActivities);
			JSONArray floors = new JSONArray();
			JSONObject f1 = new JSONObject();
			f1.put("name", "美容护肤");
			f1.put("f1", featuredActivities);
			floors.add(f1);
			JSONObject f2 = new JSONObject();
			f2.put("name", "精品男装");
			f2.put("f2", featuredActivities);
			floors.add(f2);
			JSONObject f3 = new JSONObject();
			f3.put("name", "母婴用品");
			f3.put("f3", featuredActivities);
			floors.add(f3);
			JSONObject f4 = new JSONObject();
			f4.put("name", "鞋子箱包");
			f4.put("f4", featuredActivities);
			floors.add(f4);
			data.put("mayLoves", featuredActivities);
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