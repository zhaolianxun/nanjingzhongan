package cszyylxm.module.xs.api;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cszyylxm.business.LoginStatus;
import cszyylxm.util.CszyylxmDataSource;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.InteractRuntimeException;

@Controller("xs.api.xsxm")
@RequestMapping(value = "/xs/xsxm")
public class XsxmApis {

	public static Logger logger = Logger.getLogger(XsxmApis.class);

	@RequestMapping(value = "/ent")
	public void ent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("insert into t_xm (student_id,type) values(?,1)",
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int xmId = rs.getInt(1);
			pst.close();

			pst = connection.prepareStatement("insert into t_xm_bj (xm_id) values(?)");
			pst.setObject(1, xmId);
			n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/bj/ent")
	public void bjEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select bj.id bj_id,bj.intro,bj.`status` from t_xm_bj bj left join t_xm xm on bj.xm_id=xm.id where xm.student_id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			Integer bjId = null;
			if (rs.next()) {
				bjId = (Integer) rs.getObject("bj_id");
				data.put("bj_id", rs.getObject("bjId"));
				data.put("intro", rs.getObject("intro"));
				data.put("status", rs.getObject("status"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在");
			pst.close();

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_bj_files t where t.type=1");
			pst.setObject(1, bjId);
			rs = pst.executeQuery();
			JSONArray docs = new JSONArray();
			while (rs.next()) {
				JSONObject doc = new JSONObject();
				doc.put("id", rs.getObject("id"));
				doc.put("name", rs.getObject("name"));
				doc.put("url", rs.getObject("url"));
				docs.add(doc);
			}
			pst.close();
			data.put("docs", docs);

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_bj_files t where t.type=2");
			pst.setObject(1, bjId);
			rs = pst.executeQuery();
			JSONArray videos = new JSONArray();
			while (rs.next()) {
				JSONObject video = new JSONObject();
				video.put("id", rs.getObject("id"));
				video.put("name", rs.getObject("name"));
				video.put("url", rs.getObject("url"));
				videos.add(video);
			}
			pst.close();
			data.put("videos", videos);

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

	@RequestMapping(value = "/bj/submit")
	public void bjSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String intro = StringUtils.trimToNull(request.getParameter("intro"));
			if (intro == null)
				throw new InteractRuntimeException("简介不可空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_xm_bj set intro=? where id=?");
			pst.setObject(1, intro);
			pst.setObject(2, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

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

	@RequestMapping(value = "/bj/delfile")
	public void bjDelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("请选择一个目标");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("delete from t_xm_bj_files where id=?");
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

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

	@RequestMapping(value = "/bj/addfile")
	public void bjAddFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String xmBjIdParam = StringUtils.trimToNull(request.getParameter("xm_bj_id"));
			if (xmBjIdParam == null)
				throw new InteractRuntimeException("xm_bj_id 不可空");
			int xmBjId = Integer.parseInt(xmBjIdParam);
			String url = StringUtils.trimToNull(request.getParameter("url"));
			if (url == null)
				throw new InteractRuntimeException("url不可空");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("文件名不可空");
			String typeParam = StringUtils.trimToNull(request.getParameter("type"));
			if (typeParam == null)
				throw new InteractRuntimeException("文件类型不可空");
			int type = Integer.parseInt(typeParam);
			if (type != 1 && type != 2)
				throw new InteractRuntimeException("文件类型有误");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("insert into t_xm_bj_files (xm_bj_id,name,url,type) values(?,?,?,?)");
			pst.setObject(1, xmBjId);
			pst.setObject(2, name);
			pst.setObject(3, url);
			pst.setObject(4, type);
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

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