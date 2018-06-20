package zaylt.module.client.module.clinic.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zaylt.business.LoginStatus;
import zaylt.entity.InteractRuntimeException;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("client.clinic.api.HospitalInfoEntrance")
@RequestMapping(value = "/c/c/e/hospitalinfo")
public class HospitalInfoEntrance {

	public static Logger logger = Logger.getLogger(HospitalInfoEntrance.class);

	@RequestMapping(value = "/info")
	@Transactional(rollbackFor = Exception.class)
	public void info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.intro1,t.intro2,t.intro3,t.intro4,t.intro5,t.intro_pic1,t.intro_pic2,t.tel,t.name,t.headman_name,t.address,t.remark from t_hospital t where t.id=?");
			pst.setObject(1, loginStatus.getHospitalId());
			ResultSet rs = pst.executeQuery();
			JSONObject info = new JSONObject();
			if (rs.next()) {
				info.put("intro1", rs.getObject("intro1"));
				info.put("intro2", rs.getObject("intro2"));
				info.put("intro3", rs.getObject("intro3"));
				info.put("intro4", rs.getObject("intro4"));
				info.put("intro5", rs.getObject("intro5"));
				info.put("introPic1", rs.getObject("intro_pic1"));
				info.put("introPic2", rs.getObject("intro_pic2"));
				info.put("name", rs.getObject("name"));
				info.put("headmanName", rs.getObject("headman_name"));
				info.put("tel", rs.getObject("tel"));
				info.put("address", rs.getObject("address"));
				info.put("remark", rs.getObject("remark"));
			} else
				throw new InteractRuntimeException("医院不存在");

			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(info);
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