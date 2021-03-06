package easywin.module.plat.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.util.EasywinDataSource;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

@Component
public class RefreshAuthorizerAccessToken {

	private static Logger logger = Logger.getLogger(RefreshAuthorizerAccessToken.class);

	public RefreshAuthorizerAccessToken() {
		logger.info("启动定时任务 RefreshAuthorizerAccessToken");
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void run() {
		logger.debug("执行 task.RefreshAuthorizerAccessToken 定时任务");
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取componentAccessToken
			jedis = SysConstant.jedisPool.getResource();
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			logger.debug("componentAccessToken " + componentAccessToken);
			if (StringUtils.isEmpty(componentAccessToken))
				return;

			jedis.close();
			jedis = null;

			String url = new StringBuilder("https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?")
					.append("component_access_token=").append(componentAccessToken).toString();
			logger.debug("url  " + url);

			connection = EasywinDataSource.dataSource.getConnection();
			// 获取将过期token列表
			pst = connection.prepareStatement(
					"select id,refresh_token,wx_appid from t_app where authorized=1 and (expires_in-rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')) <= 600000  limit 0,50");
			ResultSet rs = pst.executeQuery();
			JSONArray ja = new JSONArray();
			while (rs.next()) {
				JSONObject jo = new JSONObject();
				jo.put("id", rs.getString("id"));
				jo.put("refreshToken", rs.getString("refresh_token"));
				jo.put("appId", rs.getString("wx_appid"));
				ja.add(jo);
			}
			pst.close();
			connection.setAutoCommit(false);
			// 循环刷新所有token
			for (int i = 0; i < ja.size(); i++) {
				try {
					JSONObject jo = ja.getJSONObject(i);
					String id = jo.getString("id");
					String oldRefreshToken = jo.getString("refreshToken");
					String appId = jo.getString("appId");
					// 数据库中锁定记录行
					pst = connection.prepareStatement("select id from t_app where id=? for update");
					pst.setString(1, id);
					pst.executeQuery();
					pst.close();

					// 从微信获取新token
					JSONObject content = new JSONObject();
					content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);
					content.put("authorizer_appid", appId);
					content.put("authorizer_refresh_token", oldRefreshToken);
					logger.debug(content);
					Request okHttpRequest = new Request.Builder().url(url)
							.post(RequestBody.create(MediaType.parse("application/json"), content.toJSONString()))
							.build();
					Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
					String responseBody = okHttpResponse.body().string();
					logger.debug("responseBody  " + responseBody);

					JSONObject resultVo = JSON.parseObject(responseBody);

					int errcode = resultVo.getIntValue("errcode");
					if (errcode != 0)
						throw new InteractRuntimeException(resultVo.getString("errmsg"));

					String authorizerAccessToken = resultVo.getString("authorizer_access_token");
					int expiresIn = resultVo.getIntValue("expires_in");
					String authorizerRefreshToken = resultVo.getString("authorizer_refresh_token");

					// 新token入库
					pst = connection.prepareStatement(
							"update t_app set access_token=?,expires_in=?,refresh_token=? where id=?");
					pst.setObject(1, authorizerAccessToken);
					pst.setObject(2, new Date().getTime() + expiresIn * 1000l);
					pst.setObject(3, authorizerRefreshToken);
					pst.setObject(4, id);
					int n = pst.executeUpdate();
					if (n != 1)
						throw new InteractRuntimeException("更新失败");
					pst.close();
					connection.commit();
				} catch (Exception e) {
					logger.info(ExceptionUtils.getStackTrace(e));
					connection.rollback();
				} finally {
					if (pst != null)
						pst.close();
				}
			}

		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (jedis != null)
				jedis.close();
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			logger.debug("执行结束 task.RefreshAuthorizerAccessToken 定时任务");
		}
	}

}
