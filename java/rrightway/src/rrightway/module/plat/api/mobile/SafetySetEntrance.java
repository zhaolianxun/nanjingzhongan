package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

@Controller("plat.api.mobile.SafetySetEntrance")
@RequestMapping(value = "/p/m/safetysetent")
public class SafetySetEntrance {

	public static Logger logger = Logger.getLogger(SafetySetEntrance.class);

	@RequestMapping(value = "/ent")
	public void safetysetentEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			pst = connection.prepareStatement(
					"select if((isnull(t.receiver_address)||length(t.receiver_address)=0),0,1) receiver_address_setted,if((isnull(t.paypwd_md5)||length(t.paypwd_md5)=0),0,1) paypwd_setted,if((isnull(t.phone)||length(t.phone)=0),0,1) phone_bound,(select if(count(id)>0,1,0) from t_user_bankcard where user_id=t.id) bankcard_bound from t_user t where t.id= ? ");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject data = new JSONObject();
			if (rs.next()) {
				data.put("paypwdSetted", rs.getInt("paypwd_setted"));
				data.put("phoneBound", rs.getInt("phone_bound"));
				data.put("bankcardBound", rs.getInt("bankcard_bound"));
				data.put("receiverAddressSetted", rs.getInt("receiver_address_setted"));
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
		Jedis jedis = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String vcode = StringUtils.trimToNull(request.getParameter("vcode"));
			if (vcode == null)
				throw new InteractRuntimeException("vcode不可空");
			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
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
			pst = connection.prepareStatement("select count(id) from t_user where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0)
					throw new InteractRuntimeException("手机号存在");
			}
			pst.close();

			pst = connection
					.prepareStatement("update t_user set phone=? where id=? and (isnull(phone) or length(phone)=0)");
			pst.setObject(1, phone);
			pst.setObject(2, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("手机号已绑定过，请使用修改操作");

			loginStatus.setPhone(phone);
			GetLoginStatus.refreshLoginStatus(jedis, loginStatus.getToken(), loginStatus);
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
		Jedis jedis = null;
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
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
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

			loginStatus.setPhone(phone);
			GetLoginStatus.refreshLoginStatus(jedis, loginStatus.getToken(), loginStatus);

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

	@RequestMapping(value = "/addreceiveraddress")
	public void addReceiverAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String address = StringUtils.trimToNull(request.getParameter("full_address"));
			if (address == null)
				throw new InteractRuntimeException("full_address 不可空");
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			if (tel == null)
				throw new InteractRuntimeException("tel 不可空");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");
			String defIfParam = StringUtils.trimToNull(request.getParameter("def_if"));
			int defIf = defIfParam == null ? 0 : 1;

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"insert into t_receiver_address (user_id,realname,tel,full_address,def_if,add_time) values(?,?,?,?,?,?)");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, name);
			pst.setObject(3, tel);
			pst.setObject(4, address);
			pst.setObject(5, defIf);
			pst.setObject(5, new Date().getTime());
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

