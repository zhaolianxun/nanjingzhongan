package rrightway.module.plat.api;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Controller("plat.api.MyAppEntrance")
@RequestMapping(value = "/p/e/myapp")
public class MyAppEntrance {

	public static Logger logger = Logger.getLogger(MyAppEntrance.class);

	@RequestMapping(value = "/apps")
	public void apps(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t inner join t_app_seed u on t.seed_id=u.id where t.user_id=? order by t.bind_time desc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray apps = new JSONArray();
			while (rs.next()) {
				JSONObject app = new JSONObject();
				app.put("appId", rs.getObject("id"));
				app.put("headImg", rs.getObject("head_img"));
				app.put("seedId", rs.getObject("seed_id"));
				app.put("nickName", rs.getObject("nick_name"));
				app.put("currentTemplateVersion", rs.getObject("current_template_version"));
				app.put("authorized", rs.getObject("authorized"));
				app.put("auditTemplateVersion", rs.getObject("audit_template_version"));
				app.put("commitTemplateVersion", rs.getObject("commit_template_version"));
				app.put("bindTime", rs.getObject("bind_time"));
				app.put("commitStatus", rs.getObject("commit_status"));
				app.put("auditStatus", rs.getObject("audit_status"));
				app.put("auditFailReason", rs.getObject("audit_fail_reason"));
				app.put("newestTemplateVersion", rs.getObject("newest_template_version"));
				app.put("useEndtime", rs.getObject("use_endtime"));
				app.put("templateCode", rs.getObject("template_code"));
				apps.add(app);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("apps", apps);
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

	@RequestMapping(value = "/appinfo")
	public void appinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id不能空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t inner join t_app_seed u on t.seed_id=u.id where t.id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			JSONObject app = new JSONObject();
			while (rs.next()) {
				app.put("appId", rs.getObject("id"));
				app.put("headImg", rs.getObject("head_img"));
				app.put("seedId", rs.getObject("seed_id"));
				app.put("nickName", rs.getObject("nick_name"));
				app.put("currentTemplateVersion", rs.getObject("current_template_version"));
				app.put("authorized", rs.getObject("authorized"));
				app.put("auditTemplateVersion", rs.getObject("audit_template_version"));
				app.put("commitTemplateVersion", rs.getObject("commit_template_version"));
				app.put("bindTime", rs.getObject("bind_time"));
				app.put("commitStatus", rs.getObject("commit_status"));
				app.put("auditStatus", rs.getObject("audit_status"));
				app.put("auditFailReason", rs.getObject("audit_fail_reason"));
				app.put("newestTemplateVersion", rs.getObject("newest_template_version"));
				app.put("useEndtime", rs.getObject("use_endtime"));
				app.put("templateCode", rs.getObject("template_code"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(app);
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

	@RequestMapping(value = "/pretasteqrcode", method = RequestMethod.GET)
	public void getPreTasteQrcode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select access_token from t_app where id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new InteractRuntimeException("应用不存在");

			String accessToken = rs.getString("access_token");
			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			pst.close();

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_qrcode?").append("access_token=")
					.append(accessToken).toString();

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();

			// 返回结果
			OutputStream out = response.getOutputStream();
			IOUtils.copy(okHttpResponse.body().byteStream(), out);
			out.flush();
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

	@RequestMapping(value = "/commitcode", method = RequestMethod.POST)
	public void commitCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");
			String templateVersion = StringUtils.trimToNull(request.getParameter("template_version"));
			if (templateVersion == null)
				throw new InteractRuntimeException("template_version 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 查询应用信息
			pst = connection.prepareStatement(
					"select t.user_id,t.access_token,t.wx_appid,use_endtime,tt.template_code from t_app t left join t_app_seed tt on t.seed_id=tt.id  where t.id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			String templateCode = rs.getString("template_code");
			String accessToken = rs.getString("access_token");
			String userId = rs.getString("user_id");
			String wxAppid = rs.getString("wx_appid");
			Long useEndtime = (Long) rs.getObject("use_endtime");
			pst.close();
			if (!userId.equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("这不是您的应用");
			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			if (useEndtime == null || useEndtime <= new Date().getTime())
				throw new InteractRuntimeException("你的使用权已到期");
			// 查询版本信息
			pst = connection.prepareStatement(
					"select t.wx_templateid,t.tpl_version,t.tpl_code from t_seed_template t  where t.tpl_code=? and t.tpl_version=?");
			pst.setObject(1, templateCode);
			pst.setObject(2, templateVersion);
			rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("模板不存在");
			}
			String wxTemplateId = rs.getString("wx_templateid");
			String version = rs.getString("tpl_version");
			String tplCode = rs.getString("tpl_code");
			pst.close();

			// 更新应用信息
			pst = connection.prepareStatement("update t_app set commit_template_version=?,commit_status=1 where id=?");
			pst.setObject(1, version);
			pst.setObject(2, appId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 提交到微信
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/commit?").append("access_token=")
					.append(accessToken).toString();
			logger.debug(url);
			JSONObject content = new JSONObject();
			content.put("template_id", wxTemplateId);
			content.put("user_version", version);
			content.put("user_desc", tplCode);
			JSONObject extJson = new JSONObject();
			extJson.put("extAppid", wxAppid);
			JSONObject ext = new JSONObject();
			if (tplCode.equals("mall"))
				ext.put("mallId", appId);
			extJson.put("ext", ext);
			JSONObject extPages = new JSONObject();
			extJson.put("extPages", extPages);
			content.put("ext_json", extJson.toJSONString());
			String contentStr = content.toJSONString();
			logger.debug(contentStr);
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), contentStr)).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

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

	@RequestMapping(value = "/submitaudit", method = RequestMethod.POST)
	public void submitAudit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					"select user_id,access_token,commit_status,audit_status,current_template_version,commit_template_version from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			String userId = rs.getString("user_id");
			Integer commitStatus = rs.getInt("commit_status");
			Integer auditStatus = rs.getInt("audit_status");
			String commitTemplateVersion = rs.getString("commit_template_version");
			String currentTemplateVersion = rs.getString("current_template_version");
			String accessToken = rs.getString("access_token");
			pst.close();

			if (!userId.equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("这不是您的应用");
			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			if (commitStatus == 0)
				throw new InteractRuntimeException("请先提交版本");
			else {
				if (commitTemplateVersion.equals(currentTemplateVersion))
					throw new InteractRuntimeException("当前版本号与提交的版本号相同");
			}
			if (auditStatus == 1)
				throw new InteractRuntimeException("有正在审核中的版本");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_page?").append("access_token=")
					.append(accessToken).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			JSONArray pagelist = resultVo.getJSONArray("page_list");

			// 获取授权小程序帐号的可选类目
			url = new StringBuilder("https://api.weixin.qq.com/wxa/get_category?").append("access_token=")
					.append(accessToken).toString();
			okHttpRequest = new Request.Builder().url(url).build();
			okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			JSONArray categoryList = resultVo.getJSONArray("category_list");

			// 提交审核
			url = new StringBuilder("https://api.weixin.qq.com/wxa/submit_audit?").append("access_token=")
					.append(accessToken).toString();

			JSONObject content = new JSONObject();
			JSONArray itemList = new JSONArray();
			JSONObject itemListEle = new JSONObject();
			itemListEle.put("address", pagelist.getString(0));
			itemListEle.put("tag", "展示");
			itemListEle.put("title", "首页");
			itemListEle.putAll(categoryList.getJSONObject(0));
			itemList.add(itemListEle);
			content.put("item_list", itemList);
			String contentStr = content.toJSONString();
			logger.debug(contentStr);
			okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), contentStr)).build();
			okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			long auditId = resultVo.getLongValue("auditid");

			pst = connection.prepareStatement(
					"update t_app set audit_template_version=commit_template_version,audit_status=1,audit_fail_reason=null,wx_audit_id=? where id=?");
			pst.setObject(1, auditId);
			pst.setObject(2, appId);
			int n = pst.executeUpdate();
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

	@RequestMapping(value = "/getoptionalcategory", method = RequestMethod.POST)
	public void getOptionalCategory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 获取应用信息
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select t.access_token from t_app t  where t.id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			String accessToken = rs.getString("access_token");
			pst.close();
			connection.close();
			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_category?").append("access_token=")
					.append(accessToken).toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			JSONArray categoryList = resultVo.getJSONArray("category_list");

			StringBuilder optionalCategoryExplain = new StringBuilder();
			for (int i = 0; i < categoryList.size(); i++) {
				JSONObject category = categoryList.getJSONObject(i);
				String first = StringUtils.trimToEmpty(category.getString("first_class"));
				String secondClass = StringUtils.trimToEmpty(category.getString("second_class"));
				String thirdClass = StringUtils.trimToEmpty(category.getString("third_class"));
				optionalCategoryExplain = optionalCategoryExplain.append(first).append("-").append(secondClass)
						.append("-").append(thirdClass).append("\n");
			}

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("optionalCategoryExplain", optionalCategoryExplain.toString());
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

	@RequestMapping(value = "/getauditstatus", method = RequestMethod.POST)
	public void getAuditStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection
					.prepareStatement("select access_token,audit_status,wx_audit_id from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			int auditstatus = rs.getInt("audit_status");
			Integer wxAuditId = (Integer) rs.getObject("wx_audit_id");
			String accessToken = rs.getString("access_token");
			pst.close();

			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			if (auditstatus == 0 || wxAuditId == null)
				throw new InteractRuntimeException("还未提交审核");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_auditstatus?").append("access_token=")
					.append(accessToken).toString();
			logger.debug("url " + url);
			JSONObject content = new JSONObject();
			content.put("auditid", wxAuditId);

			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), content.toJSONString())).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			int status = resultVo.getIntValue("status");
			String reason = resultVo.getString("reason");
			if (status != 1 && status != 2 && status != 0)
				throw new InteractRuntimeException("未知状态");

			pst = connection.prepareStatement("update t_app set audit_fail_reason=?,audit_status=? where id=?");
			pst.setObject(1, reason);
			pst.setObject(2, status == 0 ? 2 : status == 1 ? 3 : 1);
			pst.setObject(3, appId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("status", auditstatus);
			data.put("reason", reason);
			HttpRespondWithData.todo(request, response, 0, null, data);
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

	@RequestMapping(value = "/release", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void release(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					"select use_endtime,access_token,audit_status,audit_fail_reason from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			int auditstatus = rs.getInt("audit_status");
			String accessToken = rs.getString("access_token");
			String auditFailReason = rs.getString("audit_fail_reason");
			Long useEndtime = (Long) rs.getObject("use_endtime");
			pst.close();

			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			if (auditstatus == 0)
				throw new InteractRuntimeException("请先提交审核");
			else if (auditstatus == 1)
				throw new InteractRuntimeException("正在审核中");
			else if (auditstatus == 3)
				throw new InteractRuntimeException(new StringBuilder("审核失败 ").append(auditFailReason).toString());
			else if (auditstatus == 4)
				throw new InteractRuntimeException("当前版本已发布");

			// 发布
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/release?").append("access_token=")
					.append(accessToken).toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), "{}")).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			if (useEndtime == null)
				useEndtime = new Date().getTime() + 7 * 24 * 60 * 60 * 1000l;
			else
				useEndtime = null;

			List params = new ArrayList();
			if (useEndtime != null)
				params.add(useEndtime);
			params.add(appId);
			pst = connection.prepareStatement("update t_app set" + (useEndtime == null ? "" : "use_endtime=?,")
					+ " audit_status=4,current_template_version=audit_template_version where id=?");
			for (int i = 0; i < params.size(); i++) {
				pst.setObject(i + 1, params.get(i));
			}
			int n = pst.executeUpdate();
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

}