package easywin.module.plat.api;

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

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.Business;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

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

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select agent.phone agent_phone,agent.agent_domain,t.businessbase_fill,t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t inner join t_app_seed u on t.seed_id=u.id left join t_user agent on t.from_agent_id=agent.id where t.user_id=? order by t.bind_time desc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray apps = new JSONArray();
			while (rs.next()) {
				JSONObject app = new JSONObject();
				app.put("agentPhone", rs.getObject("agent_phone"));
				app.put("agentDomain", rs.getObject("agent_domain"));
				app.put("appId", rs.getObject("id"));
				app.put("businessbaseFill", rs.getObject("businessbase_fill"));
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
		Jedis jedis = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id不能空");

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			Business.refreshWxAppInfo(appId, connection, jedis);
			jedis.close();
			jedis = null;

			pst = connection.prepareStatement(
					"select t.businessbase_fill,t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t inner join t_app_seed u on t.seed_id=u.id where t.id=?");
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
				app.put("businessbaseFill", rs.getObject("businessbase_fill"));
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
			if (jedis != null)
				jedis.close();
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

			connection = EasywinDataSource.dataSource.getConnection();
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

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 查询应用信息
			pst = connection.prepareStatement(
					"select t.commit_template_version,t.user_id,t.access_token,t.wx_appid,use_endtime,tt.template_code from t_app t left join t_app_seed tt on t.seed_id=tt.id  where t.id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			String commitTemplateVersion = rs.getString("commit_template_version");
			String templateCode = rs.getString("template_code");
			String accessToken = rs.getString("access_token");
			String userId = rs.getString("user_id");
			String wxAppid = rs.getString("wx_appid");
			Long useEndtime = (Long) rs.getObject("use_endtime");
			pst.close();
			// if (StringUtils.isEmpty(commitTemplateVersion) ||
			// !commitTemplateVersion.equals(templateVersion)) {

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
			int wxTemplateId = rs.getInt("wx_templateid");
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
			extJson.put("extEnable", true);
			JSONObject ext = new JSONObject();
			// if (tplCode.equals("mall"))
			ext.put("mallId", appId);
			extJson.put("ext", ext);
			extJson.put("mallId", appId);
			// JSONObject extPages = new JSONObject();
			// extJson.put("extPages", extPages);
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
			// }
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

			connection = EasywinDataSource.dataSource.getConnection();
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
					throw new InteractRuntimeException("该版本已在运营中，无需提交审核。");
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
			if (categoryList == null || categoryList.isEmpty())
				throw new InteractRuntimeException("您的小程序还没有类目，请登录微信公众平台设置");

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
			int wxErrcode = resultVo.getIntValue("errcode");
			if (wxErrcode != 0) {
				if (wxErrcode == 85009) {
					String url1 = new StringBuilder("https://api.weixin.qq.com/wxa/undocodeaudit?")
							.append("access_token=").append(accessToken).toString();
					logger.debug("url " + url);

					Request okHttpRequest1 = new Request.Builder().url(url1).build();
					Response okHttpResponse1 = SysConstant.okHttpClient.newCall(okHttpRequest1).execute();
					String responseBody1 = okHttpResponse1.body().string();
					logger.debug("responseBody " + responseBody1);
					JSONObject resultVo1 = JSON.parseObject(responseBody1);

					if (resultVo1.getIntValue("errcode") != 0)
						throw new InteractRuntimeException(resultVo1.getString("errmsg"));
					throw new InteractRuntimeException("请重新提交");
				} else
					throw new InteractRuntimeException("提交审核失败，微信方错误信息: " + resultVo.getString("errmsg"));
			}
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
			connection = EasywinDataSource.dataSource.getConnection();
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

			StringBuilder optionalCategoryExplainSb = new StringBuilder();
			for (int i = 0; i < categoryList.size(); i++) {
				JSONObject category = categoryList.getJSONObject(i);
				String first = StringUtils.trimToEmpty(category.getString("first_class"));
				String secondClass = StringUtils.trimToEmpty(category.getString("second_class"));
				String thirdClass = StringUtils.trimToEmpty(category.getString("third_class"));
				optionalCategoryExplainSb = optionalCategoryExplainSb.append(",").append(first).append("-")
						.append(secondClass).append(thirdClass);
			}
			String optionalCategoryExplain = "";
			if (optionalCategoryExplainSb.length() > 0)
				optionalCategoryExplain = optionalCategoryExplainSb.substring(1);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("optionalCategoryExplain", optionalCategoryExplain);
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

			connection = EasywinDataSource.dataSource.getConnection();
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
			if (auditstatus == 0)
				throw new InteractRuntimeException("还未提交审核");

			String reason = null;
			if (auditstatus == 1) {
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
				reason = resultVo.getString("reason");
				if (status != 1 && status != 2 && status != 0)
					throw new InteractRuntimeException("未知状态");
				// status 微信方审核状态，其中0为审核成功，1为审核失败，2为审核中
				auditstatus = status == 0 ? 2 : status == 1 ? 3 : 1;
				pst = connection.prepareStatement(
						"update t_app set audit_fail_reason=?,audit_status=? where id=? and audit_status=1");
				pst.setObject(1, reason);
				pst.setObject(2, auditstatus);
				pst.setObject(3, appId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			}
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

	@RequestMapping(value = "/unaudit", method = RequestMethod.POST)
	public void unaudit(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select access_token,audit_status from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			int auditstatus = rs.getInt("audit_status");
			String accessToken = rs.getString("access_token");
			pst.close();

			if (accessToken == null || accessToken.trim().length() == 0)
				throw new InteractRuntimeException("您还未授权该应用");
			if (auditstatus == 0)
				throw new InteractRuntimeException("还未提交审核");
			if (auditstatus == 2 || auditstatus == 3)
				throw new InteractRuntimeException("已审核过");
			if (auditstatus == 1) {
				// 获取小程序的第三方提交代码的页面配置
				String url = new StringBuilder("https://api.weixin.qq.com/wxa/undocodeaudit?").append("access_token=")
						.append(accessToken).toString();
				logger.debug("url " + url);

				Request okHttpRequest = new Request.Builder().url(url).build();
				Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
				String responseBody = okHttpResponse.body().string();
				logger.debug("responseBody " + responseBody);
				JSONObject resultVo = JSON.parseObject(responseBody);
				int wxErrcode = resultVo.getIntValue("errcode");
				if (wxErrcode != 0) {
					if (wxErrcode == 87013)
						throw new InteractRuntimeException("撤回次数达到上限（每天一次，每个月10次）");
					else
						throw new InteractRuntimeException("微信方错误: " + resultVo.getString("errmsg"));
				}

				pst = connection.prepareStatement(
						"update t_app set audit_status=0,audit_template_version=null,wx_audit_id=null where id=? and audit_status=1");
				pst.setObject(1, appId);
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

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					"select audit_template_version,current_template_version,use_endtime,access_token,audit_status,audit_fail_reason from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			int auditstatus = rs.getInt("audit_status");
			String accessToken = rs.getString("access_token");
			String auditFailReason = rs.getString("audit_fail_reason");
			String currentTemplateVersion = rs.getString("current_template_version");
			String auditTemplateVersion = rs.getString("audit_template_version");
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
			if (StringUtils.isNotEmpty(currentTemplateVersion) && StringUtils.isNotEmpty(auditTemplateVersion)
					&& currentTemplateVersion.equals(auditTemplateVersion))
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
			pst = connection.prepareStatement("update t_app set" + (useEndtime == null ? "" : " use_endtime=?,")
					+ " current_template_version=audit_template_version where id=?");
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

	@RequestMapping(value = "/tester/bindtester")
	@Transactional(rollbackFor = Exception.class)
	public void tasterBindtester(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");
			String wechatid = StringUtils.trimToNull(request.getParameter("wechatid"));
			if (wechatid == null)
				throw new InteractRuntimeException("微信号不能空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select authorized,access_token from t_app where id=? for update");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			String accessToken = null;
			int authorized = 0;
			if (rs.next()) {
				accessToken = rs.getString("access_token");
				authorized = rs.getInt("authorized");
			} else
				throw new InteractRuntimeException("目标不存在");
			pst.close();

			if (authorized == 0)
				throw new InteractRuntimeException(1000, "您还未授权该应用", null);

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/bind_tester?").append("access_token=")
					.append(accessToken).toString();
			logger.debug("url " + url);

			JSONObject content = new JSONObject();
			content.put("wechatid", wechatid);
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), content.toJSONString())).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			int errcode = resultVo.getIntValue("errcode");
			if (errcode != 0) {
				if (errcode == 85001)
					throw new InteractRuntimeException("微信号不存在或微信号设置为不可搜索");
				else if (errcode == 85002)
					throw new InteractRuntimeException("小程序绑定的体验者数量达到上限");
				else if (errcode == 85003)
					throw new InteractRuntimeException("微信号绑定的小程序体验者达到上限");
				else if (errcode == 85004)
					throw new InteractRuntimeException("微信号已经绑定");
				else if (errcode == -1)
					throw new InteractRuntimeException("系统繁忙");
				else
					throw new InteractRuntimeException("微信方出错");
			}
			String userstr = resultVo.getString("userstr");
			pst = connection.prepareStatement("insert into t_app_taster (wechatid,wx_userstr,app_id) values(?,?,?)");
			pst.setObject(1, wechatid);
			pst.setObject(2, userstr);
			pst.setObject(3, appId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("userstr", resultVo.getString("userstr"));
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

	@RequestMapping(value = "/tester/unbindtester")
	@Transactional(rollbackFor = Exception.class)
	public void tasterUnbindtester(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					"select a.authorized,a.access_token,at.wechatid from t_app_taster at left join t_app a on at.app_id=a.id where at.id=?");
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			String accessToken = null;
			int authorized = 0;
			String wechatid = null;
			if (rs.next()) {
				accessToken = rs.getString("access_token");
				authorized = rs.getInt("authorized");
				wechatid = rs.getString("wechatid");
			} else
				throw new InteractRuntimeException("目标不存在");
			pst.close();

			if (authorized == 0)
				throw new InteractRuntimeException(1000, "您还未授权该应用", null);

			// 发布
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/unbind_tester?").append("access_token=")
					.append(accessToken).toString();
			logger.debug("url " + url);

			JSONObject content = new JSONObject();
			content.put("wechatid", wechatid);
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), content.toJSONString())).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			int errcode = resultVo.getIntValue("errcode");
			if (errcode != 0) {
				if (errcode == -1)
					throw new InteractRuntimeException("系统繁忙");
				else
					throw new InteractRuntimeException("微信方出错");
			}

			pst = connection.prepareStatement("delete from t_app_taster where id=?");
			pst.setObject(1, id);
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

	@RequestMapping(value = "/tester/ent")
	@Transactional(rollbackFor = Exception.class)
	public void tasterEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id 不能为空");
			String wechatid = StringUtils.trimToNull(request.getParameter("wechatid"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(appId);
			if (wechatid != null)
				sqlParams.add(new StringBuilder("%").append(wechatid).append("%").toString());
			pst = connection.prepareStatement(new StringBuilder("select id,wechatid from t_app_taster where app_id=? ")
					.append(wechatid == null ? "" : " and wechatid like ?").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("wechatid", rs.getObject("wechatid"));
				items.add(item);
			}
			pst.close();

			JSONObject data = new JSONObject();
			data.put("items", items);
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