package zasellaid.module.client.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import zasellaid.constant.OutApis;
import zasellaid.constant.SysConstant;
import zasellaid.dao.entity.ClientUser;
import zasellaid.dao.mapper.ClientUserMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.module.client.business.GetLoginStatus;
import zasellaid.module.client.entity.UserLoginStatus;
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.UserAction")
@RequestMapping(value = "/c/u")
public class UserAction {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void loginByPhoneAndPwd(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("密码不可空");

			// TODO 业务处理
			String passwordMd5 = DigestUtils.md5Hex(password);

			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select (select max(date) from t_daily_report where user_id=t.id) last_daily_report_time,password_md5,if_loginable,id,loginunable_reason,realname,if_superadmin,maintain_is,if_admin from t_client_user t where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			String userId;
			String realname;
			String passwordMd5Src;
			Object lastDailyReportTime;
			int ifLoginable;
			int ifSuperadmin;
			int maintainIs;
			int ifAdmin;
			if (rs.next()) {
				ifLoginable = rs.getInt("if_loginable");
				if (ifLoginable != 1)
					throw new InteractRuntimeException("禁止登陆:" + rs.getString("loginunable_reason"));
				passwordMd5Src = rs.getString("password_md5");
				if (!passwordMd5Src.equals(passwordMd5))
					throw new InteractRuntimeException("密码错误");
				userId = rs.getString("id");
				realname = rs.getString("realname");
				ifSuperadmin = rs.getInt("if_superadmin");
				maintainIs = rs.getInt("maintain_is");
				ifAdmin = rs.getInt("if_admin");
				lastDailyReportTime = rs.getObject("last_daily_report_time");
			} else
				throw new InteractRuntimeException("手机号不存在");
			pst.close();

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(userId);
			status.setPhone(phone);
			status.setRealname(realname);
			status.setIfSuperadmin(ifSuperadmin);
			status.setIfAdmin(ifAdmin);
			status.setLoginTime(new Date().getTime());
			status.setMaintainIs(maintainIs);
			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set(token, JSON.toJSONString(status));
			jedis.expire(token, 7 * 24 * 60 * 60);

			RefreshUserAlive.bean.run(userId);
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", userId);
			data.put("phone", phone);
			data.put("realname", realname);
			data.put("ifAdmin", ifAdmin);
			data.put("ifSuperadmin", ifSuperadmin);
			data.put("maintainIs", maintainIs);
			data.put("lastDailyReportTime", lastDailyReportTime);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
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

	@RequestMapping(value = "/selfinfo", method = RequestMethod.POST)
	public void selfInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			String token = loginStatus.getToken();

			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select phone,(select max(date) from t_daily_report where user_id=t.id) last_daily_report_time,realname,if_superadmin,maintain_is,if_admin from t_client_user t where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			String realname;
			String phone;
			Object lastDailyReportTime;
			int ifSuperadmin;
			int maintainIs;
			int ifAdmin;
			if (rs.next()) {
				realname = rs.getString("realname");
				phone = rs.getString("phone");
				ifSuperadmin = rs.getInt("if_superadmin");
				maintainIs = rs.getInt("maintain_is");
				ifAdmin = rs.getInt("if_admin");
				lastDailyReportTime = rs.getObject("last_daily_report_time");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(loginStatus.getUserId());
			status.setPhone(phone);
			status.setRealname(realname);
			status.setIfSuperadmin(ifSuperadmin);
			status.setIfAdmin(ifAdmin);
			status.setLoginTime(new Date().getTime());
			status.setMaintainIs(maintainIs);

			jedis.set(token, JSON.toJSONString(status));
			jedis.expire(token, 7 * 24 * 60 * 60);

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("userId", loginStatus.getUserId());
			data.put("realname", realname);
			data.put("phone", phone);
			data.put("token", token);
			data.put("ifAdmin", ifAdmin);
			data.put("ifSuperadmin", ifSuperadmin);
			data.put("maintainIs", maintainIs);
			data.put("lastDailyReportTime", lastDailyReportTime);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
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

	@RequestMapping(value = "/loginbysmsverify", method = RequestMethod.POST)
	public void loginBySmsVerify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
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
			session = SysConstant.sqlSessionFactory.openSession(true);
			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);

			if (clientUserMapper.phoneCheck(phone) == 0)
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

			ClientUser user = clientUserMapper.getByPhone(phone);
			if (user.getIfLoginable() != 1)
				throw new InteractRuntimeException("禁止登陆:" + user.getLoginunableReason());

			UserLoginStatus status = new UserLoginStatus();
			status.setUserId(user.getId());
			status.setRealname(user.getRealname());
			status.setPhone(user.getPhone());
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
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/alterpwdbyold", method = RequestMethod.POST)
	public void alterPwdByOld(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String oldPassword = StringUtils.trimToNull(request.getParameter("old_pasword"));
			if (oldPassword == null)
				throw new InteractRuntimeException("old_pasword不可空");
			String newPassword = StringUtils.trimToNull(request.getParameter("new_password"));
			if (newPassword == null)
				throw new InteractRuntimeException("new_password不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String oldPasswordMd5 = DigestUtils.md5Hex(oldPassword);
			String newPasswordMd5 = DigestUtils.md5Hex(newPassword);

			session = SysConstant.sqlSessionFactory.openSession(true);
			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);

			String passwordMd5 = clientUserMapper.getPasswordMd5(loginStatus.getUserId());
			if (!oldPasswordMd5.equals(passwordMd5))
				throw new InteractRuntimeException("原始密码错误");

			ClientUser user = new ClientUser();
			user.setId(loginStatus.getUserId());
			user.setPassword(newPassword);
			user.setPasswordMd5(newPasswordMd5);
			clientUserMapper.updateByPrimaryKeySelective(user);

			RefreshUserAlive.bean.run(user.getId());
			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/alterpwdbyphone", method = RequestMethod.POST)
	public void alterPwdByPhone(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");
			String newPassword = StringUtils.trimToNull(request.getParameter("new_password"));
			if (newPassword == null)
				throw new InteractRuntimeException("new_password不可空");

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);

			ClientUser user = clientUserMapper.getByPhone(phone);
			if (user == null)
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

			String newPasswordMd5 = DigestUtils.md5Hex(newPassword);
			user.setPassword(newPassword);
			user.setPasswordMd5(newPasswordMd5);
			clientUserMapper.updateByPrimaryKeySelective(user);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
			if (session != null)
				session.close();
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

}
