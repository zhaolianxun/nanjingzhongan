package easywin.module.plat.business;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

import easywin.constant.SysConstant;
import easywin.module.plat.entity.UserLoginStatus;
import redis.clients.jedis.Jedis;

public class GetLoginStatus {
	
	

	public static UserLoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		if (token == null)
			return null;
		String statusStr = jedis.get(token);
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
			String statusStr = jedis.get(token);
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
