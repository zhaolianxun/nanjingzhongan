package zaylt.module.client.business;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import zaylt.constant.SysConstant;
import zaylt.module.client.entity.UserLoginStatus;

public class GetLoginStatus {

	public static UserLoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		if (token == null)
			return null;
		String loginRedisKey = new StringBuilder("zaylt.client.login-").append(token).toString();
		String statusStr = jedis.get(loginRedisKey);
		if (statusStr == null || statusStr.equals(""))
			return null;
		UserLoginStatus status = JSON.parseObject(statusStr, UserLoginStatus.class);
		status.setToken(token);
		return status;
	}

	public static UserLoginStatus todo(HttpServletRequest request) throws Exception {
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
			UserLoginStatus status = JSON.parseObject(statusStr, UserLoginStatus.class);
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