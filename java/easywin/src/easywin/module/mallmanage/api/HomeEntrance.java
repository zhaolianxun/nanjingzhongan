package easywin.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.HomeEntrance")
@RequestMapping(value = "/mm/{mallId}/e/home")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "home")
	public void goodDetailEntrance(@PathVariable("mallId") String mallId, HttpServletRequest request,
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
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select t.name,u.nick_name,u.wx_mchid,u.wx_mchkey,u.wx_mchcertpath from t_mall t left join t_app u on t.id=u.id where t.id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("mallName", rs.getObject("name"));
				item.put("fromWxminiappName", rs.getObject("nick_name"));
				String wxMchid = rs.getString("wx_mchid");
				item.put("wxMchidFill", wxMchid == null || wxMchid.isEmpty()?0:1);
				String wxMchkey = rs.getString("wx_mchkey");
				item.put("wxMchkeyFill", wxMchkey == null || wxMchkey.isEmpty()?0:1);
				String wxMchcertpath = rs.getString("wx_mchcertpath");
				item.put("wxMchcertpathFill", wxMchcertpath == null || wxMchcertpath.isEmpty()?0:1);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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
