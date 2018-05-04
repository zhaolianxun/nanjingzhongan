package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;

@Controller("plat.api.mobile.BindTaobaoEntrance")
@RequestMapping(value = "/p/m/bindtaobao")
public class BindTaobaoEntrance {

	public static Logger logger = Logger.getLogger(BindTaobaoEntrance.class);

	@RequestMapping(value = "/getoauthh5url")
	public void getoauthh5url(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// https://oauth.taobao.com/authorize?response_type=code&client_id=23075594&redirect_uri=http://www.oauth.net/2/&state=1212&view=web
			String url = new StringBuilder(SysConstant.ali_open_oauth_url).append("?response_type=code&")
					.append("client_id=").append(SysConstant.ali_open_app_rrightway_appkey).append("&redirect_uri=")
					.append(SysConstant.project_rooturl).append("/p/m/bindtaobao/getoauthnotify").append("&state=")
					.append(loginStatus.getUserId()).append("&view=wap").toString();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("url", url);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/getoauthnotify")
	public void getoauthnotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String code = request.getParameter("code");
			String state = request.getParameter("state");

			// 业务处理
			String userId = state;
			// https://oauth.taobao.com/authorize?response_type=code&client_id=23075594&redirect_uri=http://www.oauth.net/2/&state=1212&view=web
			String body = new StringBuilder().append("client_id=").append(SysConstant.ali_open_app_rrightway_appkey)
					.append("&client_secret=").append(SysConstant.ali_open_app_rrightway_appsecret)
					.append("&grant_type=authorization_code&code=").append(code).append("&redirect_uri=")
					.append(SysConstant.project_rooturl).append("&view=wap").toString();

			Request okHttpRequest = new Request.Builder().url(SysConstant.ali_open_gettoken_url)
					.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body)).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);
			
			// 返回结果
			JSONObject data = new JSONObject();
			HttpRespondWithData.todo(request, response, 0, null, data);
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
}