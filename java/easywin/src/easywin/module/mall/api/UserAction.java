package easywin.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.mall.business.GetLoginStatus;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller("mall.api.UserAction")
@RequestMapping(value = "/m/useraction")
public class UserAction {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@RequestMapping(value = "/login")
	@Transactional(rollbackFor = Exception.class)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String jscode = StringUtils.trimToNull(request.getParameter("jscode"));
			if (jscode == null)
				throw new InteractRuntimeException("jscode不可空");
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String nickname = StringUtils.trimToNull(request.getParameter("nickname"));
			String appId = mallId;
			String fromUserId = StringUtils.trimToNull(request.getParameter("from"));
			connection = EasywinDataSource.dataSource.getConnection();
			logger.debug("mallId " + mallId);
			// 查询登录的商城信息
			pst = connection.prepareStatement(
					"select t.access_token,t.wx_appid,t.use_endtime from t_app t inner join t_mall u on t.id=u.id where t.id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("商城不存在");
			}
			String accessToken = rs.getString(1);
			String wxAppid = rs.getString(2);
			Long useEndtime = rs.getLong("use_endtime");
			pst.close();

			if (useEndtime < new Date().getTime()) {
				throw new InteractRuntimeException("商城已到期");
			}

			jedis = SysConstant.jedisPool.getResource();
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			// 微信登录
			String url = new StringBuilder("https://api.weixin.qq.com/sns/component/jscode2session?").append("appid=")
					.append(wxAppid).append("&js_code=").append(jscode).append("&grant_type=").append(accessToken)
					.append("&component_appid=").append(SysConstant.wechat_open_thirdparty_AppId)
					.append("&component_access_token=").append(componentAccessToken).toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			Integer errcode = resultVo.getInteger("errcode");
			if (errcode != null && errcode != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			String openid = resultVo.getString("openid");
			String sessionKey = resultVo.getString("session_key");

			// 更新数据库用户信息
			pst = connection.prepareStatement("select id,phone from t_mall_user where wx_openid=? and mall_id=?");
			pst.setObject(1, openid);
			pst.setObject(2, mallId);
			rs = pst.executeQuery();
			String userId = null;
			String phone = null;
			if (rs.next()) {
				userId = rs.getString(1);
				phone = rs.getString(2);
			}
			pst.close();
			if (userId == null) {
				userId = RandomStringUtils.randomNumeric(12);
				pst = connection.prepareStatement(
						"insert into t_mall_user (id,mall_id,headimg,wx_openid,wx_sessionkey,register_time,register_from_user_id,nickname) values(?,?,?,?,?,rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0'),?,?)");
				pst.setObject(1, userId);
				pst.setObject(2, mallId);
				pst.setObject(3, "");
				pst.setObject(4, openid);
				pst.setObject(5, sessionKey);
				pst.setObject(6, fromUserId);
				pst.setObject(7, nickname);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			} else {
				pst = connection
						.prepareStatement("update t_mall_user set wx_openid=?,wx_sessionkey=?,nickname=? where id=?");
				pst.setObject(1, openid);
				pst.setObject(2, sessionKey);
				pst.setObject(3, nickname);
				pst.setObject(4, userId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			}

			// 缓存登录状态
			String token = RandomStringUtils.randomNumeric(12);

			UserLoginStatus loginStatus = new UserLoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setMallId(mallId);
			loginStatus.setWxOpenid(openid);
			String loginRedisKey = new StringBuilder(SysConstant.MALL_Login_Token_Prefix).append(token).toString();
			//// 清除历史登录状态
			String oldToken = jedis.get(userId);
			if (oldToken != null && !oldToken.isEmpty())
				jedis.del(new StringBuilder(SysConstant.MALL_Login_Token_Prefix).append(oldToken).toString());
			jedis.del(userId);
			//// 设置新登录状态
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			data.put("userId", userId);
			data.put("nickname", loginStatus.getNickname());
			data.put("phone", phone);
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

	/**
	 * 登录刷新
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/selfinfo")
	public void loginRefresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"select t.phone,t.nickname,u.use_endtime from t_mall_user t inner join t_app u on t.mall_id=u.id where t.id=?");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			String nickname = null;
			String phone = null;
			long useEndtime;
			if (rs.next()) {
				phone = rs.getString("phone");
				nickname = rs.getString("nickname");
				useEndtime = rs.getLong("use_endtime");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			String loginRedisKey = new StringBuilder(SysConstant.MALL_Login_Token_Prefix).append(loginStatus.getToken())
					.toString();
			if (useEndtime < new Date().getTime()) {
				jedis.del(loginStatus.getUserId());
				jedis.del(loginRedisKey);
				throw new InteractRuntimeException("商城已到期");
			}

			loginStatus.setNickname(nickname);
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(loginStatus.getUserId(), 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("nickname", nickname);
			data.put("phone", phone);
			data.put("userId", loginStatus.getUserId());
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

	/**
	 * 记录查看商品的足迹
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/recordtrack")
	public void recordTrack(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus != null) {
				connection = EasywinDataSource.dataSource.getConnection();
				// 查詢主轮播图
				pst = connection.prepareStatement(
						"select (select min(price) from t_mall_good_sku where t.id=good_id) price,t.name,t.cover from t_mall_good t where t.id=?");
				pst.setObject(1, goodId);
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					int price = rs.getInt("price");
					String name = rs.getString("name");
					String cover = rs.getString("cover");
					pst.close();

					pst = connection.prepareStatement("delete from t_mall_track where user_id=? and good_id=?");
					pst.setObject(1, loginStatus.getUserId());
					pst.setObject(2, goodId);
					pst.executeUpdate();
					pst.close();

					pst = connection.prepareStatement(
							"insert into t_mall_track (mall_id,user_id,good_id,price,name,cover) values(?,?,?,?,?,?)");
					pst.setObject(1, mallId);
					pst.setObject(2, loginStatus.getUserId());
					pst.setObject(3, goodId);
					pst.setObject(4, price);
					pst.setObject(5, name);
					pst.setObject(6, cover);
					pst.executeUpdate();
					pst.close();
				}
				pst.close();
			}
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

	public static void main(String[] args) {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		SysConstant.jedisPool = new JedisPool(config, "121.40.168.181", 6480, 10000, "Lx1990159sJspeafdQM");
		Jedis jedis = SysConstant.jedisPool.getResource();
		UserLoginStatus loginStatus = new UserLoginStatus();
		loginStatus.setUserId("1");
		loginStatus.setLoginTime(new Date().getTime());
		loginStatus.setMallId("1");
		String loginRedisKey = new StringBuilder("easywin.mall.login-").append("999999999999").toString();
		jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
		jedis.close();
	}
}