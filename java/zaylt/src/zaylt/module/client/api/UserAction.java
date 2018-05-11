package zaylt.module.client.api;

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
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import zaylt.constant.OutApis;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.module.client.business.GetLoginStatus;
import zaylt.module.client.entity.UserLoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("client.api.UserAction")
@RequestMapping(value = "/c/useraction")
public class UserAction {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@RequestMapping(value = "/wxapplogin")
	@Transactional(rollbackFor = Exception.class)
	public void wxapplogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String openid = StringUtils.trimToNull(request.getParameter("openid"));
			if (openid == null)
				throw new InteractRuntimeException("openid 不可空");

			// 更新数据库用户信息
			connection = ZayltDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id,phone,type from t_user where wx_openid=?");
			pst.setObject(1, openid);
			ResultSet rs = pst.executeQuery();
			String userId = null;
			String phone = null;
			String type = null;
			if (rs.next()) {
				userId = rs.getString(1);
				phone = rs.getString(2);
				type = rs.getString(3);
			} else
				throw new InteractRuntimeException("未绑定手机");
			pst.close();

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			UserLoginStatus loginStatus = new UserLoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setType(type);
			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
			//// 清除历史登录状态
			String oldToken = jedis.get(userId);
			if (oldToken != null && !oldToken.isEmpty())
				jedis.del(new StringBuilder("zaylt.client.login-").append(oldToken).toString());
			jedis.del(userId);
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", userId);
			data.put("phone", phone);
			data.put("type", type);
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

	@RequestMapping(value = "/wxopenidbindphone")
	@Transactional(rollbackFor = Exception.class)
	public void wxopenidbindphone(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String openid = StringUtils.trimToNull(request.getParameter("openid"));
			if (openid == null)
				throw new InteractRuntimeException("openid 不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");
			String smsvcode = StringUtils.trimToNull(request.getParameter("smsvcode"));
			if (smsvcode == null)
				throw new InteractRuntimeException("smsvcode 不可空");
			// 短信校验
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(smsvcode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getInteger("code") != 0 || resultVo.getJSONObject("data").getInteger("ifSuccess") != 1) {
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));
			}

			// 更新数据库用户信息
			connection = ZayltDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			String userId = null;
			if (rs.next()) {
				userId = rs.getString(1);
				pst.close();

				pst = connection.prepareStatement("update t_user set wx_openid=? where id=?");
				pst.setObject(1, openid);
				pst.setObject(2, userId);
				pst.executeUpdate();
				pst.close();
			} else
				throw new InteractRuntimeException("手机号不存在");

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

	@RequestMapping(value = "/getandchkwxopenid")
	@Transactional(rollbackFor = Exception.class)
	public void getandchkwxopenid(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String jscode = StringUtils.trimToNull(request.getParameter("jscode"));
			if (jscode == null)
				throw new InteractRuntimeException("jscode不可空");

			// 微信登录
			String url = new StringBuilder("https://api.weixin.qq.com/sns/jscode2session?").append("appid=")
					.append(SysConstant.wx_smallapp_appid).append("&secret=").append(SysConstant.wx_smallapp_appsecret)
					.append("&js_code=").append(jscode).append("&grant_type=authorization_code").toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			Integer errcode = resultVo.getInteger("errcode");
			if (errcode != null && errcode != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			String openid = resultVo.getString("openid");
			String sessionKey = resultVo.getString("session_key");

			// 更新数据库用户信息
			connection = ZayltDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where wx_openid=?");
			pst.setObject(1, openid);
			ResultSet rs = pst.executeQuery();
			int boundPhoneIf = 0;
			if (rs.next()) {
				String userId = rs.getString(1);
				boundPhoneIf = 1;
				pst.close();
				pst = connection.prepareStatement("update t_user set wx_session_key=? where id=?");
				pst.setObject(1, sessionKey);
				pst.setObject(2, userId);
				pst.executeUpdate();
				pst.close();
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("boundPhoneIf", boundPhoneIf);
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

	@RequestMapping(value = "/login")
	@Transactional(rollbackFor = Exception.class)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");
			connection = ZayltDataSource.dataSource.getConnection();

			// 查询登录的商城信息
			pst = connection
					.prepareStatement("select password_md5,type,id,hospital_id,clinic_id from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("手机号不存在");
			}
			String passwordMd5 = rs.getString("password_md5");
			String type = rs.getString("type");
			String userId = rs.getString("id");
			Integer hospitalId = (Integer) rs.getObject("hospital_id");
			Integer clinicId = (Integer) rs.getObject("clinic_id");
			pst.close();
			if (!passwordMd5.equals(DigestUtils.md5Hex(pwd)))
				throw new InteractRuntimeException("密码错误");

			jedis = SysConstant.jedisPool.getResource();

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			UserLoginStatus loginStatus = new UserLoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setType(type);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setHospitalId(hospitalId);
			loginStatus.setClinicId(clinicId);
			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
			//// 清除历史登录状态
			String oldToken = jedis.get(userId);
			if (oldToken != null)
				jedis.del(new StringBuilder("zaylt.client.login-").append(oldToken).toString());
			jedis.del(userId);
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("phone", phone);
			data.put("type", type);
			data.put("userId", userId);
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

	@RequestMapping(value = "/selfinfo")
	public void selfInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("phone", loginStatus.getPhone());
			data.put("type", loginStatus.getType());
			data.put("userId", loginStatus.getUserId());
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/logincheck")
	public void loginCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			int loginIs = 0;
			if (loginStatus != null)
				loginIs = 1;

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("loginIs", loginIs);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}
}