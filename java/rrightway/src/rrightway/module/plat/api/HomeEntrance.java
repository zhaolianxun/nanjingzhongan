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

import redis.clients.jedis.Jedis;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.HomeEntrance")
@RequestMapping(value = "/p/homee")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "/home")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			jedis.set("rrightway.plat.token-" + token, JSON.toJSONString(loginStatus));
			jedis.set(loginStatus.getUserId(), token);

			jedis.expire(loginStatus.getUserId(), 7 * 24 * 60 * 60);
			jedis.expire("rrightway.plat.token-" + token, 7 * 24 * 60 * 60);

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

}