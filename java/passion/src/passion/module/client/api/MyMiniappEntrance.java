package passion.module.client.api;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import passion.constant.SysConstant;
import passion.dao.entity.HpHospitalUser;
import passion.dao.entity.WxMiniapp;
import passion.dao.entity.WxMiniappTemplate;
import passion.dao.mapper.HpHospitalMapper;
import passion.dao.mapper.HpHospitalUserMapper;
import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("controller.client.MyMiniappEntrance")
@RequestMapping(value = "/c/myminiapp")
public class MyMiniappEntrance extends BaseController {

	public static Logger logger = Logger.getLogger(MyMiniappEntrance.class);
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManagementMapper;
	@Autowired
	protected AppShopEntranceMapper appShopModuleMapper;
	@Autowired
	protected MyMiniappEntranceMapper myMiniappMapper;
	@Autowired
	protected UserManageMapper userManageMapper;
	@Autowired
	protected HpHospitalUserMapper hpHospitalUserMapper;
	@Autowired
	protected HpHospitalMapper hpHospitalMapper;

	@RequestMapping(value = "/apps", method = RequestMethod.POST)
	public void apps(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取并处理参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			Long totalItemCount = myMiniappMapper.appCount(loginStatus.getUserId());
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> apps = myMiniappMapper.apps(loginStatus.getUserId(), pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject appsJson = new JSONObject();
			appsJson.put("items", apps);
			appsJson.put("pageNo", pagingPage.getPageNo());
			appsJson.put("pageSize", pagingPage.getPageSize());
			appsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			appsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("apps", appsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/pretasteqrcode", method = RequestMethod.GET)
	public void getPreTasteQrcode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String accessToken = myMiniappMapper.getAccessTokenById(miniappId);

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
		}

	}

	@RequestMapping(value = "/commitcode", method = RequestMethod.POST)
	public void commitCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId 不能为空");
			String templateId = StringUtils.trimToNull(request.getParameter("template_id"));
			if (templateId == null)
				throw new InteractRuntimeException("template_id 不能为空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			WxMiniappTemplate template = wxMiniappTemplateMapper.selectByPrimaryKey(templateId);

			MyMiniappEntranceMapper.GetCommitCodeInfoById commitCodeInfo = myMiniappMapper
					.getCommitCodeInfoById(miniappId);

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/commit?").append("access_token=")
					.append(commitCodeInfo.accessToken).toString();
			logger.debug(url);
			JSONObject content = new JSONObject();
			content.put("template_id", template.getWxTemplateId());
			content.put("user_version", template.getUserVersion());
			content.put("user_desc", template.getUserDesc());
			JSONObject extJson = new JSONObject();
			extJson.put("extAppid", commitCodeInfo.appId);

			JSONObject ext = new JSONObject();
			ext.put("miniappId", miniappId);
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

			myMiniappMapper.codeCommitted(miniappId, templateId);

			// 返回结果
			JSONObject data = new JSONObject();
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/submitaudit", method = RequestMethod.POST)
	public void submitAudit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			WxMiniapp miniapp = wxMiniappMapper.selectByPrimaryKey(miniappId);
			if (miniapp.getCodeCommit() == 0 || StringUtils.isEmpty(miniapp.getCommittedTemplateId()))
				throw new InteractRuntimeException("请先提交版本");
			if (miniapp.getSubmitAudit() == 1 && 0 == miniapp.getAuditStatus())
				throw new InteractRuntimeException("有正在审核中的版本");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_page?").append("access_token=")
					.append(miniapp.getAccessToken()).toString();
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
					.append(miniapp.getAccessToken()).toString();
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
					.append(miniapp.getAccessToken()).toString();

			JSONObject content = new JSONObject();
			JSONArray itemList = new JSONArray();
			JSONObject itemListEle = new JSONObject();
			itemListEle.put("address", pagelist.getString(0));
			itemListEle.put("tag", "健康");
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

			myMiniappMapper.submitAudit(miniappId, auditId);

			// 返回结果
			JSONObject data = new JSONObject();
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/getoptionalcategory", method = RequestMethod.POST)
	public void getOptionalCategory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniapp_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			MyMiniappEntranceMapper.GetGetAuditStatusInfoById info = myMiniappMapper
					.getGetAuditStatusInfoById(miniappId);
			if (!loginStatus.getUserId().equals(info.userId))
				throw new InteractRuntimeException("不是您的小程序");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_category?").append("access_token=")
					.append(info.accessToken).toString();
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
			throw e;
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/getauditstatus", method = RequestMethod.POST)
	public void getAuditStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniapp_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			MyMiniappEntranceMapper.GetGetAuditStatusInfoById info = myMiniappMapper
					.getGetAuditStatusInfoById(miniappId);
			if (!loginStatus.getUserId().equals(info.userId))
				throw new InteractRuntimeException("不是您的小程序");

			if (info.auditId == null)
				throw new InteractRuntimeException("还未提交审核");

			// 获取小程序的第三方提交代码的页面配置
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/get_auditstatus?").append("access_token=")
					.append(info.accessToken).toString();
			logger.debug("url " + url);
			JSONObject content = new JSONObject();
			content.put("auditid", info.auditId);

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

			myMiniappMapper.updateAuditInfoByAuditId(status, reason, info.auditId);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("status", status);
			data.put("reason", reason);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
			throw e;
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/release", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void release(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			WxMiniapp miniapp = wxMiniappMapper.selectByPrimaryKey(miniappId);
			if (miniapp.getSubmitAudit() != 1)
				throw new InteractRuntimeException("请先提交审核");
			if (miniapp.getAuditStatus() != 0)
				throw new InteractRuntimeException("还未审核通过");

			// 发布
			String url = new StringBuilder("https://api.weixin.qq.com/wxa/release?").append("access_token=")
					.append(miniapp.getAccessToken()).toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), "{}")).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			myMiniappMapper.release(miniappId);

			if (miniapp.getUseEndtime() == null || miniapp.getUseEndtime() == 0)
				miniapp.setUseEndtime(new Date().getTime() + 7 * 24 * 60 * 60 * 1000l);
			wxMiniappMapper.updateByPrimaryKeySelective(miniapp);

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
			throw e;
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/entermanagement", method = RequestMethod.POST)
	public void enterManagement(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniapp_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			MyMiniappEntranceMapper.GetMiniappManagementInfo info = myMiniappMapper.getMiniappManagementInfo(miniappId);
			if (info == null)
				throw new InteractRuntimeException("小程序不存在");
			if (!info.userId.equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("只能进入自己的小程序管理平台");

			// 登录
			if ("1".equals(info.shopappId)) {
				HpHospitalUser user = myMiniappMapper.getHospitalPublicityHospitalSuperadminUser(miniappId);

				if (user.getIfLoginable() != 1)
					throw new InteractRuntimeException("禁止登陆:" + user.getLoginunableReason());
				if (user.getIfSuperadmin() != 1)
					throw new InteractRuntimeException("您不是超级管理员");

				passion.module.hospitalpublicity.hospital.entity.UserLoginStatus status = new passion.module.hospitalpublicity.hospital.entity.UserLoginStatus(
						user.getId(), user.getUsername(), user.getPhone(), user.getNickname(), user.getIfSuperadmin(),
						user.getIfAdmin(), user.getHospitalId(), new Date());

				request.getSession(true).setAttribute("HospitalPublicityHospitalUserLoginStatus", status);
			}
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

	public static void main(String[] args) {
		JSONObject content = new JSONObject();
		content.put("template_id", Integer.parseInt("1"));
		System.out.println(content.toJSONString());
	}
}
