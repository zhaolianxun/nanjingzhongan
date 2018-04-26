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

import redis.clients.jedis.Jedis;
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