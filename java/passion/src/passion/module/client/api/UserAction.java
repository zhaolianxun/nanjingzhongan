package passion.module.client.api;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import passion.constant.OutApis;
import passion.constant.SysConstant;
import passion.dao.entity.ClientUser;
import passion.entity.InteractRuntimeException;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("controller.client.UserAction")
@RequestMapping(value = "/c/u")
public class UserAction extends BaseController {

	public static Logger logger = Logger.getLogger(UserAction.class);
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManagementMapper;
	@Autowired
	protected AppShopEntranceMapper appShopModuleMapper;
	@Autowired
	protected MyMiniappEntranceMapper myMiniappMapper;
	@Autowired
	protected UserManageMapper userManageMapper;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("用户名不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("密码不可空");

			// TODO 业务处理
			String passwordMd5 = DigestUtils.md5Hex(password);
			ClientUser user = userManageMapper.loginByUsernameAndPwdCheck(username, passwordMd5);
			if (user == null)
				throw new InteractRuntimeException("用户名或密码错误");
			if (user.getIfLoginable() != 1)
				throw new InteractRuntimeException("禁止登陆:" + user.getLoginunableReason());

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(user.getId());
			status.setUsername(user.getUsername());
			status.setPhone(user.getPhone());
			status.setNickname(user.getNickname());
			status.setIfSuperadmin(user.getIfSuperadmin());
			status.setIfAdmin(user.getIfAdmin());
			status.setLoginTime(new Date().getTime());

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set(token, JSON.toJSONString(status));

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

	@RequestMapping(value = "/selfinfo", method = RequestMethod.POST)
	public void selfInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.putAll((JSONObject) JSON.toJSON(loginStatus));
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/registerbysmsverifyandlogin", method = RequestMethod.POST)
	public void registerBySmsVerifyAndLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");

			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");

			String password = StringUtils.trimToNull(request.getParameter("password"));
			// TODO 业务处理
			if (userManageMapper.phoneCheck(phone) > 0)
				throw new InteractRuntimeException("手机号已存在");

			// 校验短信验证码
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(verificationCode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getInteger("code") != 0 || resultVo.getJSONObject("data").getInteger("ifSuccess") != 1) {
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));
			}

			ClientUser user = new ClientUser();
			String userId = RandomStringUtils.randomNumeric(12);
			user.setId(userId);
			user.setRegisterTime(new Date().getTime());
			user.setPhone(phone);
			user.setUsername(phone);
			user.setIfLoginable((byte) 1);
			user.setIfSuperadmin((byte) 0);
			user.setIfAdmin((byte) 0);
			user.setPassword(password);
			if (password != null)
				user.setPasswordMd5(DigestUtils.md5Hex(password));
			clientUserMapper.insertSelective(user);

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(user.getId());
			status.setUsername(user.getUsername());
			status.setPhone(user.getPhone());
			status.setNickname(user.getNickname());
			status.setIfSuperadmin(user.getIfSuperadmin());
			status.setIfAdmin(user.getIfAdmin());
			status.setLoginTime(new Date().getTime());

			String token = RandomStringUtils.randomNumeric(32);
			jedis = SysConstant.jedisPool.getResource();
			String oldToken = jedis.get(userId);
			if (oldToken != null)
				jedis.del(oldToken);
			jedis.del(userId);
			jedis.set(token, JSON.toJSONString(status));
			jedis.set(userId, token);
			jedis.expire(token, 15 * 24 * 60 * 60);
			jedis.expire(userId, 15 * 24 * 60 * 60);
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
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

	@RequestMapping(value = "/loginbysmsverify", method = RequestMethod.POST)
	public void loginBySmsVerify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");

			// TODO 业务处理
			if (userManageMapper.phoneCheck(phone) == 0)
				throw new InteractRuntimeException("手机号不存在");

			// 校验短信验证码
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(verificationCode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getInteger("code") != 0 || resultVo.getJSONObject("data").getInteger("ifSuccess") != 1) {
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));
			}

			ClientUser user = userManageMapper.getByPhone(phone);
			if (user.getIfLoginable() != 1)
				throw new InteractRuntimeException("禁止登陆:" + user.getLoginunableReason());

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(user.getId());
			status.setUsername(user.getUsername());
			status.setPhone(user.getPhone());
			status.setNickname(user.getNickname());
			status.setIfSuperadmin(user.getIfSuperadmin());
			status.setIfAdmin(user.getIfAdmin());
			status.setLoginTime(new Date().getTime());

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set(token, JSON.toJSONString(status));

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

	@RequestMapping(value = "/logincheck", method = RequestMethod.POST)
	public void loginCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			byte ifLogin = 0;
			if (GetLoginStatus.todo(request) != null)
				ifLogin = 1;

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("ifLogin", ifLogin);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus status = GetLoginStatus.todo(request, jedis);
			if (status == null)
				throw new InteractRuntimeException(20);

			jedis.del(status.getToken());

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
		UserLoginStatus loginStatus = new UserLoginStatus();
		loginStatus.setUserId("12323");
		// TODO 返回结果
		JSONObject data = new JSONObject();
		System.out.println(JSON.toJSON(loginStatus).getClass());
		data.putAll((JSONObject) JSON.toJSON(loginStatus));
		System.out.println(data);
	}
}
