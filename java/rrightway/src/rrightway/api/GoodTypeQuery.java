package rrightway.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("api.GoodTypeQuery")
@RequestMapping(value = "/goodtype")
public class GoodTypeQuery {

	public static Logger logger = Logger.getLogger(GoodTypeQuery.class);

	/**
	 * 加载1级商品分类
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/goodtype1s")
	public void goodTypeEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement("select id,name,cover from t_good_type where  level=1 order by name asc");
			ResultSet rs = pst.executeQuery();
			JSONArray type1s = new JSONArray();
			while (rs.next()) {
				JSONObject type1 = new JSONObject();
				type1.put("id", rs.getObject(1));
				type1.put("name", rs.getObject(2));
				type1.put("cover", rs.getObject(3));
				type1s.add(type1);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1s", type1s);
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

	/**
	 * 加载2级商品分类
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/goodtype2s")
	public void type2s(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String type1IdParam = request.getParameter("type1_id");
			if (type1IdParam == null || type1IdParam.trim() == "")
				throw new InteractRuntimeException("type1_id不可空");
			int type1Id = Integer.parseInt(type1IdParam);
			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id,name,cover from t_mall_good_type where level=2 and upid=? order by name asc");
			pst.setObject(1, type1Id);
			ResultSet rs = pst.executeQuery();
			JSONArray type2s = new JSONArray();
			while (rs.next()) {
				JSONObject type2 = new JSONObject();
				type2.put("id", rs.getObject(1));
				type2.put("name", rs.getObject(2));
				type2.put("cover", rs.getObject(3));
				type2s.add(type2);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type2s", type2s);
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
