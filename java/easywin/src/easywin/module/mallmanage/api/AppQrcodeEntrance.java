package easywin.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Controller("mallManage.api.AppQrcodeEntrance")
@RequestMapping(value = "/mm/{mallId}/e/appqrcode")
public class AppQrcodeEntrance {

	public static Logger logger = Logger.getLogger(AppQrcodeEntrance.class);

	@RequestMapping(value = "/appqrcode")
	public void appqrcode(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement("select u.access_token from t_app u where u.id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			String accessToken = null;
			if (rs.next()) {
				accessToken = rs.getString("access_token");
			} else
				throw new InteractRuntimeException("app不存在");
			pst.close();

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/getwxacodeunlimit").append("?")
					.append("access_token=").append(accessToken).toString();
			JSONObject od = new JSONObject();
			od.put("scene", 1);
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), od.toJSONString())).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();

			// 返回结果
			response.setCharacterEncoding(SysConstant.SYS_CHARSET);
			response.setStatus(200);
			IOUtils.copy(okHttpResponse.body().byteStream(), response.getOutputStream());
			okHttpResponse.close();
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
