package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.BindTaobaoEntrance")
@RequestMapping(value = "/p/m/bindtaobao")
public class BindTaobaoEntrance {

	public static Logger logger = Logger.getLogger(BindTaobaoEntrance.class);

	@RequestMapping(value = "/getoauthh5url")
	public void getoauthh5url(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String accountTypeParam = StringUtils.trimToNull(request.getParameter("account_type"));
			if (accountTypeParam == null)
				throw new InteractRuntimeException("account_type 不能空");
			Integer accountType = Integer.parseInt(accountTypeParam);
			String go = StringUtils.trimToNull(request.getParameter("go"));
			if (go == null)
				throw new InteractRuntimeException("go 不能空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// https://oauth.taobao.com/authorize?response_type=code&client_id=23075594&redirect_uri=http://www.oauth.net/2/&state=1212&view=web
			String url = new StringBuilder(SysConstant.ali_open_oauth_url).append("?response_type=token&")
					.append("client_id=").append(SysConstant.ali_open_app_rrightway_appkey).append("&redirect_uri=")
					.append(SysConstant.project_rooturl).append(go).append("&state=").append(loginStatus.getUserId())
					.append(",").append(accountType).append("&view=wap").toString();

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

	@RequestMapping(value = "/uploadoauthdata")
	public void uploadoauthdata(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String state = request.getParameter("state");
			String[] stateSplits = state.split(",");
			String userId = stateSplits[0];
			int accountType = Integer.parseInt(stateSplits[1]);
			String taobaoUserNick = StringUtils.trimToNull(request.getParameter("taobao_user_nick"));
			if (taobaoUserNick == null)
				throw new InteractRuntimeException("taobao_user_nick 不能空");
			String taobaoOpenUid = StringUtils.trimToNull(request.getParameter("taobao_open_uid"));
			if (taobaoOpenUid == null)
				throw new InteractRuntimeException("taobao_open_uid 不能空");
			String refreshToken = StringUtils.trimToNull(request.getParameter("refresh_token"));
			if (refreshToken == null)
				throw new InteractRuntimeException("refresh_token 不能空");
			String accessToken = StringUtils.trimToNull(request.getParameter("access_token"));
			if (accessToken == null)
				throw new InteractRuntimeException("access_token 不能空");
			String expiresInParam = StringUtils.trimToNull(request.getParameter("expires_in"));
			if (expiresInParam == null)
				throw new InteractRuntimeException("expires_in 不能空");
			int expiresIn = Integer.parseInt(expiresInParam);

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select id,user_id,type from t_taobaoaccount where taobao_user_id=?");
			pst.setObject(1, taobaoOpenUid);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				String existUserId = rs.getString("user_id");
				int existType = rs.getInt("type");
				int existId = rs.getInt("id");
				pst.close();
				if (!existUserId.equals(userId))
					throw new InteractRuntimeException("该淘宝账号已授权给其他用户");
				if (existType != accountType)
					throw new InteractRuntimeException("该淘宝账号已使用");
				pst = connection.prepareStatement(
						"update t_taobaoaccount set taobao_user_nick=?,access_token=?,expire_time=?,refresh_token=? where id=?");
				pst.setObject(1, taobaoUserNick);
				pst.setObject(2, accessToken);
				pst.setObject(3, new Date().getTime() + expiresIn * 1000l);
				pst.setObject(4, refreshToken);
				pst.setObject(5, existId);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			} else {
				pst = connection.prepareStatement(
						"INSERT INTO `t_taobaoaccount` ( `user_id`, `taobao_user_nick`, `taobao_user_id`,`type`, `access_token`, `expire_time`, `refresh_token`) VALUES ( ?, ?, ?, ?, ?, ?, ?)");
				pst.setObject(1, userId);
				pst.setObject(2, taobaoUserNick);
				pst.setObject(3, taobaoOpenUid);
				pst.setObject(4, accountType);
				pst.setObject(5, accessToken);
				pst.setObject(6, new Date().getTime() + expiresIn * 1000l);
				pst.setObject(7, refreshToken);
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
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

	// @RequestMapping(value = "/getoauthnotify")
	// public void getoauthnotify(HttpServletRequest request,
	// HttpServletResponse response) throws Exception {
	// Connection connection = null;
	// PreparedStatement pst = null;
	// try {
	// // 获取请求参数
	// String code = request.getParameter("code");
	// String state = request.getParameter("state");
	// String[] stateSplits = state.split(",");
	// String userId = stateSplits[0];
	// int accountType = Integer.parseInt(stateSplits[1]);
	//
	// // 业务处理
	// //
	// https://oauth.taobao.com/authorize?response_type=code&client_id=23075594&redirect_uri=http://www.oauth.net/2/&state=1212&view=web
	// String body = new
	// StringBuilder().append("client_id=").append(SysConstant.ali_open_app_rrightway_appkey)
	// .append("&client_secret=").append(SysConstant.ali_open_app_rrightway_appsecret)
	// .append("&grant_type=authorization_code&code=").append(code).append("&redirect_uri=")
	// .append(SysConstant.project_rooturl).append("&view=wap").toString();
	//
	// Request okHttpRequest = new
	// Request.Builder().url(SysConstant.ali_open_gettoken_url)
	// .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
	// body)).build();
	// Response okHttpResponse =
	// SysConstant.okHttpClient.newCall(okHttpRequest).execute();
	// String responseBody = okHttpResponse.body().string();
	// logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " +
	// responseBody);
	// JSONObject resultVo = JSON.parseObject(responseBody);
	// String taobaoOpenUid = resultVo.getString("taobao_open_uid");
	//
	// connection = RrightwayDataSource.dataSource.getConnection();
	// pst = connection.prepareStatement("select id,user_id,type from
	// t_taobaoaccount where taobao_user_id=?");
	// pst.setObject(1, taobaoOpenUid);
	// ResultSet rs = pst.executeQuery();
	// if (rs.next()) {
	// String existUserId = rs.getString("user_id");
	// int existType = rs.getInt("type");
	// int existId = rs.getInt("id");
	// pst.close();
	// if (!existUserId.equals(userId))
	// throw new InteractRuntimeException("该淘宝账号已授权给其他用户");
	// if (existType != accountType)
	// throw new InteractRuntimeException("该淘宝账号已使用");
	// pst = connection.prepareStatement(
	// "update t_taobaoaccount set
	// taobao_user_nick=?,access_token=?,expire_time=?,refresh_token=? where
	// id=?");
	// pst.setObject(1, resultVo.getString("taobao_user_nick"));
	// pst.setObject(2, resultVo.getString("access_token"));
	// pst.setObject(3, resultVo.getLong("expire_time"));
	// pst.setObject(4, resultVo.getString("refresh_token"));
	// pst.setObject(5, existId);
	// int n = pst.executeUpdate();
	// pst.close();
	// if (n != 1)
	// throw new InteractRuntimeException("操作失败");
	// } else {
	// pst = connection.prepareStatement(
	// "INSERT INTO `t_taobaoaccount` ( `user_id`, `taobao_user_nick`,
	// `taobao_user_id`,`type`, `access_token`, `expire_time`, `refresh_token`)
	// VALUES ( ?, ?, ?, ?, ?, ?, ?)");
	// pst.setObject(1, userId);
	// pst.setObject(2, resultVo.getString("taobao_user_nick"));
	// pst.setObject(3, taobaoOpenUid);
	// pst.setObject(4, accountType);
	// pst.setObject(5, resultVo.getString("access_token"));
	// pst.setObject(6, resultVo.getLong("expire_time"));
	// pst.setObject(7, resultVo.getString("refresh_token"));
	// int n = pst.executeUpdate();
	// pst.close();
	// if (n != 1)
	// throw new InteractRuntimeException("操作失败");
	// }
	// // 返回结果
	// HttpRespondWithData.todo(request, response, 0, null, null);
	// } catch (Exception e) {
	// // 处理异常
	// logger.info(ExceptionUtils.getStackTrace(e));
	// HttpRespondWithData.exception(request, response, e);
	// } finally {
	// // 释放资源
	// if (pst != null)
	// pst.close();
	// if (connection != null)
	// connection.close();
	// }
	// }
}