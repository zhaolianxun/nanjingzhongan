package cszyylxm.module.xs.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

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
			pst = connection.prepareStatement("select id from t_xm where student_id=? and type=1");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			Integer xmId = null;
			if (rs.next()) {
				xmId = (Integer) rs.getObject("id");
			}
			pst.close();

			if (xmId == null) {
				pst = connection.prepareStatement("insert into t_xm (student_id,type) values(?,1)",
						Statement.RETURN_GENERATED_KEYS);
				pst.setObject(1, loginStatus.getUserId());
				int n = pst.executeUpdate();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
				rs = pst.getGeneratedKeys();
				rs.next();
				xmId = rs.getInt(1);
				pst.close();
			}

			pst = connection.prepareStatement("select id from t_xm_bj where xm_id=?");
			pst.setObject(1, xmId);
			rs = pst.executeQuery();
			Integer bjId = null;
			if (rs.next()) {
				bjId = (Integer) rs.getObject("id");
			}
			pst.close();

			if (bjId == null) {
				pst = connection.prepareStatement("insert into t_xm_bj (xm_id) values(?)");
				pst.setObject(1, xmId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

			}

			pst = connection.prepareStatement("select id from t_xm_cddybg where xm_id=?");
			pst.setObject(1, xmId);
			rs = pst.executeQuery();
			Integer cddybgId = null;
			if (rs.next()) {
				cddybgId = (Integer) rs.getObject("id");
			}
			pst.close();

			if (bjId == null) {
				pst = connection.prepareStatement("insert into t_xm_cddybg (xm_id) values(?)");
				pst.setObject(1, cddybgId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

			}

			pst = connection.prepareStatement("select id from t_xm_hdgg where xm_id=?");
			pst.setObject(1, xmId);
			rs = pst.executeQuery();
			Integer hdggId = null;
			if (rs.next()) {
				hdggId = (Integer) rs.getObject("id");
			}
			pst.close();

			if (bjId == null) {
				pst = connection.prepareStatement("insert into t_xm_hdgg (xm_id) values(?)");
				pst.setObject(1, hdggId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

			}

			pst = connection.prepareStatement("select id from t_xm_yqh where xm_id=?");
			pst.setObject(1, xmId);
			rs = pst.executeQuery();
			Integer yqhId = null;
			if (rs.next()) {
				yqhId = (Integer) rs.getObject("id");
			}
			pst.close();

			if (bjId == null) {
				pst = connection.prepareStatement("insert into t_xm_yqh (xm_id) values(?)");
				pst.setObject(1, yqhId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

			}
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
				data.put("bjId", bjId);
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
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id不可空");
			int id = Integer.parseInt(idParam);

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
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("update t_xm_bj set intro=? where id=?");
			pst.setObject(1, intro);
			pst.setObject(2, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(
					"update t_xm_bj bj left join t_xm xm on bj.xm_id=xm.id set xm.start_time=? where bj.id=?");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, id);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
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
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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
			String bjIdParam = StringUtils.trimToNull(request.getParameter("bj_id"));
			if (bjIdParam == null)
				throw new InteractRuntimeException("bj_id 不可空");
			int bjId = Integer.parseInt(bjIdParam);
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
			pst = connection.prepareStatement("insert into t_xm_bj_files (bj_id,name,url,type) values(?,?,?,?)");
			pst.setObject(1, bjId);
			pst.setObject(2, name);
			pst.setObject(3, url);
			pst.setObject(4, type);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/cddybg/ent")
	public void cddybgEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select bj.id cddybg_id,bj.intro,bj.`status` from t_xm_cddybg bj left join t_xm xm on bj.xm_id=xm.id where xm.student_id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			Integer cddybgId = null;
			if (rs.next()) {
				cddybgId = (Integer) rs.getObject("cddybg_id");
				data.put("cddybgId", cddybgId);
				data.put("intro", rs.getObject("intro"));
				data.put("status", rs.getObject("status"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在");
			pst.close();

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_cddybg_files t where t.type=1");
			pst.setObject(1, cddybgId);
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

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_cddybg_files t where t.type=2");
			pst.setObject(1, cddybgId);
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

	@RequestMapping(value = "/cddybg/submit")
	public void cddybgSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id不可空");
			int id = Integer.parseInt(idParam);
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
			pst = connection.prepareStatement("update t_xm_cddybg set intro=? where id=?");
			pst.setObject(1, intro);
			pst.setObject(2, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/cddybg/delfile")
	public void cddybgDelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			pst = connection.prepareStatement("delete from t_xm_cddybg_files where id=?");
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/cddybg/addfile")
	public void cddybgAddFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String cddybgIdParam = StringUtils.trimToNull(request.getParameter("cddybg_id"));
			if (cddybgIdParam == null)
				throw new InteractRuntimeException("cddybg_id 不可空");
			int cddybgId = Integer.parseInt(cddybgIdParam);
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
			pst = connection
					.prepareStatement("insert into t_xm_cddybg_files (cddybg_id,name,url,type) values(?,?,?,?)");
			pst.setObject(1, cddybgId);
			pst.setObject(2, name);
			pst.setObject(3, url);
			pst.setObject(4, type);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/hdgg/ent")
	public void hdggEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select bj.id hdgg_id,bj.intro,bj.`status` from t_xm_hdgg bj left join t_xm xm on bj.xm_id=xm.id where xm.student_id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			Integer hdggId = null;
			if (rs.next()) {
				hdggId = (Integer) rs.getObject("hdgg_id");
				data.put("hdggId", hdggId);
				data.put("intro", rs.getObject("intro"));
				data.put("status", rs.getObject("status"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在");
			pst.close();

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_hdgg_files t where t.type=1");
			pst.setObject(1, hdggId);
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

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_hdgg_files t where t.type=3");
			pst.setObject(1, hdggId);
			rs = pst.executeQuery();
			JSONArray pics = new JSONArray();
			while (rs.next()) {
				JSONObject pic = new JSONObject();
				pic.put("id", rs.getObject("id"));
				pic.put("name", rs.getObject("name"));
				pic.put("url", rs.getObject("url"));
				pics.add(pic);
			}
			pst.close();
			data.put("pics", pics);

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

	@RequestMapping(value = "/hdgg/submit")
	public void hdggSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id不可空");
			int id = Integer.parseInt(idParam);
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
			pst = connection.prepareStatement("update t_xm_hdgg set intro=? where id=?");
			pst.setObject(1, intro);
			pst.setObject(2, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/hdgg/delfile")
	public void hdggDelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			pst = connection.prepareStatement("delete from t_xm_hdgg_files where id=?");
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/hdgg/addfile")
	public void hdggAddFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hdggIdParam = StringUtils.trimToNull(request.getParameter("hdgg_id"));
			if (hdggIdParam == null)
				throw new InteractRuntimeException("cddybg_id 不可空");
			int hdggId = Integer.parseInt(hdggIdParam);
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
			if (type != 1 && type != 3)
				throw new InteractRuntimeException("文件类型有误");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("insert into t_xm_hdgg_files (hdgg_id,name,url,type) values(?,?,?,?)");
			pst.setObject(1, hdggId);
			pst.setObject(2, name);
			pst.setObject(3, url);
			pst.setObject(4, type);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/yqh/ent")
	public void yqhEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select bj.id yqh_id,bj.intro,bj.`status` from t_xm_yqh bj left join t_xm xm on bj.xm_id=xm.id where xm.student_id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			Integer yqhId = null;
			if (rs.next()) {
				yqhId = (Integer) rs.getObject("yqh_id");
				data.put("yqhId", yqhId);
				data.put("intro", rs.getObject("intro"));
				data.put("status", rs.getObject("status"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在");
			pst.close();

			pst = connection.prepareStatement("select t.id,t.name,t.url from t_xm_yqh_files t where t.type=1");
			pst.setObject(1, yqhId);
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

	@RequestMapping(value = "/yqh/submit")
	public void yqhSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id不可空");
			int id = Integer.parseInt(idParam);
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
			pst = connection.prepareStatement("update t_xm_yqh set intro=? where id=?");
			pst.setObject(1, intro);
			pst.setObject(2, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/yqh/delfile")
	public void yqhDelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			pst = connection.prepareStatement("delete from t_xm_yqh_files where id=?");
			pst.setObject(1, id);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/yqh/addfile")
	public void yqhAddFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String yqhIdParam = StringUtils.trimToNull(request.getParameter("yqh_id"));
			if (yqhIdParam == null)
				throw new InteractRuntimeException("yqh_id 不可空");
			int yqhId = Integer.parseInt(yqhIdParam);
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
			if (type != 1)
				throw new InteractRuntimeException("文件类型有误");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getType() != 1)
				throw new InteractRuntimeException("不是学生账号");

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("insert into t_xm_yqh_files (hdgg_id,name,url,type) values(?,?,?,?)");
			pst.setObject(1, yqhId);
			pst.setObject(2, name);
			pst.setObject(3, url);
			pst.setObject(4, type);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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