package rrightway.module.plat.api;

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
import rrightway.constant.OutApis;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

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
			String account = StringUtils.trimToNull(request.getParameter("account"));
			if (account == null)
				throw new InteractRuntimeException("account不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select id,nickname,realname,seller_is,buyer_is,pwd_md5,phone from t_user where phone=? or username=?");
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
				loginStatus.setNickname(rs.getString("nickname"));
				loginStatus.setRealname(rs.getString("realname"));
				loginStatus.setSellerIs(rs.getInt("seller_is"));
				loginStatus.setBuyerIs(rs.getInt("buyer_is"));
			} else
				throw new InteractRuntimeException("账号不存在");

			String token = RandomStringUtils.randomNumeric(12);
			jedis = SysConstant.jedisPool.getResource();
			jedis.set(token, JSON.toJSONString(loginStatus));
			jedis.set(loginStatus.getUserId(), token);

			jedis.expire(loginStatus.getUserId(), 7 * 24 * 60 * 60);
			jedis.expire(token, 7 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", loginStatus.getUserId());
			data.put("phone", loginStatus.getPhone());
			data.put("realname", loginStatus.getRealname());
			data.put("nickname", loginStatus.getNickname());
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

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection
					.prepareStatement("select phone,nickname,realname,seller_is,buyer_is from t_user where id=?");
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				loginStatus.setPhone(rs.getString("phone"));
				loginStatus.setNickname(rs.getString("nickname"));
				loginStatus.setRealname(rs.getString("realname"));
				loginStatus.setSellerIs(rs.getInt("seller_is"));
				loginStatus.setBuyerIs(rs.getInt("buyer_is"));
			} else
				throw new InteractRuntimeException(20);

			jedis.set(token, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);

			jedis.expire(userId, 7 * 24 * 60 * 60);
			jedis.expire(token, 7 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("userId", userId);
			data.put("phone", loginStatus.getPhone());
			data.put("realname", loginStatus.getRealname());
			data.put("nickname", loginStatus.getNickname());
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

	@RequestMapping(value = "/alterpwdbysrc")
	public void alterPwdBySms(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String oldPwd = StringUtils.trimToNull(request.getParameter("old_pwd"));
			if (oldPwd == null)
				throw new InteractRuntimeException("old_pwd不可空");
			String newPwd = StringUtils.trimToNull(request.getParameter("new_pwd"));
			if (newPwd == null)
				throw new InteractRuntimeException("new_pwd不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set password=?,password_md5=? where id=? and pwd_md5=?");
			pst.setObject(1, newPwd);
			pst.setObject(2, DigestUtils.md5Hex(newPwd));
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(3, DigestUtils.md5Hex(oldPwd));
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("原密码错误");

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
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("username不可空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd不可空");
			String qq = StringUtils.trimToNull(request.getParameter("qq"));
			if (qq == null)
				throw new InteractRuntimeException("qq不可空");
			// 业务处理

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id from t_user where username=?");
			pst.setObject(1, username);
			ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new InteractRuntimeException("用户名已存在");
			pst.close();

			pst = connection.prepareStatement(
					"insert into t_user (id,username,password,password_md5,register_time,qq) values(?,?,?,?,?,?)");
			pst.setObject(1, RandomStringUtils.randomNumeric(12));
			pst.setObject(2, username);
			pst.setObject(3, pwd);
			pst.setObject(4, DigestUtils.md5Hex(pwd));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, qq);

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

	@RequestMapping(value = "/setpaypwd")
	public void setpaypwd(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String paypwd = StringUtils.trimToNull(request.getParameter("paypwd"));
			if (paypwd == null)
				throw new InteractRuntimeException("paypwd不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set paypwd=?,paypwd_md5=? where id=?");
			pst.setObject(1, paypwd);
			pst.setObject(2, DigestUtils.md5Hex(paypwd));
			pst.setObject(3, loginStatus.getUserId());
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

	@RequestMapping(value = "/bindphone")
	public void bindphone(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String vcode = StringUtils.trimToNull(request.getParameter("vcode"));
			if (vcode == null)
				throw new InteractRuntimeException("vcode不可空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 短信校验
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(vcode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getInteger("code") != 0)
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set phone=? where id=?");
			pst.setObject(1, phone);
			pst.setObject(2, loginStatus.getUserId());
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

	@RequestMapping(value = "/setreceiverinfo")
	public void setreceiverinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String address = StringUtils.trimToNull(request.getParameter("address"));
			if (address == null)
				throw new InteractRuntimeException("address不可空");
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			if (tel == null)
				throw new InteractRuntimeException("vcodetel");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection
					.prepareStatement("update t_user set receiver_address=?,receiver_name=?,receiver_tel=? where id=?");
			pst.setObject(1, address);
			pst.setObject(2, name);
			pst.setObject(3, tel);
			pst.setObject(4, loginStatus.getUserId());
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

	@RequestMapping(value = "/setbankinfo")
	public void setbankinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String belonger = StringUtils.trimToNull(request.getParameter("belonger"));
			if (belonger == null)
				throw new InteractRuntimeException("belonger不可空");
			String bankname = StringUtils.trimToNull(request.getParameter("bankname"));
			if (bankname == null)
				throw new InteractRuntimeException("bankname");
			String cardno = StringUtils.trimToNull(request.getParameter("cardno"));
			if (cardno == null)
				throw new InteractRuntimeException("cardno");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone");
			String paypwd = StringUtils.trimToNull(request.getParameter("paypwd"));
			if (paypwd == null)
				throw new InteractRuntimeException("paypwd");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select paypwd_md5 from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (DigestUtils.md5Hex(paypwd).equals(rs.getString("paypwd_md5")))
					throw new InteractRuntimeException("支付密码错误");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			pst = connection.prepareStatement(
					"insert into t_user_bankcard (user_id,bankname,cardno,phone,belonger) values(?,?,?,?,?)");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, bankname);
			pst.setObject(3, cardno);
			pst.setObject(4, phone);
			pst.setObject(5, belonger);
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
}