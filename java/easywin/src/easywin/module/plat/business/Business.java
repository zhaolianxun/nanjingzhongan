package easywin.module.plat.business;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

public class Business {
	public static Logger logger = Logger.getLogger(Business.class);

	public static void refreshWxAppInfo(String appId, Connection connection, Jedis jedis) throws Exception {
		PreparedStatement pst = null;
		try {
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			if (StringUtils.isEmpty(componentAccessToken))
				throw new InteractRuntimeException("componentAccessToken is empty.");

			pst = connection.prepareStatement("select wx_appid from t_app where id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			String wxAppId = null;
			if (rs.next()) {
				wxAppId = rs.getString("wx_appid");
			} else
				throw new InteractRuntimeException("小程序不存在");
			pst.close();

			// 从微信拉取授权者信息
			StringBuilder url = new StringBuilder(
					"https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info?")
							.append("component_access_token=").append(componentAccessToken);
			JSONObject content = new JSONObject();
			content.put("component_appid", SysConstant.wechat_open_thirdparty_AppId);
			content.put("authorizer_appid", wxAppId);

			RequestBody body = RequestBody.create(MediaType.parse("application/json"), content.toJSONString());

			Request okHttpRequest = new Request.Builder().url(url.toString()).post(body).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			if (resultVo.getIntValue("errcode") != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			JSONObject authorizerInfo = resultVo.getJSONObject("authorizer_info");
			pst = connection.prepareStatement(
					"update t_app set nick_name=?,head_img=?,user_name=?,principal_name=?,qrcode_url=?,signature=? where id=?");
			pst.setObject(1, authorizerInfo.getString("nick_name"));
			pst.setObject(2, authorizerInfo.getString("head_img"));
			pst.setObject(3, authorizerInfo.getString("user_name"));
			pst.setObject(4, authorizerInfo.getString("principal_name"));
			pst.setObject(5, authorizerInfo.getString("qrcode_url"));
			pst.setObject(6, authorizerInfo.getString("signature"));
			pst.setObject(7, appId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
		}

	}
}
