package rrightway.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.constant.OutApis;
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.mall.business.GetLoginStatus;
import rrightway.module.mall.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;
import okhttp3.Request;
import okhttp3.Response;

@Controller("mall.api.IEntrance")
@RequestMapping(value = "/m/e/i")
public class IEntrance {

	public static Logger logger = Logger.getLogger(IEntrance.class);

	@RequestMapping(value = "/bindphone")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String verificationCode = StringUtils.trimToNull(request.getParameter("verification_code"));
			if (verificationCode == null)
				throw new InteractRuntimeException("verification_code不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 校验短信验证码
			String url = new StringBuilder(OutApis.sms_verification_verify).append("?").append("phone=").append(phone)
					.append("&verification_code=").append(verificationCode).toString();
			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getInteger("code") != 0 || resultVo.getJSONObject("data").getInteger("ifSuccess") != 1) {
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));
			}

			connection = RrightwayDataSource.dataSource.getConnection();
			// 检查手机号
			pst = connection.prepareStatement("select id from t_mall_user where phone=? and mall_id=?");
			pst.setObject(1, phone);
			pst.setObject(2, mallId);
			ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new InteractRuntimeException("手机号已存在");
			pst.close();

			// 跟新手机号
			pst = connection.prepareStatement("update t_mall_user set phone=? where id=?");
			pst.setObject(1, phone);
			pst.setObject(2, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			pst.close();

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

}
