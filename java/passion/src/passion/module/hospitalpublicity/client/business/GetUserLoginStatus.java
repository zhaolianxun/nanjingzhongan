package passion.module.hospitalpublicity.client.business;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;

import passion.constant.SysConstant;
import passion.module.hospitalpublicity.client.entity.UserLoginStatus;
import redis.clients.jedis.Jedis;

public class GetUserLoginStatus {

	public static UserLoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		if (token == null)
			return null;
		String statusStr = jedis.get("passion.hospitalpublicity.client.userloginstatus." + token);
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
			String statusStr = jedis.get("passion.hospitalpublicity.client.userloginstatus." + token);
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
