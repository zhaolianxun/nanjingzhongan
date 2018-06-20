package zaylt.business;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import zaylt.constant.SysConstant;

public class LoginStatus {

	private String token;
	private String userId;
	private String phone;
	private String type;
	private Integer hospitalId;
	private Integer clinicId;
	private long loginTime;

	public Integer getHospitalId() {
		return hospitalId;
	}

	public void setHospitalId(Integer hospitalId) {
		this.hospitalId = hospitalId;
	}

	public Integer getClinicId() {
		return clinicId;
	}

	public void setClinicId(Integer clinicId) {
		this.clinicId = clinicId;
	}

	public String getToken() {
		return token;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public static LoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		if (token == null)
			return null;
		String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
		String statusStr = jedis.get(loginRedisKey);
		if (statusStr == null || statusStr.equals(""))
			return null;
		LoginStatus status = JSON.parseObject(statusStr, LoginStatus.class);
		status.setToken(token);
		return status;
	}

	public static LoginStatus todo(HttpServletRequest request) throws Exception {
		Jedis jedis = null;
		try {
			String token = (String) request.getParameter("token");
			if (token == null)
				return null;
			jedis = SysConstant.jedisPool.getResource();
			String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
			String statusStr = jedis.get(loginRedisKey);
			if (statusStr == null || statusStr.equals(""))
				return null;
			LoginStatus status = JSON.parseObject(statusStr, LoginStatus.class);
			status.setToken(token);
			return status;
		} catch (Exception e) {
			throw e;
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

}
