package easywin.module.plat.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import easywin.constant.OutApis;
import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.module.plat.task.PushMessageQueue;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

@Controller("plat.api.WxOpenP3Authorization")
@RequestMapping(value = "/p/wop3a")
public class WxOpenP3Authorization {

	public static Logger logger = Logger.getLogger(WxOpenP3Authorization.class);

	@RequestMapping(value = "/authorizeCall")
	public void authorizeCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
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
				connection = EasywinDataSource.dataSource.getConnection();
				connection.setAutoCommit(false);
				pst = connection.prepareStatement(
						"update t_app set authorized=0,access_token=null,expires_in=null,refresh_token=null,func_info=null where wx_appid=?");
				pst.setObject(1, authorizerAppid);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
				connection.commit();

				pst = connection.prepareStatement(
						"select u.phone,a.nick_name,ag.agent_domain from  t_app a left join  t_user u  on u.id=a.user_id left join t_user ag on a.from_agent_id=ag.id where a.wx_appid=?");
				pst.setObject(1, authorizerAppid);
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					String phone = rs.getString("phone");
					String nickName = rs.getString("nick_name");
					String agentDomain = rs.getString("agent_domain");
					PushMessageQueue.systemMsg(null, phone, "", new StringBuilder("您已取消小程序'").append(nickName)
							.append("'的授权，这将影响您小程序的运营，您可以登录网站查看!").append("http://").append(agentDomain).toString());
				}
				pst.close();
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
			if (connection != null)
				connection.rollback();
			response.getOutputStream().write("fail".getBytes());
			response.getOutputStream().flush();
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}

	}

	@RequestMapping(value = "/getauthurl", method = RequestMethod.POST)
	public void getAuthUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String seedId = request.getParameter("seed_id");
			if (seedId == null)
				throw new InteractRuntimeException("seed_id不能为空。");
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			appId = appId == null ? "0" : appId;
			String agentDomain = request.getHeader("Host");
			agentDomain = agentDomain == null || agentDomain.isEmpty() ? request.getRemoteHost() : agentDomain;

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where agent_domain=?");
			pst.setObject(1, agentDomain);
			ResultSet rs = pst.executeQuery();
			String agentId = null;
			if (rs.next())
				agentId = rs.getString(1);
			else
				throw new InteractRuntimeException("代理不存在");

			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			String preAuthCodeKey = seedId + loginStatus.getUserId();
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
					.append(SysConstant.project_rooturl + "/p/wop3a/authcallback/").append(loginStatus.getUserId())
					.append("/").append(seedId).append("/").append(agentId).append("/").append(appId)
					.append("&auth_type=").append("3").toString();

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
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/authcallback/{userId}/{seedId}/{agentId}/{appId}", method = RequestMethod.GET)
	public void userAuthOfMiniappTemplateCallback(@PathVariable("userId") String fromUserId,
			@PathVariable("seedId") String seedId, @PathVariable("agentId") String fromAgentId,
			@PathVariable("appId") String fromAppId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String authCode = request.getParameter("auth_code");
			int authCodeExpiresIn = Integer.parseInt(request.getParameter("expires_in"));
			fromAgentId = "0".equals(fromAgentId) ? null : fromAgentId;
			fromAppId = "0".equals(fromAppId) ? null : fromAppId;
			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			jedis.del(seedId + fromUserId);
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");
			jedis.close();
			jedis = null;

			if (StringUtils.isEmpty(componentAccessToken))
				throw new InteractRuntimeException("componentAccessToken is empty.");

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
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			JSONObject authorizationInfo = resultVo.getJSONObject("authorization_info");
			String authorizerAppId = authorizationInfo.getString("authorizer_appid");

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement("select count(id) from t_user where id=?");
			pst.setObject(1, fromAgentId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int agentExist = rs.getInt(1);
				if (agentExist == 0)
					throw new InteractRuntimeException("代理不存在");
				else if (agentExist > 1)
					throw new InteractRuntimeException("代理不明");
			} else
				throw new InteractRuntimeException("代理不存在");

			String fromAgentDomain = null;
			if (fromAgentId != null) {
				pst = connection.prepareStatement("select agent_domain from t_user where id=?");
				pst.setObject(1, fromAgentId);
				rs = pst.executeQuery();
				if (rs.next()) {
					fromAgentDomain = rs.getString("agent_domain");
				}
				pst.close();
			}

			String fromAppNickName = null;
			if (fromAppId != null) {
				pst = connection.prepareStatement("select nick_name from t_app where id=?");
				pst.setObject(1, fromAppId);
				rs = pst.executeQuery();
				if (rs.next()) {
					fromAppNickName = rs.getString("nick_name");
				}
				pst.close();
			}

			pst = connection.prepareStatement(
					"select agent.agent_domain from_agent_domain,t.seed_id,agent.phone from_agent_phone,t.from_agent_id,t.id,t.user_id,u.phone,t.nick_name from t_app t inner join t_user u on t.user_id=u.id left join t_user agent on t.from_agent_id=agent.id where t.wx_appid=?");
			pst.setObject(1, authorizerAppId);
			rs = pst.executeQuery();
			String existAppId = null;
			String existUserId = null;
			String existPhone = null;
			String existNickName = null;
			String existFromAgentId = null;
			String existFromAgentPhone = null;
			String existFromAgentDomain = null;
			String existSeedId = null;

			if (rs.next()) {
				existAppId = rs.getString("id");
				existUserId = rs.getString("user_id");
				existPhone = rs.getString("phone");
				existNickName = rs.getString("nick_name");
				existFromAgentId = rs.getString("from_agent_id");
				existFromAgentPhone = rs.getString("from_agent_phone");
				existFromAgentDomain = rs.getString("from_agent_domain");
				seedId = rs.getString("seed_id");
			}
			pst.close();
			if (seedId != null && existSeedId != null && !seedId.equals(existSeedId))
				throw new InteractRuntimeException("目前不可以更换小程序所绑定的应用，您可以用新的小程序来绑定。");
			if (existAppId != null && fromAppId != null && !existAppId.equals(fromAppId))
				throw new InteractRuntimeException("您选择的小程序没有绑定该应用，绑定的小程序为：" + fromAppNickName);
			if (existUserId != null && !existUserId.equals(fromUserId))
				throw new InteractRuntimeException("您的微信小程序公众号已经绑定了其他账号，账号:" + existPhone);
			if (existFromAgentId != null && !existFromAgentId.equals(fromAgentId))
				throw new InteractRuntimeException(
						"您的微信小程序(" + existNickName + ")已授权给了其他代理 请通过该代理域名进行登录:" + existFromAgentDomain);

			String authorizerAccessToken = authorizationInfo.getString("authorizer_access_token");
			long authorizerExpiresIn = authorizationInfo.getIntValue("expires_in");
			long expireIn = new Date().getTime() + authorizerExpiresIn * 1000l;
			String authorizerRefreshToken = authorizationInfo.getString("authorizer_refresh_token");
			// "func_info": [{"funcscope_category": {"id":
			// 1}},{"funcscope_category": {"id": 2}},{"funcscope_category":
			// {"id": 3}}]
			JSONArray funcInfo = authorizationInfo.getJSONArray("func_info");
			List<Integer> funcs = new ArrayList<Integer>();
			for (int i = 0; i < funcInfo.size(); i++) {
				int nn = funcInfo.getJSONObject(i).getJSONObject("funcscope_category").getIntValue("id");
				funcs.add(nn);
			}
			if (!funcs.contains(17))
				throw new InteractRuntimeException("需要您授予帐号管理权限");
			if (!funcs.contains(18))
				throw new InteractRuntimeException("需要您授予开发管理与数据分析权限");
			if (!funcs.contains(19))
				throw new InteractRuntimeException("需要您授予客服消息管理权限");
			if (!funcs.contains(25))
				throw new InteractRuntimeException("需要您授予开放平台帐号管理权限");
			if (!funcs.contains(30))
				throw new InteractRuntimeException("需要您授予小程序基本信息设置权限");
			if (!funcs.contains(31))
				throw new InteractRuntimeException("需要您授予小程序认证权限");

			// 从微信拉取授权者信息
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
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			JSONObject authorizerInfo = resultVo.getJSONObject("authorizer_info");
			if (existAppId != null) {
				pst = connection.prepareStatement(
						"update t_app set authorized=1,access_token=?,expires_in=?,refresh_token=?,func_info=?,nick_name=?,head_img=?,user_name=?,principal_name=?,qrcode_url=?,signature=?,authorization_time=? where id=?");
				pst.setObject(1, authorizerAccessToken);
				pst.setObject(2, expireIn);
				pst.setObject(3, authorizerRefreshToken);
				pst.setObject(4, JSON.toJSONString(funcInfo, SerializerFeature.WriteMapNullValue));
				pst.setObject(5, authorizerInfo.getString("nick_name"));
				pst.setObject(6, authorizerInfo.getString("head_img"));
				pst.setObject(7, authorizerInfo.getString("user_name"));
				pst.setObject(8, authorizerInfo.getString("principal_name"));
				pst.setObject(9, authorizerInfo.getString("qrcode_url"));
				pst.setObject(10, authorizerInfo.getString("signature"));
				pst.setObject(11, new Date().getTime());
				pst.setObject(12, existAppId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

				File appOssRoot = new File(SysConstant.project_ossroot, "mall" + existAppId);
				if (!appOssRoot.exists())
					appOssRoot.mkdirs();
			} else {
				pst = connection.prepareStatement(
						"insert into t_app (id,user_id,seed_id,authorized,wx_appid,access_token,expires_in,refresh_token,func_info,nick_name,head_img,user_name,principal_name,qrcode_url,signature,authorization_time,bind_time,use_endtime,from_agent_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				String newAppId = RandomStringUtils.randomNumeric(12);
				pst.setObject(1, newAppId);
				pst.setObject(2, fromUserId);
				pst.setObject(3, seedId);
				pst.setObject(4, 1);
				pst.setObject(5, authorizerAppId);
				pst.setObject(6, authorizerAccessToken);
				pst.setObject(7, expireIn);
				pst.setObject(8, authorizerRefreshToken);
				pst.setObject(9, JSON.toJSONString(funcInfo, SerializerFeature.WriteMapNullValue));
				pst.setObject(10, authorizerInfo.getString("nick_name"));
				pst.setObject(11, authorizerInfo.getString("head_img"));
				pst.setObject(12, authorizerInfo.getString("user_name"));
				pst.setObject(13, authorizerInfo.getString("principal_name"));
				pst.setObject(14, authorizerInfo.getString("qrcode_url"));
				pst.setObject(15, authorizerInfo.getString("signature"));
				pst.setObject(16, new Date().getTime());
				pst.setObject(17, new Date().getTime());
				pst.setObject(18, new Date().getTime() + 7 * 24 * 60 * 60 * 1000l);
				pst.setObject(19, fromAgentId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

				if ("1".equals(seedId)) {
					pst = connection.prepareStatement(
							"insert into t_mall (id,name,enter_time) values(?,?,rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0'))");
					pst.setObject(1, newAppId);
					pst.setObject(2, authorizerInfo.getString("nick_name") + "商城");
					n = pst.executeUpdate();
					if (n != 1)
						throw new InteractRuntimeException("操作失败");
				}

				File appOssRoot = new File(SysConstant.project_ossroot, newAppId);
				if (!appOssRoot.exists())
					appOssRoot.mkdirs();
			}
			// 设置小程序服务器域名
			url = new StringBuilder("https://api.weixin.qq.com/wxa/modify_domain?").append("access_token=")
					.append(authorizerAccessToken);
			logger.debug("url " + url);
			content = new JSONObject();
			content.put("action", "set");
			JSONArray requestdomain = new JSONArray();
			requestdomain.add("https://" + SysConstant.project_domain);
			JSONArray wsrequestdomain = new JSONArray();
			wsrequestdomain.add("wss://" + SysConstant.project_domain);
			JSONArray uploaddomain = new JSONArray();
			uploaddomain.add("https://" + SysConstant.project_domain);
			JSONArray downloaddomain = new JSONArray();
			downloaddomain.add("https://" + SysConstant.project_domain);

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
			logger.debug("responseBody " + responseBody);
			resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			// 设置小程序业务域名
			// url = new
			// StringBuilder("https://api.weixin.qq.com/wxa/setwebviewdomain?").append("access_token=")
			// .append(authorizerAccessToken);
			// logger.debug("url " + url);
			//
			// content = new JSONObject();
			// content.put("action", "set");
			// JSONArray webviewdomain = new JSONArray();
			// webviewdomain.add("https://" + SysConstant.project_domain);
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
			// throw new InteractRuntimeException(resultVo.getString("errmsg"));

			connection.commit();
			// 返回结果
			response.sendRedirect(fromAgentDomain == null ? "/easywin/plat/index.html?tab=myapp"
					: request.getScheme() + "://" + fromAgentDomain + "/easywin/plat/index.html?tab=myapp");
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}

	}

	@RequestMapping(value = "/eventMessage/{appId}/callback")
	public void eventMessageCallback(@PathVariable("appId") String appId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String timestamp = request.getParameter("timestamp");
			String nonce = request.getParameter("nonce");
			String msgSignature = request.getParameter("msg_signature");
			String charset = StringUtils.isEmpty(request.getCharacterEncoding()) ? SysConstant.SYS_CHARSET
					: request.getCharacterEncoding();
			String reqData = IOUtils.toString(request.getInputStream(), charset);
			logger.debug(reqData);

			// 业务处理
			WXBizMsgCrypt pc = new WXBizMsgCrypt(SysConstant.wechat_open_thirdparty_MsgVerificationToken,
					SysConstant.wechat_open_thirdparty_MsgEncryptAndDecryptKey,
					SysConstant.wechat_open_thirdparty_AppId);
			String result2 = pc.decryptMsg(msgSignature, timestamp, nonce, reqData);
			logger.debug("解密后: " + result2);

			SAXReader reader = new SAXReader();
			Document doc = reader.read(new ByteArrayInputStream(result2.getBytes(charset)));
			Element root = doc.getRootElement();
			String msgType = root.elementTextTrim("MsgType");
			String event = root.elementTextTrim("Event");

			if ("event".equals(msgType)) {
				connection = EasywinDataSource.dataSource.getConnection();
				connection.setAutoCommit(false);
				if ("weapp_audit_success".equals(event)) {
					String toUserName = root.elementTextTrim("ToUserName");
					String createTime = root.elementTextTrim("CreateTime");
					String succTime = root.elementTextTrim("SuccTime");
					pst = connection
							.prepareStatement("update t_app set audit_status=2,audit_fail_reason='' where wx_appid=?");
					pst.setObject(1, appId);
					int n = pst.executeUpdate();
					pst.close();
					if (n != 1)
						throw new InteractRuntimeException("操作失败");

					// 短信通知
					pst = connection.prepareStatement(
							"select  u.phone,a.nick_name,ag.agent_domain from t_app a left join t_user u  on u.id=a.user_id left join t_user ag on a.from_agent_id=ag.id where a.wx_appid=?");
					pst.setObject(1, appId);
					ResultSet rs = pst.executeQuery();
					if (rs.next()) {
						String phone = rs.getString("phone");
						String nickName = rs.getString("nick_name");
						String agentDomain = rs.getString("agent_domain");

						PushMessageQueue.systemMsg(null, phone, "", new StringBuilder("您的小程序'").append(nickName)
								.append("'审核通过，您可以登录网站发布上线了!").append("http://").append(agentDomain).toString());

					}
					pst.close();
				} else if ("weapp_audit_fail".equals(event)) {
					String toUserName = root.elementTextTrim("ToUserName");
					String createTime = root.elementTextTrim("CreateTime");
					String reason = root.elementTextTrim("Reason");
					String failTime = root.elementTextTrim("FailTime");
					pst = connection
							.prepareStatement("update t_app set audit_status=3,audit_fail_reason=? where wx_appid=?");
					pst.setObject(1, reason);
					pst.setObject(2, appId);
					int n = pst.executeUpdate();
					pst.close();
					if (n != 1)
						throw new InteractRuntimeException("操作失败");

					// 短信通知
					pst = connection.prepareStatement(
							"select u.phone,a.nick_name,ag.agent_domain from t_app a left join t_user u on u.id=a.user_id left join t_user ag on a.from_agent_id=ag.id where a.wx_appid=?");
					pst.setObject(1, appId);
					ResultSet rs = pst.executeQuery();
					if (rs.next()) {
						String phone = rs.getString("phone");
						String nickName = rs.getString("nick_name");
						String agentDomain = rs.getString("agent_domain");

						PushMessageQueue.systemMsg(null, phone, "", new StringBuilder("您的小程序'").append(nickName)
								.append("'审核失败，您可以登录网站查看!").append("http://").append(agentDomain).toString());
					}
					pst.close();
				}
			}

			if (connection != null)
				connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
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
