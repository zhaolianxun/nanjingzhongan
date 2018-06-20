package cszyylxm.module.plat.api;

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
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import cszyylxm.constant.OutApis;
import cszyylxm.constant.SysConstant;
import cszyylxm.entity.InteractRuntimeException;
import cszyylxm.module.plat.business.GetLoginStatus;
import cszyylxm.module.plat.entity.UserLoginStatus;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.CszyylxmDataSource;

@Controller("plat.api.UserAction")
@RequestMapping(value = "/p/useraction")
public class UserAction {

	public static Logger logger = Logger.getLogger(UserAction.class);

	/**
	 * 登录
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String account = StringUtils.trimToNull(request.getParameter("account"));
			if (account == null)
				throw new InteractRuntimeException("account不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");

			// 业务处理
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select admin_if,id,pwd_md5,phone,username from t_user where phone=? or username=?");
			pst.setObject(1, account);
			pst.setObject(2, account);
			ResultSet rs = pst.executeQuery();
			UserLoginStatus loginStatus = new UserLoginStatus();
			if (rs.next()) {
				if (!DigestUtils.md5Hex(pwd).equals(rs.getString("pwd_md5")))
					throw new InteractRuntimeException("密码错误");
				loginStatus.setLoginTime(new Date().getTime());
				loginStatus.setPhone(rs.getString("phone"));
				loginStatus.setUserId(rs.getString("id"));
				loginStatus.setUsername(rs.getString("username"));
				loginStatus.setAdminIf(rs.getInt("admin_if"));
			} else
				throw new InteractRuntimeException("账号不存在");

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set("cszyylxm.plat.token-" + token, JSON.toJSONString(loginStatus));
			jedis.set(loginStatus.getUserId(), token);

			jedis.expire(loginStatus.getUserId(), 7 * 24 * 60 * 60);
			jedis.expire("cszyylxm.plat.token-" + token, 7 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", loginStatus.getUserId());
			data.put("phone", loginStatus.getPhone());
			data.put("username", loginStatus.getUsername());
			data.put("adminIf", loginStatus.getAdminIf());

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

	/**
	 * 登录刷新
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/loginrefresh")
	public void loginRefresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			String token = loginStatus.getToken();
			String userId = loginStatus.getUserId();

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select phone,username,admin_if from t_user where id=?");
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				loginStatus.setPhone(rs.getString("phone"));
				loginStatus.setUsername(rs.getString("username"));
				loginStatus.setAdminIf(rs.getInt("admin_if"));
			} else
				throw new InteractRuntimeException(20);

			GetLoginStatus.refreshLoginStatus(jedis, token, loginStatus);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("userId", userId);
			data.put("phone", loginStatus.getPhone());
			data.put("username", loginStatus.getUsername());
			data.put("adminIf", loginStatus.getAdminIf());
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

	/**
	 * 登出
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus != null) {
				jedis.del("cszyylxm.plat.token-" + loginStatus.getToken());
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

	@RequestMapping(value = "/alterpwdbysmsvcode")
	public void alterPwdBySmsvcode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");
			String smsvcode = StringUtils.trimToNull(request.getParameter("smsvcode"));
			if (smsvcode == null)
				throw new InteractRuntimeException("smsvcode 不可空");
			String newpwd = StringUtils.trimToNull(request.getParameter("newpwd"));
			if (newpwd == null)
				throw new InteractRuntimeException("newpwd 不可空");

			// 业务处理
			// 短信校验
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(smsvcode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getInteger("code") != 0)
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));

			// 更新密码
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set pwd=?,pwd_md5=? where phone=?");
			pst.setObject(1, newpwd);
			pst.setObject(2, DigestUtils.md5Hex(newpwd));
			pst.setObject(3, phone);
			int n = pst.executeUpdate();
			if (n != 1)
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

	@RequestMapping(value = "/getphonebyusername")
	public void getphonebyusername(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("username 不可空");

			// 业务处理

			// 更新密码
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select phone from t_user where username=?");
			pst.setObject(1, username);
			ResultSet rs = pst.executeQuery();
			String phone = null;
			if (rs.next()) {
				phone = rs.getString(1);
			} else
				throw new InteractRuntimeException("用户名不存在");

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("phone", phone);
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

	@RequestMapping(value = "/checkphoneexist")
	public void checkphoneexist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");

			// 业务处理

			// 更新密码
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			int exist = 0;
			if (rs.next()) {
				exist = 1;
			}
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("exist", exist);
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

	/**
	 * 注册
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/register")
	public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String presenterId = StringUtils.trimToNull(request.getParameter("presenter_id"));
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("username 不可空");
			if (username.length() < 6 || username.length() > 20)
				throw new InteractRuntimeException("用户名长度需在6-20位之间");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不可空");
			if (pwd.length() < 6 || pwd.length() > 16)
				throw new InteractRuntimeException("密码长度需在6-16位之间");
			String qq = StringUtils.trimToNull(request.getParameter("qq"));
			if (qq == null)
				throw new InteractRuntimeException("qq 不可空");
			// 业务处理

			// 校验用户名
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where username=?");
			pst.setObject(1, username);
			ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new InteractRuntimeException("用户名已存在");
			pst.close();

			// 插入用户
			pst = connection.prepareStatement(
					"insert into t_user (id,username,pwd,pwd_md5,register_time,qq,register_presenter_id) values(?,?,?,?,?,?,?)");
			pst.setObject(1, RandomStringUtils.randomNumeric(12));
			pst.setObject(2, username);
			pst.setObject(3, pwd);
			pst.setObject(4, DigestUtils.md5Hex(pwd));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, qq);
			pst.setObject(7, presenterId);

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

}