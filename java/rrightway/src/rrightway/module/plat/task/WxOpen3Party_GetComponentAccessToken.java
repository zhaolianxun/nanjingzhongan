package rrightway.module.plat.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

@Component
public class WxOpen3Party_GetComponentAccessToken {

	private static Logger logger = Logger.getLogger(WxOpen3Party_GetComponentAccessToken.class);

	public WxOpen3Party_GetComponentAccessToken() {
		logger.info("启动定时任务 WxOpen3Party_GetComponentAccessToken");
	}

	@Scheduled(cron = "0 0/3 * * * ?")
	public void run() {
		logger.debug("执行 task.WxOpen3Party_GetComponentAccessToken 定时任务");
		Jedis jedis = null;
		try {
			jedis = SysConstant.jedisPool.getResource();
			String componentVerifyTicket = jedis
					.get(SysConstant.wechat_open_thirdparty_AppId + "-componentVerifyTicket");

			logger.debug("componentVerifyTicket " + componentVerifyTicket);

			if (StringUtils.isEmpty(componentVerifyTicket))
				return;

			Long remainExpire = jedis.pttl(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");
			remainExpire = remainExpire / 1000;
			logger.debug("remainExpire " + remainExpire);
			if (remainExpire > 700)
				return;

			String url = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
			logger.debug("url  " + url);

			JSONObject content = new JSONObject();
			content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);
			content.put("component_appsecret", SysConstant.wechat_open_thirdparty_AppSecret);
			content.put("component_verify_ticket", componentVerifyTicket);
			logger.debug("content  " + content);
			
			RequestBody body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());

			Request okHttpRequest = new Request.Builder().url(url).post(body).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			int errcode = resultVo.getIntValue("errcode");
			if (errcode != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			String componentAccessToken = resultVo.getString("component_access_token");
			int expiresIn = resultVo.getIntValue("expires_in");

			jedis.set(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken", componentAccessToken);
			jedis.expire(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken", expiresIn);

		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (jedis != null)
				jedis.close();
			logger.debug("执行结束 task.WxOpen3Party_GetComponentAccessToken 定时任务");
		}
	}

}
