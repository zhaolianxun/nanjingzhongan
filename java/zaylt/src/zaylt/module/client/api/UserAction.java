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
import zaylt.business.LoginStatus;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
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
			pst = connection.prepareStatement(
					"select power_addhospital,power_lookallhospitals,id,phone,type,hospital_id,clinic_id from t_user where wx_openid=?");
			pst.setObject(1, openid);
			ResultSet rs = pst.executeQuery();
			String userId = null;
			String phone = null;
			String type = null;
			Integer hospitalId = null;
			Integer clinicId = null;
			int powerAddhospital = 0;
			int powerLookallhospitals = 0;
			if (rs.next()) {
				userId = rs.getString("id");
				phone = rs.getString("phone");
				type = rs.getString("type");
				hospitalId = (Integer) rs.getObject("hospital_id");
				clinicId = (Integer) rs.getObject("clinic_id");
				powerAddhospital = (Integer) rs.getObject("power_addhospital");
				powerLookallhospitals = (Integer) rs.getObject("power_lookallhospitals");
			} else
				throw new InteractRuntimeException("未绑定手机");
			pst.close();

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			LoginStatus loginStatus = new LoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setType(type);
			loginStatus.setHospitalId(hospitalId);
			loginStatus.setClinicId(clinicId);
			loginStatus.setPowerAddHospital(powerAddhospital);
			loginStatus.setPowerLookAllHospitals(powerLookallhospitals);
			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
			//// 清除历史登录状态
			jedis = SysConstant.jedisPool.getResource();
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
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不可空");

			// 更新数据库用户信息
			connection = ZayltDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select id,password_md5 from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			String userId = null;
			if (rs.next()) {
				userId = rs.getString(1);
				String srcPwdMd5 = rs.getString(2);
				pst.close();

				if (!srcPwdMd5.equals(DigestUtils.md5Hex(pwd)))
					throw new InteractRuntimeException("密码错误");
				pst = connection
						.prepareStatement("update t_user set wx_openid=null,wx_session_key=null where wx_openid=?");
				pst.setObject(1, openid);
				pst.executeUpdate();
				pst.close();

				pst = connection.prepareStatement("update t_user set wx_openid=? where id=?");
				pst.setObject(1, openid);
				pst.setObject(2, userId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			} else
				throw new InteractRuntimeException("手机号不存在");

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
			data.put("openid", openid);
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
			pst = connection.prepareStatement(
					"select power_addhospital,power_lookallhospitals,password_md5,type,id,hospital_id,clinic_id from t_user where phone=?");
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
			int powerAddhospital = (Integer) rs.getObject("power_addhospital");
			int powerLookallhospitals = (Integer) rs.getObject("power_lookallhospitals");
			pst.close();
			if (!passwordMd5.equals(DigestUtils.md5Hex(pwd)))
				throw new InteractRuntimeException("密码错误");

			jedis = SysConstant.jedisPool.getResource();

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			LoginStatus loginStatus = new LoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setType(type);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setHospitalId(hospitalId);
			loginStatus.setClinicId(clinicId);
			loginStatus.setPowerAddHospital(powerAddhospital);
			loginStatus.setPowerLookAllHospitals(powerLookallhospitals);
			loginStatus.setPhone(phone);

			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

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
	public void loginRefresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			LoginStatus loginStatus = LoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = ZayltDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select power_addhospital,power_lookallhospitals,type,id,hospital_id,clinic_id,phone from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int powerAddhospital = 0;
			int powerLookallhospitals = 0;
			String type = null;
			String userId = null;
			String phone = null;
			Integer hospitalId = null;
			Integer clinicId = null;
			if (rs.next()) {
				powerAddhospital = (Integer) rs.getObject("power_addhospital");
				powerLookallhospitals = (Integer) rs.getObject("power_lookallhospitals");
				type = rs.getString("type");
				phone = rs.getString("phone");
				userId = rs.getString("id");
				hospitalId = (Integer) rs.getObject("hospital_id");
				clinicId = (Integer) rs.getObject("clinic_id");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			loginStatus.setClinicId(clinicId);
			loginStatus.setHospitalId(hospitalId);
			loginStatus.setPhone(phone);
			loginStatus.setType(type);
			loginStatus.setPowerAddHospital(powerAddhospital);
			loginStatus.setPowerLookAllHospitals(powerLookallhospitals);

			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(loginStatus.getToken()).toString();
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, loginStatus.getToken());
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

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
			if (jedis != null)
				jedis.close();
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/logincheck")
	public void loginCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
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

	@RequestMapping(value = "/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			LoginStatus loginStatus = LoginStatus.todo(request, jedis);
			if (loginStatus != null) {
				String loginRedisKey = new StringBuilder("zaylt.client.login-").append(loginStatus.getToken())
						.toString();
				//// 设置新登录状态
				jedis.del(loginRedisKey);
				jedis.del(loginStatus.getUserId());
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
}