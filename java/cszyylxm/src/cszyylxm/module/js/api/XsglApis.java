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

@Controller("xs.api.xsgl")
@RequestMapping(value = "/xs/xsgl")
public class XsglApis {

	public static Logger logger = Logger.getLogger(XsglApis.class);

	@RequestMapping(value = "/clazzlist")
	public void clazzList(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					new StringBuilder("select t.id,t.name from t_clazz t where t.teacher_id=? and t.del=0 ")
							.append(" order by t.name desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
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

	@RequestMapping(value = "/addclazz")
	public void addClazz(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(name);
			sqlParams.add(loginStatus.getUserId());
			pst = connection.prepareStatement(
					new StringBuilder("insert into t_clazz (name,teacher_id) values(?,?)").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
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

	@RequestMapping(value = "/delclazz")
	public void delClazz(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("请选择一个目标");
			int id = Integer.parseInt(idParam);
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("密码不能为空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select pwd_md5 from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			String currentPwdMd5 = null;
			if (rs.next()) {
				currentPwdMd5 = rs.getString("pwd_md5");
			} else
				throw new InteractRuntimeException(1404, "账号不存在");

			if (!DigestUtils.md5Hex(pwd).equals(currentPwdMd5))
				throw new InteractRuntimeException("密码错误");

			pst = connection.prepareStatement(new StringBuilder("update t_clazz set del=1 where id=?").toString());
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

	@RequestMapping(value = "/studentlist")
	public void studentList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String clazzIdParam = StringUtils.trimToNull(request.getParameter("clazz_id"));
			if (clazzIdParam == null)
				throw new InteractRuntimeException("clazz_id 不可空");
			int clazzId = Integer.parseInt(clazzIdParam);
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
			sqlParams.add(clazzId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.realname,t.student_no,t.pwd from t_user t where t.clazz_id=? and type=1")
							.append(" order by t.student_no asc limit ?,? ").toString());
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
				item.put("pwd", rs.getObject("pwd"));
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

	@RequestMapping(value = "/addstudent")
	public void addStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			if (realname == null)
				throw new InteractRuntimeException("realname 不可空");
			String studentNo = StringUtils.trimToNull(request.getParameter("student_no"));
			if (studentNo == null)
				throw new InteractRuntimeException("student_no 不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不可空");
			String clazzIdParam = StringUtils.trimToNull(request.getParameter("clazz_id"));
			if (clazzIdParam == null)
				throw new InteractRuntimeException("clazz_id 不可空");
			int clazzId = Integer.parseInt(clazzIdParam);
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder("select id from t_user where account=?").toString());
			pst.setObject(1, studentNo);
			ResultSet rs = pst.executeQuery();
			String existId = null;
			if (rs.next()) {
				existId = rs.getString("id");
			}
			pst.close();
			if (existId != null)
				throw new InteractRuntimeException("账号已存在");

			List sqlParams = new ArrayList();
			sqlParams.add(studentNo);
			sqlParams.add(realname);
			sqlParams.add(studentNo);
			sqlParams.add(clazzId);
			sqlParams.add(pwd);
			sqlParams.add(DigestUtils.md5Hex(pwd));
			sqlParams.add(new Date().getTime());
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_user (account,realname,student_no,clazz_id,pwd,pwd_md5,type,register_time) values(?,?,?,?,?,?,1,?)")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
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

	@RequestMapping(value = "/delstudent")
	public void delStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String id = StringUtils.trimToNull(request.getParameter("id"));
			if (id == null)
				throw new InteractRuntimeException("id 不可空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder("delete from t_user where id=?").toString());
			pst.setObject(1, id);
			pst.executeUpdate();
			pst.close();

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

	@RequestMapping(value = "/xmcountofstudent")
	public void xmCountOfStudent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String id = StringUtils.trimToNull(request.getParameter("id"));
			if (id == null)
				throw new InteractRuntimeException("id 不可空");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 2)
				throw new InteractRuntimeException("不是教师账号");

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("select count(id) from t_xm t where t.student_id=?").toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			if (rs.next()) {
				data.put("count", rs.getObject("count"));
			} else
				throw new InteractRuntimeException("操作失败");
			pst.close();

			// 返回结果
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