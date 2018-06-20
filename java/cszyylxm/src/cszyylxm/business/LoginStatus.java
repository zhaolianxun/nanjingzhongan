package cszyylxm.business;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import cszyylxm.constant.SysConstant;
import redis.clients.jedis.Jedis;

public class LoginStatus {

	public static Logger logger = Logger.getLogger(LoginStatus.class);

	private String token;
	private String userId;
	private String account;
	private String realname;
	private long loginTime;
	private int type;

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getToken() {
		return token;
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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public static LoginStatus todo(HttpServletRequest request, Jedis jedis) {
		String token = (String) request.getParameter("token");
		logger.debug("token : " + token);
		if (token == null)
			return null;
		String statusStr = jedis.get("cszyylxm.plat.token-" + token);
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
			logger.debug("token : " + token);
			if (token == null)
				return null;
			jedis = SysConstant.jedisPool.getResource();
			String statusStr = jedis.get("cszyylxm.plat.token-" + token);
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

	public static void refreshLoginStatus(Jedis jedis, String token, LoginStatus loginStatus) throws Exception {
		loginStatus.setToken(token);

		jedis.set("cszyylxm.plat.token-" + token, JSON.toJSONString(loginStatus));
		jedis.set(loginStatus.getUserId(), token);

		jedis.expire(loginStatus.getUserId(), 7 * 24 * 60 * 60);
		jedis.expire("cszyylxm.plat.token-" + token, 7 * 24 * 60 * 60);

	}

}
