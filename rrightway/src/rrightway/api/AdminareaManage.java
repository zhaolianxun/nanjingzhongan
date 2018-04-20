package rrightway.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("api.AdminareaManage")
@RequestMapping(value = "/adminarea")
public class AdminareaManage {

	public static Logger logger = Logger.getLogger(AdminareaManage.class);

	@RequestMapping(value = "/provinces", method = RequestMethod.POST)
	public void provinces(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		Statement st = null;
		try {
			// 获取请求参数

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("select id,name from `cm-plat`.t_adminarea where level=1 order by name asc");
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject o = new JSONObject();
				o.put("id", rs.getString(1));
				o.put("name", rs.getString(2));
				items.add(o);
			}
			rs.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (st != null)
				st.close();
			if (connection != null)
				connection.close();

		}
	}

	@RequestMapping(value = "/cities", method = RequestMethod.POST)
	public void cities(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			if (provinceIdParam == null)
				throw new InteractRuntimeException("province_id can`t be empty.");
			int provinceId = Integer.parseInt(provinceIdParam);

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select id,name from `cm-plat`.t_adminarea where level=2 and upid=? order by name asc");
			pst.setInt(1, provinceId);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject o = new JSONObject();
				o.put("id", rs.getString(1));
				o.put("name", rs.getString(2));
				items.add(o);
			}
			rs.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
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

	@RequestMapping(value = "/distincts", method = RequestMethod.POST)
	public void distincts(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// TODO 获取请求参数
			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			if (cityIdParam == null)
				throw new InteractRuntimeException("city_id can`t be empty.");
			int cityId = Integer.parseInt(cityIdParam);

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select id,name from `cm-plat`.t_adminarea where level=3 and upid=? order by name asc");
			pst.setInt(1, cityId);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject o = new JSONObject();
				o.put("id", rs.getString(1));
				o.put("name", rs.getString(2));
				items.add(o);
			}
			rs.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
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
