package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import rrightway.entity.BaseResVo;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.HomeEntrance")
@RequestMapping(value = "/p/m/homee")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement("select cover,type,link from t_mainrotation order by sort_no asc,id desc");
			ResultSet rs = pst.executeQuery();
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
					"select id,title,if(length(content)<1000,content,null) content from t_notice order by add_time desc limit 0,10");
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

			pst = connection.prepareStatement("select id,gift_cover,gift_name,pay_price from t_activity  limit 0,10");
			rs = pst.executeQuery();
			JSONArray featuredActivities = new JSONArray();
			while (rs.next()) {
				JSONObject featuredActivity = new JSONObject();
				featuredActivity.put("id", rs.getInt("id"));
				featuredActivity.put("giftCover", rs.getString("gift_cover"));
				featuredActivity.put("giftName", rs.getString("gift_name"));
				featuredActivity.put("payPrice", rs.getBigDecimal("pay_price"));
				featuredActivities.add(featuredActivity);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("mainrotations", mainrotations);
			data.put("notices", notices);
			data.put("featuredActivities", featuredActivities);
			data.put("mayLoves", featuredActivities);
			data.put("searchkey_preset", "衣服");

			HttpRespondWithData.todo(request, response,
					JSON.toJSONString(BaseResVo.getSuccess(data, (String) request.getAttribute("requestId")),
							SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect));
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