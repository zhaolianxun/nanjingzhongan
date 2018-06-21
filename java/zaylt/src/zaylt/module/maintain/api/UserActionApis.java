package zaylt.module.maintain.api;

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
import zaylt.module.maintain.business.LoginStatus;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("maintain.api.UserAction")
@RequestMapping(value = "/mm/useraction")
public class UserActionApis {

	public static Logger logger = Logger.getLogger(UserActionApis.class);

	@RequestMapping(value = "/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("username 不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");
			connection = ZayltDataSource.dataSource.getConnection();

			pst = connection.prepareStatement("select password_md5,role,id from t_maintainer where username=?");
			pst.setObject(1, username);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("账号不存在");
			}
			String passwordMd5 = rs.getString("password_md5");
			int role = rs.getInt("role");
			String userId = rs.getString("id");
			pst.close();
			if (!passwordMd5.equals(DigestUtils.md5Hex(pwd)))
				throw new InteractRuntimeException("密码错误");
			jedis = SysConstant.jedisPool.getResource();

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			LoginStatus loginStatus = new LoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setRole(role);
			loginStatus.setUsername(username);
			loginStatus.setLoginTime(new Date().getTime());
			String loginRedisKey = new StringBuilder("zaylt.maintain.login-").append(token).toString();
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("username", username);
			data.put("role", role);
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

	@RequestMapping(value = "/loginrefresh")
	public void loginRefresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			LoginStatus loginStatus = LoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String loginRedisKey = new StringBuilder("zaylt.maintain.login-").append(loginStatus.getToken()).toString();
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(loginStatus.getUserId(), loginStatus.getToken());
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(loginStatus.getUserId(), 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("role", loginStatus.getRole());
			data.put("username", loginStatus.getUsername());
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
				String loginRedisKey = new StringBuilder("zaylt.maintain.login-").append(loginStatus.getToken())
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
}