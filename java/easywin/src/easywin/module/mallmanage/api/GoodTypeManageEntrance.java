package easywin.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.GoodTypeManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/goodtype")
public class GoodTypeManageEntrance {

	public static Logger logger = Logger.getLogger(GoodTypeManageEntrance.class);

	@RequestMapping(value = "/type1s")
	public void type1s(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement("select id,name from t_mall_good_type where mall_id=? and level=1");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject types = new JSONObject();
			types.put("items", items);
			data.put("types", types);
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

	@RequestMapping(value = "/type2s")
	public void type2s(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String type1IdParam = StringUtils.trimToNull(request.getParameter("type1_id"));
			if (type1IdParam == null)
				throw new InteractRuntimeException("type1_id 不可空");
			Integer type1Id = Integer.parseInt(type1IdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id,name,cover from t_mall_good_type where mall_id=? and level=2 and upid=?");
			pst.setObject(1, mallId);
			pst.setObject(2, type1Id);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("cover", rs.getObject("cover"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject types = new JSONObject();
			types.put("items", items);
			data.put("types", types);
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

	@RequestMapping(value = "/addtype1")
	public void addType1(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("insert into t_mall_good_type (mall_id,name,level,upid) values(?,?,1,0)",
					PreparedStatement.RETURN_GENERATED_KEYS);

			pst.setObject(1, mallId);
			pst.setObject(2, name);

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int id = rs.getInt(1);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1Id", id);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (

		Exception e) {
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

	@RequestMapping(value = "/addtype2")
	public void addType2(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String type1IdParam = StringUtils.trimToNull(request.getParameter("type1_id"));
			if (type1IdParam == null)
				throw new InteractRuntimeException("type1_id 不可空");
			Integer type1Id = Integer.parseInt(type1IdParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			if (cover == null)
				throw new InteractRuntimeException("cover 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement(
					"insert into t_mall_good_type (cover,mall_id,name,level,upid) values(?,?,?,2,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);

			pst.setObject(1, cover);
			pst.setObject(2, mallId);
			pst.setObject(3, name);
			pst.setObject(4, type1Id);

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int id = rs.getInt(1);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type2Id", id);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (

		Exception e) {
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

	@RequestMapping(value = "/altertype")
	public void alterType(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String typeIdParam = StringUtils.trimToNull(request.getParameter("type_id"));
			if (typeIdParam == null)
				throw new InteractRuntimeException("type_id 不可空");
			Integer typeId = Integer.parseInt(typeIdParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String cover = StringUtils.trimToNull(request.getParameter("cover"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 查詢商品详情
			List sqlParams = new ArrayList();
			boolean conditionHas = false;
			if (name != null)
				sqlParams.add(name);
			if (cover != null)
				sqlParams.add(cover);
			if (!sqlParams.isEmpty())
				conditionHas = true;
			sqlParams.add(mallId);
			sqlParams.add(typeId);
			if (conditionHas) {
				connection = EasywinDataSource.dataSource.getConnection();
				StringBuilder sql = new StringBuilder("update t_mall_good_type set ");
				StringBuilder sql1 = new StringBuilder().append(name == null ? "" : ",`name`=?")
						.append(cover == null ? "" : ",`cover`=?");
				sql.append(sql1.substring(1)).append(" where mall_id=? and id=?");
				pst = connection.prepareStatement(sql.toString());
				for (int i = 0; i < sqlParams.size(); i++) {
					pst.setObject(i + 1, sqlParams.get(i));
				}

				int n = pst.executeUpdate();
				pst.close();
				if (n == 0) {
					throw new InteractRuntimeException("类型不存在");
				} else if (n > 1) {
					throw new InteractRuntimeException("操作失败,发现多条记录");
				}
				pst.close();
			}

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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

	@RequestMapping(value = "/deltype")
	public void delType(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String typeIdParam = StringUtils.trimToNull(request.getParameter("type_id"));
			if (typeIdParam == null)
				throw new InteractRuntimeException("type_id 不可空");
			Integer typeId = Integer.parseInt(typeIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("delete from t_mall_good_type where id=? or upid=?");
			pst.setObject(1, typeId);
			pst.setObject(2, typeId);

			int n = pst.executeUpdate();
			pst.close();
			if (n == 0) {
				throw new InteractRuntimeException("类型不存在");
			}

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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