	@RequestMapping(value = "/alterreceiveraddress")
	public void alterReceiverAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String addressIdParam = StringUtils.trimToNull(request.getParameter("address_id"));
			if (addressIdParam == null)
				throw new InteractRuntimeException("address_id 不可空");
			int addressId = Integer.parseInt(addressIdParam);
			String address = StringUtils.trimToNull(request.getParameter("full_address"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String defIfParam = StringUtils.trimToNull(request.getParameter("def_if"));
			Integer defIf = defIfParam == null ? null : Integer.parseInt(defIfParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			List<Object> sqlParams = new ArrayList<Object>();
			if (address != null)
				sqlParams.add(address);
			if (tel != null)
				sqlParams.add(tel);
			if (name != null)
				sqlParams.add(name);
			if (defIf != null)
				sqlParams.add(defIf);
			sqlParams.add(addressId);
			pst = connection.prepareStatement(new StringBuilder("update t_receiver_address set id=id")
					.append(address == null ? "" : ",full_address=?").append(tel == null ? "" : ",tel=?")
					.append(name == null ? "" : ",realname=?").append(defIf == null ? "" : ",def_if=?")
					.append(" where id=?").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
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

	@RequestMapping(value = "/getreceiveraddresslist")
	public void getreceiveraddresslist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.realname,t.postcode,t.tel,t.full_address,t.def_if,t.add_time from t_receiver_address t where t.user_id=? order by t.def_if desc,t.add_time desc")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("addressId", rs.getInt("id"));
				item.put("realname", rs.getString("realname"));
				item.put("postcode", rs.getString("postcode"));
				item.put("tel", rs.getString("tel"));
				item.put("fullAddress", rs.getString("full_address"));
				item.put("defIf", rs.getInt("def_if"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/delreceiveraddress")
	public void delReceiverAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String addressIdParam = StringUtils.trimToNull(request.getParameter("address_id"));
			if (addressIdParam == null)
				throw new InteractRuntimeException("address_id 不可空");
			int addressId = Integer.parseInt(addressIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 更新密码
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection
					.prepareStatement(new StringBuilder("delete from t_receiver_address where id=?").toString());
			pst.setObject(1, addressId);
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
				data.put("receiverAddress", rs.getString("receiver_address"));
				data.put("receiverName", rs.getString("receiver_name"));
				data.put("receiverTel", rs.getString("receiver_tel"));
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
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select paypwd_md5 from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				String paypwdMd5 = rs.getString("paypwd_md5");
				if (paypwdMd5 == null || paypwdMd5.isEmpty())
					throw new InteractRuntimeException(1001, "请先至'安全设置'设置支付密码");
				if (!DigestUtils.md5Hex(paypwd).equals(paypwdMd5))
					throw new InteractRuntimeException("支付密码错误");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			pst = connection.prepareStatement(
					"insert into t_user_bankcard (user_id,bankname,cardno,phone,belonger,status) values(?,?,?,?,?,2)");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, bankname);
			pst.setObject(3, cardno);
			pst.setObject(4, phone);
			pst.setObject(5, belonger);
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement("update t_user set realname=? where id=?");
			pst.setObject(1, belonger);
			pst.setObject(2, loginStatus.getUserId());
			n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
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

	@RequestMapping(value = "/updatebankinfo")
	public void updatebankinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
				String paypwdMd5 = rs.getString("paypwd_md5");
				if (paypwdMd5 == null || paypwdMd5.isEmpty())
					throw new InteractRuntimeException("请先设置支付密码");
				if (!DigestUtils.md5Hex(paypwd).equals(paypwdMd5))
					throw new InteractRuntimeException("支付密码错误");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			pst = connection.prepareStatement(
					"update t_user_bankcard set  bankname=?,cardno=?,phone=?,belonger=?,status=2 where user_id=?");
			pst.setObject(1, bankname);
			pst.setObject(2, cardno);
			pst.setObject(3, phone);
			pst.setObject(4, belonger);
			pst.setObject(5, loginStatus.getUserId());
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

	@RequestMapping(value = "/getbankinfo")
	public void getbankinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select status,belonger,phone,cardno,bankname from t_user_bankcard t where t.user_id=?");
			pst.setObject(1, loginStatus.getUserId());
			JSONObject item = new JSONObject();
			ResultSet rs = pst.executeQuery();
			String belonger = null;
			String phone = null;
			String cardno = null;
			String bankname = null;
			Integer status = null;
			if (rs.next()) {
				belonger = rs.getString("belonger");
				phone = rs.getString("phone");
				cardno = rs.getString("cardno");
				bankname = rs.getString("bankname");
				status = rs.getInt("status");

			}
			pst.close();

			item.put("belonger", belonger);
			item.put("phone", phone);
			item.put("cardno", cardno);
			item.put("bankname", bankname);
			item.put("status", status);

			JSONObject data = new JSONObject();
			data.putAll(item);
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

	/**
	 * 根据原密码修改密码
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/alterpwdbysrc")
	public void alterpwdbysrc(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			if (loginStatus.getPhone() == null || loginStatus.getPhone().isEmpty())
				throw new InteractRuntimeException(1002, "未绑定手机号");
			if (!loginStatus.getPhone().equals(phone))
				throw new InteractRuntimeException("手机号错误");
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
			if (loginStatus.getPhone() == null || loginStatus.getPhone().isEmpty())
				throw new InteractRuntimeException(1002, "未绑定手机号");

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select phone from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (!rs.getString("phone").equals(phone))
					throw new InteractRuntimeException("手机号错误");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

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

	@RequestMapping(value = "/buyertaobaoaccounts")
	public void buyertaobaoaccounts(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select if((isnull(u.realname) || length(u.realname)=0),0,1) bankinfo_fill_if,t.alipay_account,u.realname,t.id,t.taobao_user_nick,t.status,t.idcard_front,t.idcard_back,t.idcard_no,t.audit_fail_reason,if((isnull(u.paypwd_md5)||length(u.paypwd_md5)=0),0,1) paypwd_setted from t_taobaoaccount t inner join t_user u on t.user_id=u.id where t.user_id=? and t.type=1")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("taobaoAccountId", rs.getInt("id"));
				item.put("taobaoUserNick", rs.getString("taobao_user_nick"));
				item.put("status", rs.getInt("status"));
				item.put("idcardFront", rs.getString("idcard_front"));
				item.put("idcardBack", rs.getString("idcard_back"));
				item.put("idcardNo", rs.getString("idcard_no"));
				item.put("alipayAccount", rs.getString("alipay_account"));
				item.put("realname", rs.getString("realname"));
				item.put("bankinfoFillIf", rs.getInt("bankinfo_fill_if"));
				item.put("auditFailReason", rs.getString("audit_fail_reason"));
				item.put("paypwdSetted", rs.getString("paypwd_setted"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/sellertaobaoaccounts")
	public void sellertaobaoaccounts(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.taobao_user_nick from t_taobaoaccount t where t.user_id=? and t.type=2").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getInt("id"));
				item.put("taobaoUserNick", rs.getString("taobao_user_nick"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/completetaobaoinfo")
	public void completetaobaoinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String taobaoaccountIdParam = StringUtils.trimToNull(request.getParameter("taobaoaccount_id"));
			if (taobaoaccountIdParam == null)
				throw new InteractRuntimeException("taobaoaccount_id 不可空");
			int taobaoaccountId = Integer.parseInt(taobaoaccountIdParam);
			String idcardFront = StringUtils.trimToNull(request.getParameter("idcard_front"));
			if (idcardFront == null)
				throw new InteractRuntimeException("idcard_front 不可空");
			String idcardBack = StringUtils.trimToNull(request.getParameter("idcard_back"));
			if (idcardBack == null)
				throw new InteractRuntimeException("idcard_back 不可空");
			String idcardNo = StringUtils.trimToNull(request.getParameter("idcard_no"));
			if (idcardNo == null)
				throw new InteractRuntimeException("idcard_no 不可空");
			String alipayAccount = StringUtils.trimToNull(request.getParameter("alipay_account"));
			if (alipayAccount == null)
				throw new InteractRuntimeException("alipay_account 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("update t_taobaoaccount set alipay_account=null where id=?");
			pst.setObject(1, taobaoaccountId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(new StringBuilder(
					"select (select count(id) from t_user_bankcard where user_id=?) bank_if,(select count(id) from t_taobaoaccount where alipay_account=?) alipay_if")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, alipayAccount);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getInt("bank_if") == 0)
					throw new InteractRuntimeException("请补全银行卡信息");
				if (rs.getInt("alipay_if") == 1)
					throw new InteractRuntimeException("支付宝已存在");
			}
			pst.close();

			pst = connection.prepareStatement(
					"update t_taobaoaccount set idcard_front=?,idcard_back=?,idcard_no=?,alipay_account=?,status=1 where id=?");
			pst.setObject(1, idcardFront);
			pst.setObject(2, idcardBack);
			pst.setObject(3, idcardNo);
			pst.setObject(4, alipayAccount);
			pst.setObject(5, taobaoaccountId);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");
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

	@RequestMapping(value = "/unbindtaobaoaccount")
	public void unbindtaobaoaccount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String taobaoaccountIdParam = StringUtils.trimToNull(request.getParameter("taobaoaccount_id"));
			if (taobaoaccountIdParam == null)
				throw new InteractRuntimeException("taobaoaccount_id 不可空");
			int taobaoaccountId = Integer.parseInt(taobaoaccountIdParam);
			String paypwd = StringUtils.trimToNull(request.getParameter("paypwd"));
			if (paypwd == null)
				throw new InteractRuntimeException("paypwd 不可空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select count(id) from t_order where finished=0 and (buyer_taobaoaccount_id=? or seller_taobaoaccount_id=?)");
			pst.setObject(1, taobaoaccountId);
			pst.setObject(2, taobaoaccountId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0)
					throw new InteractRuntimeException("此淘宝账号正在参与交易中，无法取消绑定！");
			}
			pst.close();

			pst = connection.prepareStatement("select count(id) from t_activity where status=1 and taobaoaccount_id=?");
			pst.setObject(1, taobaoaccountId);
			rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0)
					throw new InteractRuntimeException("此淘宝账号中有在线的活动，请下架后再操作。");
			}
			pst.close();

			pst = connection.prepareStatement("select paypwd_md5 from t_user where id=?");
			pst.setObject(1, loginStatus.getUserId());
			rs = pst.executeQuery();
			if (rs.next()) {
				String paypwdMd5 = rs.getString("paypwd_md5");
				if (paypwdMd5 == null || paypwdMd5.isEmpty())
					throw new InteractRuntimeException("未设置支付密码");
				if (!rs.getString("paypwd_md5").equals(DigestUtils.md5Hex(paypwd)))
					throw new InteractRuntimeException("支付密码错误");
			}
			pst.close();

			pst = connection.prepareStatement("delete from t_taobaoaccount where id=?");
			pst.setObject(1, taobaoaccountId);
			int cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
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