package cszyylxm.api;

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

import cszyylxm.business.LoginStatus;
import cszyylxm.constant.SysConstant;
import cszyylxm.util.CszyylxmDataSource;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.InteractRuntimeException;
import redis.clients.jedis.Jedis;

@Controller("api.UserAction")
@RequestMapping(value = "/useraction")
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
			pst = connection.prepareStatement("select id,pwd_md5,account,type,realname from t_user where account=?");
			pst.setObject(1, account);
			ResultSet rs = pst.executeQuery();
			LoginStatus loginStatus = new LoginStatus();
			if (rs.next()) {
				if (!DigestUtils.md5Hex(pwd).equals(rs.getString("pwd_md5")))
					throw new InteractRuntimeException("密码错误");
				loginStatus.setLoginTime(new Date().getTime());
				loginStatus.setUserId(rs.getString("id"));
				loginStatus.setAccount(rs.getString("account"));
				loginStatus.setType(rs.getInt("type"));
				loginStatus.setRealname(rs.getString("realname"));
			} else
				throw new InteractRuntimeException(1404, "账号不存在");

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set("cszyylxm.token-" + token, JSON.toJSONString(loginStatus));
			jedis.set(loginStatus.getUserId(), token);

			jedis.expire(loginStatus.getUserId(), 7 * 24 * 60 * 60);
			jedis.expire("cszyylxm.token-" + token, 7 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", loginStatus.getUserId());
			data.put("realname", loginStatus.getRealname());
			data.put("type", loginStatus.getType());

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
			LoginStatus loginStatus = LoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			String token = loginStatus.getToken();
			String userId = loginStatus.getUserId();

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id,account,type,realname from t_user where id=?");
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				loginStatus.setUserId(rs.getString("id"));
				loginStatus.setAccount(rs.getString("account"));
				loginStatus.setType(rs.getInt("type"));
				loginStatus.setRealname(rs.getString("realname"));
			} else
				throw new InteractRuntimeException(20);

			LoginStatus.refreshLoginStatus(jedis, token, loginStatus);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("userId", loginStatus.getUserId());
			data.put("realname", loginStatus.getRealname());
			data.put("type", loginStatus.getType());
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
			LoginStatus loginStatus = LoginStatus.todo(request, jedis);
			if (loginStatus != null) {
				jedis.del("cszyylxm.token-" + loginStatus.getToken());
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

	@RequestMapping(value = "/accountexist")
	public void accountexist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String account = StringUtils.trimToNull(request.getParameter("account"));
			if (account == null)
				throw new InteractRuntimeException("account 不可空");

			// 业务处理

			// 更新密码
			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where account=?");
			pst.setObject(1, account);
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
}