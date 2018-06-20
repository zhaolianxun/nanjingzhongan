package cszyylxm.module.js.api;

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

import cszyylxm.business.LoginStatus;
import cszyylxm.util.CszyylxmDataSource;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.InteractRuntimeException;

@Controller("js.api.xszuoye")
@RequestMapping(value = "/js/xszuoye")
public class XsZuoyeApis {

	public static Logger logger = Logger.getLogger(XsZuoyeApis.class);

	@RequestMapping(value = "/ent")
	public void clazzList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String studentId = StringUtils.trimToNull(request.getParameter("student_id"));
			if (studentId == null)
				throw new InteractRuntimeException("student_id 不能空");
			String xmTypeParam = StringUtils.trimToNull(request.getParameter("xm_type"));
			if (xmTypeParam == null)
				throw new InteractRuntimeException("xm_type 不能空");
			int xmType = Integer.parseInt(xmTypeParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select bj.status,bj.id  from t_xm_bj bj left join t_xm xm on bj.xm_id=xm.id where xm.student_id=? and xm.type=?")
							.toString());
			pst.setObject(1, studentId);
			pst.setObject(2, xmType);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("status", rs.getObject("status"));
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

	@RequestMapping(value = "/bjgood")
	public void bjgood(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			pst = connection.prepareStatement(new StringBuilder("update t_xm_bj set good_if=1 where id=?").toString());
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

	@RequestMapping(value = "/cddybggood")
	public void cddybggood(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					.prepareStatement(new StringBuilder("update t_xm_cddybg set good_if=1 where id=?").toString());
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

	@RequestMapping(value = "/hdgggood")
	public void hdgggood(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					.prepareStatement(new StringBuilder("update t_xm_hdgg set good_if=1 where id=?").toString());
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

	@RequestMapping(value = "/yqhgood")
	public void yqhgood(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			pst = connection.prepareStatement(new StringBuilder("update t_xm_yqh set good_if=1 where id=?").toString());
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

	@RequestMapping(value = "/bjscore")
	public void bjscore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String scoreParam = StringUtils.trimToNull(request.getParameter("score"));
			if (scoreParam == null)
				throw new InteractRuntimeException("score 不能空");
			float score = Float.parseFloat(scoreParam);
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("update t_xm_bj set score=? where id=?").toString());
			pst.setObject(1, score);
			pst.setObject(2, id);
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

	@RequestMapping(value = "/cddybgscore")
	public void cddybgscore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String scoreParam = StringUtils.trimToNull(request.getParameter("score"));
			if (scoreParam == null)
				throw new InteractRuntimeException("score 不能空");
			float score = Float.parseFloat(scoreParam);
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("update t_xm_cddybg set score=? where id=?").toString());
			pst.setObject(1, score);
			pst.setObject(2, id);
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

	@RequestMapping(value = "/hdggscore")
	public void hdggscore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String scoreParam = StringUtils.trimToNull(request.getParameter("score"));
			if (scoreParam == null)
				throw new InteractRuntimeException("score 不能空");
			float score = Float.parseFloat(scoreParam);
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("update t_xm_hdgg set score=? where id=?").toString());
			pst.setObject(1, score);
			pst.setObject(2, id);
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

	@RequestMapping(value = "/yqhscore")
	public void yqhscore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String scoreParam = StringUtils.trimToNull(request.getParameter("score"));
			if (scoreParam == null)
				throw new InteractRuntimeException("score 不能空");
			float score = Float.parseFloat(scoreParam);
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("update t_xm_yqh set score=? where id=?").toString());
			pst.setObject(1, score);
			pst.setObject(2, id);
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