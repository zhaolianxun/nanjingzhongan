package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import rrightway.entity.BaseResVo;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);

			connection = RrightwayDataSource.dataSource.getConnection();
			ResultSet rs = null;

			int newmsgCount = 0;
			if (loginStatus != null) {
				pst = connection
						.prepareStatement("select count(*) newmsg_count from t_message where user_id=? and read_if=0");
				pst.setObject(1, loginStatus.getUserId());
				rs = pst.executeQuery();
				if (rs.next()) {
					newmsgCount = rs.getInt("newmsg_count");
				}
				pst.close();
			}

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
					"select id,title,if(length(content)<1000,content,null) content from t_notice order by add_time desc limit 0,10");
			rs = pst.executeQuery();
			JSONArray notices = new JSONArray();
			while (rs.next()) {
				JSONObject notice = new JSONObject();
				notice.put("id", rs.getInt("id"));
				notice.put("title", rs.getString("title"));
				notices.add(notice);
			}
			pst.close();

			pst = connection.prepareStatement(
					"select t.id,t.gift_cover,t.gift_name,t.pay_price from t_activity t where  (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-(t.keep_days*24*60*60*1000+t.start_time))<=0 and t.status=1 and t.del=0 order by rand() limit 0,8");
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
			data.put("newmsgCount", newmsgCount);
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

	@RequestMapping(value = "/notice/ent")
	public void noticeEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.title,t.content,t.add_time from t_notice t order by t.add_time desc limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("title", rs.getObject("title"));
				item.put("addTime", rs.getObject("add_time"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/notice/detail")
	public void noticeDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = idParam == null ? null : Integer.parseInt(idParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("select t.title,t.content,t.add_time from t_notice t where t.id=? ").toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("title", rs.getObject("title"));
				item.put("content", rs.getObject("content"));
				item.put("addTime", rs.getObject("add_time"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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