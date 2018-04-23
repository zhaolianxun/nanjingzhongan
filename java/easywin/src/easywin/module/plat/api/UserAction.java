package easywin.module.plat.api;

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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.OutApis;
import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller("plat.api.UserAction")
@RequestMapping(value = "/p/useraction")
public class UserAction {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@RequestMapping(value = "/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("password不可空");

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select id,nickname,realname,loginable_is,admin_is,superadmin_is,agent_is from t_user where phone=? and password_md5=?");
			pst.setObject(1, phone);
			pst.setObject(2, DigestUtils.md5Hex(password));
			ResultSet rs = pst.executeQuery();
			UserLoginStatus loginStatus = new UserLoginStatus();
			if (rs.next()) {
				loginStatus.setLoginTime(new Date().getTime());
				loginStatus.setPhone(phone);
				loginStatus.setUserId(rs.getString("id"));
				loginStatus.setNickname(rs.getString("nickname"));
				loginStatus.setRealname(rs.getString("realname"));
				loginStatus.setAdminIs(rs.getInt("admin_is"));
				loginStatus.setSuperadminIs(rs.getInt("superadmin_is"));
				loginStatus.setAgentIs(rs.getInt("agent_is"));
			} else
				throw new InteractRuntimeException("用户名或密码错误");

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set(token, JSON.toJSONString(loginStatus));
			jedis.expire(token, 7 * 24 * 60 * 60);
			
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
			data.put("nickname", loginStatus.getNickname());
			data.put("superadminIs", loginStatus.getSuperadminIs());
			data.put("adminIs", loginStatus.getAdminIs());
			data.put("agentIs", loginStatus.getAgentIs());
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/alterpwdbysms")
	public void alterPwdBySms(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");
			String newPassword = StringUtils.trimToNull(request.getParameter("new_password"));
			if (newPassword == null)
				throw new InteractRuntimeException("new_password不可空");

			// 业务处理
			// 短信校验
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

			// 更新密码
			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set password=?,password_md5=? where phone=?");
			pst.setObject(1, newPassword);
			pst.setObject(2, DigestUtils.md5Hex(newPassword));
			pst.setObject(3, phone);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("手机号不存在");
			else if (n > 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/register")
	public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String agentId = StringUtils.trimToNull(request.getParameter("agent_id"));
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("password不可空");

			// 业务处理
			// 短信校验
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

			// 更新密码
			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new InteractRuntimeException("手机号已存在");
			pst.close();

			if (agentId != null) {
				pst = connection.prepareStatement("select id from t_user where id=?");
				pst.setObject(1, agentId);
				rs = pst.executeQuery();
				if (!rs.next())
					throw new InteractRuntimeException("代理不存在");
				pst.close();
			}

			pst = connection.prepareStatement(
					"insert into t_user (id,phone,password,password_md5,register_time,from_agent_id) values(?,?,?,?,?,?)");
			pst.setObject(1, RandomStringUtils.randomNumeric(12));
			pst.setObject(2, phone);
			pst.setObject(3, password);
			pst.setObject(4, DigestUtils.md5Hex(password));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, agentId);

			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("注册失败");
			pst.close();

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

	public static void main(String[] args) {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		SysConstant.jedisPool = new JedisPool(config, "121.40.168.181", 6480, 10000, "Lx1990159sJspeafdQM");
		Jedis jedis = SysConstant.jedisPool.getResource();
		UserLoginStatus loginStatus = new UserLoginStatus();
		loginStatus.setUserId("1");
		loginStatus.setAdminIs(0);
		loginStatus.setPhone("11111111111");
		loginStatus.setLoginTime(new Date().getTime());
		jedis.set("888888888888", JSON.toJSONString(loginStatus));
		jedis.close();
	}
}