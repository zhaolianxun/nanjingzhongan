package cszyylxm.module.js.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cszyylxm.business.LoginStatus;
import cszyylxm.util.CszyylxmDataSource;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.InteractRuntimeException;

@Controller("js.api.goodzuoye")
@RequestMapping(value = "/js/goodzuoye")
public class GoodZuoyeApis {

	public static Logger logger = Logger.getLogger(GoodZuoyeApis.class);

	@RequestMapping(value = "/xmbjlist")
	public void bjList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String xmTypeParam = StringUtils.trimToNull(request.getParameter("xm_type"));
			if (xmTypeParam == null)
				throw new InteractRuntimeException("xm_type 不能空");
			int xmType = Integer.parseInt(xmTypeParam);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(xmType);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select u.realname,u.student_no,c.name clazz_name,bj.id from t_xm_bj bj left join t_xm xm on bj.xm_id=xm.id left join t_user u on xm.student_id=u.id left join t_clazz c on u.clazz_id=c.id where xm.type=? and bj.good_if=1")
							.append(" order by xm.start_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("studentNo", rs.getObject("student_no"));
				item.put("clazzName", rs.getObject("clazz_name"));
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

	@RequestMapping(value = "/xmcddybglist")
	public void cddybgList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String xmTypeParam = StringUtils.trimToNull(request.getParameter("xm_type"));
			if (xmTypeParam == null)
				throw new InteractRuntimeException("xm_type 不能空");
			int xmType = Integer.parseInt(xmTypeParam);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(xmType);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select u.realname,u.student_no,c.name clazz_name,bj.id from t_xm_cddybg bj left join t_xm xm on bj.xm_id=xm.id left join t_user u on xm.student_id=u.id left join t_clazz c on u.clazz_id=c.id where xm.type=? and bj.good_if=1")
							.append(" order by xm.start_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("studentNo", rs.getObject("student_no"));
				item.put("clazzName", rs.getObject("clazz_name"));
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

	@RequestMapping(value = "/xmhdgglist")
	public void hdggList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String xmTypeParam = StringUtils.trimToNull(request.getParameter("xm_type"));
			if (xmTypeParam == null)
				throw new InteractRuntimeException("xm_type 不能空");
			int xmType = Integer.parseInt(xmTypeParam);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(xmType);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select u.realname,u.student_no,c.name clazz_name,bj.id from t_xm_hdgg bj left join t_xm xm on bj.xm_id=xm.id left join t_user u on xm.student_id=u.id left join t_clazz c on u.clazz_id=c.id where xm.type=? and bj.good_if=1")
							.append(" order by xm.start_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("studentNo", rs.getObject("student_no"));
				item.put("clazzName", rs.getObject("clazz_name"));
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

	@RequestMapping(value = "/yqhlist")
	public void yqhList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String xmTypeParam = StringUtils.trimToNull(request.getParameter("xm_type"));
			if (xmTypeParam == null)
				throw new InteractRuntimeException("xm_type 不能空");
			int xmType = Integer.parseInt(xmTypeParam);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(xmType);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select u.realname,u.student_no,c.name clazz_name,bj.id from t_xm_yqh bj left join t_xm xm on bj.xm_id=xm.id left join t_user u on xm.student_id=u.id left join t_clazz c on u.clazz_id=c.id where xm.type=? and bj.good_if=1")
							.append(" order by xm.start_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("studentNo", rs.getObject("student_no"));
				item.put("clazzName", rs.getObject("clazz_name"));
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

	@RequestMapping(value = "/bjungood")
	public void bjUngood(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("update t_xm_bj set good_if=0 where id=?").toString());
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/cddybgungood")
	public void cddybgUngood(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("update t_xm_cddybg set good_if=0 where id=?").toString());
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/hdggungood")
	public void hdggUngood(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("update t_xm_hdgg set good_if=0 where id=?").toString());
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/yqhungood")
	public void yqhUngood(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("update t_xm_yqh set good_if=0 where id=?").toString());
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			JSONObject data = new JSONObject();
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