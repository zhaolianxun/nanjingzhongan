package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import rrightway.constant.OutApis;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.SafetySetEntrance")
@RequestMapping(value = "/p/m/safetysetent")
public class SafetySetEntrance {

	public static Logger logger = Logger.getLogger(SafetySetEntrance.class);

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
			pst = connection.prepareStatement(
					"update t_user set paypwd=?,paypwd_md5=? where id=? and (isnull(paypwd) or length(paypwd)=0  or isnull(paypwd_md5) or length(paypwd_md5)=0)");
			pst.setObject(1, paypwd);
			pst.setObject(2, DigestUtils.md5Hex(paypwd));
			pst.setObject(3, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("失败，已设置过支付密码，请使用修改操作。");

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

	@RequestMapping(value = "/paypwdexist")
	public void paypwdexist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select paypwd,paypwd_md5 from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int paypwdExist = 0;
			if (rs.next()) {
				paypwdExist = (StringUtils.isEmpty(rs.getString("paypwd"))
						|| StringUtils.isEmpty(rs.getString("paypwd_md5"))) ? 0 : 1;
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("paypwdExist", paypwdExist);
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

	@RequestMapping(value = "/boundphoneexist")
	public void boundphoneexist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select insert(phone, 4, 4, '****') phone from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			int phoneExist = 0;
			String existingPhone = null;
			if (rs.next()) {
				existingPhone = rs.getString("phone");
				phoneExist = StringUtils.isEmpty(existingPhone) ? 0 : 1;
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("phoneExist", phoneExist);
			data.put("existingPhone", existingPhone);
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
			pst = connection
					.prepareStatement("update t_user set phone=? where id=? and (isnull(phone) or length(phone)=0)");
			pst.setObject(1, phone);
			pst.setObject(2, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("手机号已绑定过，请使用修改操作");

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

	@RequestMapping(value = "/alterbindphone")
	public void alterbindphone(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phoneOld = StringUtils.trimToNull(request.getParameter("phone_old"));
			if (phoneOld == null)
				throw new InteractRuntimeException("phone_old 不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");
			String vcode = StringUtils.trimToNull(request.getParameter("vcode"));
			if (vcode == null)
				throw new InteractRuntimeException("vcode 不可空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 短信校验
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=")
					.append(phoneOld).append("&verification_code=").append(vcode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getInteger("code") != 0)
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set phone=? where id=? and phone=?");
			pst.setObject(1, phone);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, phoneOld);
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
				throw new InteractRuntimeException("address 不可空");
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			if (tel == null)
				throw new InteractRuntimeException("tel 不可空");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");

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

	@RequestMapping(value = "/getreceiverinfo")
	public void getreceiverinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection
					.prepareStatement("select receiver_address,receiver_name,receiver_tel from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			if (rs.next()) {
				data.put("receiverAddress", "receiver_address");
				data.put("receiverName", "receiver_name");
				data.put("receiverTel", "receiver_tel");
			}
			pst.close();
			// 返回结果
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

	@RequestMapping(value = "/setbankinfo")
	public void setbankinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String belonger = StringUtils.trimToNull(request.getParameter("belonger"));
			if (belonger == null)
				throw new InteractRuntimeException("belonger 不可空");
			String bankname = StringUtils.trimToNull(request.getParameter("bankname"));
			if (bankname == null)
				throw new InteractRuntimeException("bankname 不可空");
			String cardno = StringUtils.trimToNull(request.getParameter("cardno"));
			if (cardno == null)
				throw new InteractRuntimeException("cardno 不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不可空");
			String paypwd = StringUtils.trimToNull(request.getParameter("paypwd"));
			if (paypwd == null)
				throw new InteractRuntimeException("paypwd 不可空");

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

	/**
	 * 根据原密码修改密码
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/alterpwdbysrc")
	public void alterPwdBySms(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String oldPwd = StringUtils.trimToNull(request.getParameter("old_pwd"));
			if (oldPwd == null)
				throw new InteractRuntimeException("old_pwd 不可空");
			String newPwd = StringUtils.trimToNull(request.getParameter("new_pwd"));
			if (newPwd == null)
				throw new InteractRuntimeException("new_pwd 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set pwd=?,pwd_md5=? where id=? and pwd_md5=?");
			pst.setObject(1, newPwd);
			pst.setObject(2, DigestUtils.md5Hex(newPwd));
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, DigestUtils.md5Hex(oldPwd));
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

	@RequestMapping(value = "/alterpaypwdbysrc")
	public void alterPayPwdBySrc(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String oldPwd = StringUtils.trimToNull(request.getParameter("old_pwd"));
			if (oldPwd == null)
				throw new InteractRuntimeException("old_pwd 不可空");
			String newPwd = StringUtils.trimToNull(request.getParameter("new_pwd"));
			if (newPwd == null)
				throw new InteractRuntimeException("new_pwd 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set paypwd=?,paypwd_md5=? where id=? and paypwd_md5=?");
			pst.setObject(1, newPwd);
			pst.setObject(2, DigestUtils.md5Hex(newPwd));
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, DigestUtils.md5Hex(oldPwd));
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

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
			connection = RrightwayDataSource.dataSource.getConnection();
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

	@RequestMapping(value = "/alterpaypwdbysmsvcode")
	public void alterPaypwdBySmsvcode(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

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
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_user set paypwd=?,paypwd_md5=? where phone=?");
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
}