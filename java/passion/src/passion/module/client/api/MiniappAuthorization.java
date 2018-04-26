package passion.module.client.api;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import passion.constant.SysConstant;
import passion.dao.entity.HpHospital;
import passion.dao.entity.HpHospitalUser;
import passion.dao.entity.WxMiniapp;
import passion.dao.entity.WxMiniappTemplate;
import passion.dao.mapper.HpHospitalMapper;
import passion.dao.mapper.HpHospitalUserMapper;
import passion.entity.InteractException;
import passion.entity.InteractRuntimeException;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("controller.client.MiniappAuthorization")
@RequestMapping(value = "/c/miniappauth")
public class MiniappAuthorization extends BaseController {

	public static Logger logger = Logger.getLogger(MiniappAuthorization.class);
	@Autowired
	protected HpHospitalUserMapper hpHospitalUserMapper;
	@Autowired
	protected HpHospitalMapper hpHospitalMapper;
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManageMapper;
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManagementMapper;
	@Autowired
	protected AppShopEntranceMapper appShopModuleMapper;
	@Autowired
	protected MyMiniappEntranceMapper myMiniappMapper;
	@Autowired
	protected UserManageMapper userManageMapper;

	@RequestMapping(value = "/eventnotify", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void eventNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			String signature = request.getParameter("signature");
			String timestamp = request.getParameter("timestamp");
			String nonce = request.getParameter("nonce");
			String msgSignature = request.getParameter("msg_signature");
			String encryptType = request.getParameter("encrypt_type");

			// 获取请求参数
			String charset = StringUtils.isEmpty(request.getCharacterEncoding()) ? SysConstant.SYS_CHARSET
					: request.getCharacterEncoding();
			String reqData = IOUtils.toString(request.getInputStream(), charset);
			logger.debug(reqData);

			WXBizMsgCrypt pc = new WXBizMsgCrypt(SysConstant.wechat_open_thirdparty_MsgVerificationToken,
					SysConstant.wechat_open_thirdparty_MsgEncryptAndDecryptKey,
					SysConstant.wechat_open_thirdparty_AppId);
			String result2 = pc.decryptMsg(msgSignature, timestamp, nonce, reqData);
			logger.debug("解密后明文: " + result2);

			SAXReader reader = new SAXReader();
			Document doc = reader.read(new ByteArrayInputStream(result2.getBytes(charset)));
			Element root = doc.getRootElement();
			String infoType = root.elementTextTrim("InfoType");
			if (infoType.equals("component_verify_ticket")) {
				String componentVerifyTicket = root.elementTextTrim("ComponentVerifyTicket");
				jedis = SysConstant.jedisPool.getResource();
				jedis.set(SysConstant.wechat_open_thirdparty_AppId + "-componentVerifyTicket", componentVerifyTicket);
			} else if (infoType.equals("unauthorized")) {
				String authorizerAppid = root.elementTextTrim("AuthorizerAppid");
				myMiniappMapper.unauthorized(authorizerAppid);
			} else {
				logger.debug("未实现的业务");
			}

			// 返回结果
			OutputStream out = response.getOutputStream();
			out.write("success".getBytes());
			out.flush();
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			response.getOutputStream().write("fail".getBytes());
			response.getOutputStream().flush();

			throw e;
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}

	}

	@RequestMapping(value = "/getauthurl", method = RequestMethod.POST)
	public void getAuthUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数
			String shopappId = request.getParameter("shopapp_id");
			if (shopappId == null)
				throw new InteractRuntimeException("shopapp_id不能为空。");

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			String preAuthCodeKey = shopappId + loginStatus.getUserId();
			String preAuthCode = jedis.get(preAuthCodeKey);
			if (preAuthCode == null || preAuthCode.isEmpty()) {
				// 获取预授权码
				String url = new StringBuilder("https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?")
						.append("component_access_token=").append(componentAccessToken).toString();
				logger.debug("url " + url);

				JSONObject content = new JSONObject();
				content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);

				RequestBody body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());

				Request okHttpRequest = new Request.Builder().url(url).post(body).build();
				Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
				String responseBody = okHttpResponse.body().string();
				logger.debug("responseBody " + responseBody);
				JSONObject resultVo = JSON.parseObject(responseBody);
				int errcode = resultVo.getIntValue("errcode");
				if (errcode != 0)
					throw new InteractRuntimeException(resultVo.getString("errmsg"));

				preAuthCode = resultVo.getString("pre_auth_code");
				int expiresIn = resultVo.getIntValue("expires_in");
				jedis.set(preAuthCodeKey, preAuthCode);
				jedis.expire(preAuthCodeKey, expiresIn / 10);
			}

			String url = new StringBuilder("https://mp.weixin.qq.com/cgi-bin/componentloginpage?")
					.append("component_appid=").append(SysConstant.wechat_open_thirdparty_AppId)
					.append("&pre_auth_code=").append(preAuthCode).append("&redirect_uri=")
					.append(SysConstant.project_rooturl + "/c/miniappauth/authcallback/")
					.append(loginStatus.getUserId()).append("/").append(shopappId).append("&auth_type=").append("3")
					.toString();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("url", url);
			HttpRespondWithData.todo(request, response, 0, null, data);
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

	@RequestMapping(value = "/authcallback/{userId}/{shopappId}", method = RequestMethod.GET)
	@Transactional(rollbackFor = Exception.class)
	public void userAuthOfMiniappTemplateCallback(@PathVariable("userId") String userId,
			@PathVariable("shopappId") String shopappId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数
			String authCode = request.getParameter("auth_code");
			int expiresIn = Integer.parseInt(request.getParameter("expires_in"));

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();

			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			if (StringUtils.isEmpty(componentAccessToken))
				throw new InteractException("componentAccessToken is empty.");

			StringBuilder url = new StringBuilder("https://api.weixin.qq.com/cgi-bin/component/api_query_auth?")
					.append("component_access_token=").append(componentAccessToken);
			logger.debug("url " + url);
			JSONObject content = new JSONObject();
			content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);
			content.put("authorization_code", authCode);

			RequestBody body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());

			Request okHttpRequest = new Request.Builder().url(url.toString()).post(body).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractException(resultVo.getString("errmsg"));

			JSONObject authorizationInfo = resultVo.getJSONObject("authorization_info");
			String authorizerAppId = authorizationInfo.getString("authorizer_appid");

			// 所属人判断
			WxMiniapp wxMiniapp = myMiniappMapper.selectByAppId(authorizerAppId);
			if (wxMiniapp != null && !wxMiniapp.getUserId().equals(userId))
				throw new InteractException("您的微信小程序公众号已经绑定了其他账号:" + userManageMapper.getUsername(userId));

			String authorizerAccessToken = authorizationInfo.getString("authorizer_access_token");
			short authorizerExpiresIn = authorizationInfo.getShortValue("expires_in");
			String authorizerRefreshToken = authorizationInfo.getString("authorizer_refresh_token");
			// "func_info": [{"funcscope_category": {"id":
			// 1}},{"funcscope_category": {"id": 2}},{"funcscope_category":
			// {"id": 3}}]
			JSONArray funcInfo = authorizationInfo.getJSONArray("func_info");

			url = new StringBuilder("https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info?")
					.append("component_access_token=").append(componentAccessToken);
			logger.debug("url " + url);
			content = new JSONObject();
			content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);
			content.put("authorizer_appid", authorizerAppId);

			body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());

			okHttpRequest = new Request.Builder().url(url.toString()).post(body).build();
			okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractException(resultVo.getString("errmsg"));

			JSONObject authorizerInfo = resultVo.getJSONObject("authorizer_info");
			if (wxMiniapp != null) {
				wxMiniapp.setAccessToken(authorizerAccessToken);
				wxMiniapp.setExpiresIn(authorizerExpiresIn);
				wxMiniapp.setRefreshToken(authorizerRefreshToken);
				wxMiniapp.setFuncInfo(JSON.toJSONString(funcInfo, SerializerFeature.WriteMapNullValue));
				wxMiniapp.setNickName(authorizerInfo.getString("nick_name"));
				wxMiniapp.setHeadImg(authorizerInfo.getString("head_img"));
				wxMiniapp.setUserName(authorizerInfo.getString("user_name"));
				wxMiniapp.setPrincipalName(authorizerInfo.getString("principal_name"));
				wxMiniapp.setQrcodeUrl(authorizerInfo.getString("qrcode_url"));
				wxMiniapp.setSignature(authorizerInfo.getString("signature"));
				wxMiniapp.setAuthorizationTime(new Date().getTime());
				wxMiniapp.setAuthorized((byte) 1);
				wxMiniappMapper.updateByPrimaryKeySelective(wxMiniapp);
			} else {
				// int wxTemplateId =
				// miniappTemplateManageMapper.getWxTemplateIdById(templateId);
				String wxMiniappId = RandomStringUtils.randomNumeric(12);
				wxMiniapp = new WxMiniapp();
				wxMiniapp.setId(wxMiniappId);
				wxMiniapp.setUserId(userId);
				wxMiniapp.setShopappId(shopappId);
				wxMiniapp.setAppId(authorizerAppId);
				wxMiniapp.setAccessToken(authorizerAccessToken);
				wxMiniapp.setExpiresIn(authorizerExpiresIn);
				wxMiniapp.setRefreshToken(authorizerRefreshToken);
				wxMiniapp.setFuncInfo(JSON.toJSONString(funcInfo, SerializerFeature.WriteMapNullValue));
				wxMiniapp.setNickName(authorizerInfo.getString("nick_name"));
				wxMiniapp.setHeadImg(authorizerInfo.getString("head_img"));
				wxMiniapp.setUserName(authorizerInfo.getString("user_name"));
				wxMiniapp.setPrincipalName(authorizerInfo.getString("principal_name"));
				wxMiniapp.setQrcodeUrl(authorizerInfo.getString("qrcode_url"));
				wxMiniapp.setSignature(authorizerInfo.getString("signature"));
				wxMiniapp.setAuthorizationTime(new Date().getTime());
				// wxMiniapp.setWxTemplateId(wxTemplateId);
				wxMiniapp.setCodeCommit((byte) 0);
				wxMiniapp.setSubmitAudit((byte) 0);
				wxMiniapp.setBindTime(new Date().getTime());
				wxMiniapp.setAuthorized((byte) 1);
				wxMiniapp.setReleased((byte) 0);
				wxMiniappMapper.insertSelective(wxMiniapp);

				if ("1".equals(shopappId)) {
					HpHospital hospital = new HpHospital();
					String hospitalId = RandomStringUtils.randomNumeric(12);
					hospital.setId(hospitalId);
					hospital.setEnteringTime(new Date().getTime());
					hospital.setWxMiniappId(wxMiniappId);
					hpHospitalMapper.insertSelective(hospital);

					HpHospitalUser user = new HpHospitalUser();
					user.setId(userId);
					user.setHospitalId(hospitalId);
					user.setRegisterTime(new Date().getTime());
					user.setIfLoginable((byte) 1);
					user.setIfSuperadmin((byte) 1);
					user.setIfAdmin((byte) 1);
					hpHospitalUserMapper.insertSelective(user);
				}
			}

			// 设置小程序服务器域名
			url = new StringBuilder("https://api.weixin.qq.com/wxa/modify_domain?").append("access_token=")
					.append(authorizerAccessToken);
			logger.debug("url " + url);
			content = new JSONObject();
			content.put("action", "set");
			JSONArray requestdomain = new JSONArray();
			requestdomain.add("https://passion.njshangka.com");
			JSONArray wsrequestdomain = new JSONArray();
			wsrequestdomain.add("wss://passion.njshangka.com");
			JSONArray uploaddomain = new JSONArray();
			uploaddomain.add("https://passion.njshangka.com");
			JSONArray downloaddomain = new JSONArray();
			downloaddomain.add("https://passion.njshangka.com");

			content.put("requestdomain", requestdomain);
			content.put("wsrequestdomain", wsrequestdomain);
			content.put("uploaddomain", uploaddomain);
			content.put("downloaddomain", downloaddomain);

			String contentStr = content.toJSONString();
			logger.debug(contentStr);

			body = RequestBody.create(MediaType.parse("application/json"), contentStr);

			okHttpRequest = new Request.Builder().url(url.toString()).post(body).build();
			okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			responseBody = okHttpResponse.body().string();
			logger.debug("C " + responseBody);
			resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractException(resultVo.getString("errmsg"));

			// 设置小程序业务域名
			// url = new
			// StringBuilder("https://api.weixin.qq.com/wxa/setwebviewdomain?").append("access_token=")
			// .append(authorizerAccessToken);
			// logger.debug("url " + url);
			//
			// content = new JSONObject();
			// content.put("action", "set");
			// JSONArray webviewdomain = new JSONArray();
			// webviewdomain.add("https://passion.njshangka.com");
			// webviewdomain.add("https://hospital-publicity.njshangka.com");
			// content.put("webviewdomain", webviewdomain);
			//
			// contentStr = content.toJSONString();
			// logger.debug(contentStr);
			//
			// body = RequestBody.create(MediaType.parse("application/json"),
			// contentStr);
			//
			// okHttpRequest = new
			// Request.Builder().url(url.toString()).post(body).build();
			// okHttpResponse =
			// SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			// responseBody = okHttpResponse.body().string();
			// logger.debug("responseBody " + responseBody);
			// resultVo = JSON.parseObject(responseBody);
			// if (resultVo.getIntValue("errcode") != 0)
			// throw new InteractException(resultVo.getString("errmsg"));

			// 返回结果
			JSONObject data = new JSONObject();
			HttpRespondWithData.todo(request, response, 0, null, data);
			response.sendRedirect(SysConstant.project_rooturl + "/client/myapp.html");
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);

			throw e;
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}

	}

	@RequestMapping(value = "/wx14ce9af359895e9f/officialandminiappeventpush", method = RequestMethod.POST)
	public void officialAndMiniAppEventPush(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String charset = StringUtils.isEmpty(request.getCharacterEncoding()) ? SysConstant.SYS_CHARSET
					: request.getCharacterEncoding();
			String reqData = IOUtils.toString(request.getInputStream(), charset);
			logger.debug(reqData);

			// 业务处理
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new ByteArrayInputStream(reqData.getBytes(charset)));
			Element root = doc.getRootElement();
			String msgType = root.elementTextTrim("MsgType");
			String event = root.elementTextTrim("Event");

			if ("event".equals(msgType)) {
				if ("weapp_audit_success".equals(event)) {
					String toUserName = root.elementTextTrim("ToUserName");
					String createTime = root.elementTextTrim("CreateTime");
					String succTime = root.elementTextTrim("SuccTime");
					WxMiniapp miniapp = myMiniappMapper.selectByUserName(toUserName);
					miniapp.setAuditStatus((byte) 0);
					miniapp.setCodeCommit((byte) 0);
					wxMiniappMapper.updateByPrimaryKeySelective(miniapp);
				} else if ("weapp_audit_fail".equals(event)) {
					String toUserName = root.elementTextTrim("ToUserName");
					String createTime = root.elementTextTrim("CreateTime");
					String reason = root.elementTextTrim("Reason");
					String failTime = root.elementTextTrim("FailTime");
					WxMiniapp miniapp = myMiniappMapper.selectByUserName(toUserName);
					miniapp.setAuditStatus((byte) 1);
					miniapp.setAuditFailReason(reason);
					miniapp.setCodeCommit((byte) 0);
					miniapp.setCommittedTemplateId("");
					wxMiniappMapper.updateByPrimaryKeySelective(miniapp);
				}
			}
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

	public static void main(String[] args) throws DocumentException, UnsupportedEncodingException {
		JSONObject content = new JSONObject();
		content.put("action", "add");
		content.put("requestdomain", "[\"https://passion.njshangka.com\"]");
		content.put("wsrequestdomain", "[\"wss://passion.njshangka.com\"]");
		content.put("uploaddomain", "[\"https://passion.njshangka.com\"]");
		content.put("downloaddomain", "[\"https://passion.njshangka.com\"]");

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());
	}
}
