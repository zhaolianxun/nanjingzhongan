package rrightway.module.plat.business;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import rrightway.constant.SysConstant;
import rrightway.module.plat.entity.UserLoginStatus;
import redis.clients.jedis.Jedis;

public class GetLoginStatus {

	public static Logger logger = Logger.getLogger(GetLoginStatus.class);

	public static UserLoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		logger.debug("token : " + token);
		if (token == null)
			return null;
		String statusStr = jedis.get("rrightway.plat.token-" + token);
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
			logger.debug("token : " + token);
			if (token == null)
				return null;
			jedis = SysConstant.jedisPool.getResource();
			String statusStr = jedis.get("rrightway.plat.token-" + token);
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
