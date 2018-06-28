package zaylt.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

public class LoginStatus {

	private String token;
	private String userId;
	private String phone;
	private String type;
	private Integer hospitalId;
	private Integer clinicId;
	private long loginTime;

	private int powerAddHospital;
	private int powerLookAllHospitals;

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

	public int getPowerAddHospital() {
		return powerAddHospital;
	}

	public void setPowerAddHospital(int powerAddHospital) {
		this.powerAddHospital = powerAddHospital;
	}

	public int getPowerLookAllHospitals() {
		return powerLookAllHospitals;
	}

	public void setPowerLookAllHospitals(int powerLookAllHospitals) {
		this.powerLookAllHospitals = powerLookAllHospitals;
	}

	public static void loginRefresh(String userId) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			String token = jedis.get(userId);
			if (token != null && !token.isEmpty()) {
				String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
				String statusStr = jedis.get(loginRedisKey);
				if (statusStr != null && !statusStr.isEmpty()) {
					LoginStatus loginStatus = JSON.parseObject(statusStr, LoginStatus.class);
					connection = ZayltDataSource.dataSource.getConnection();
					pst = connection.prepareStatement(
							"select power_addhospital,power_lookallhospitals,type,id,hospital_id,clinic_id,phone from t_user where id=?");
					pst.setObject(1, userId);
					ResultSet rs = pst.executeQuery();
					int powerAddhospital = 0;
					int powerLookallhospitals = 0;
					String type = null;
					String phone = null;
					Integer hospitalId = null;
					Integer clinicId = null;
					if (rs.next()) {
						powerAddhospital = (Integer) rs.getObject("power_addhospital");
						powerLookallhospitals = (Integer) rs.getObject("power_lookallhospitals");
						type = rs.getString("type");
						phone = rs.getString("phone");
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

					//// 设置新登录状态
					jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
					jedis.set(userId, loginStatus.getToken());
					jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
					jedis.expire(userId, 30 * 24 * 60 * 60);
				}
			}
		} catch (Exception e) {
			// 处理异常
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
