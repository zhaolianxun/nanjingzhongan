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

import zaylt.entity.InteractRuntimeException;
import zaylt.module.client.business.GetLoginStatus;
import zaylt.module.client.entity.UserLoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("client.clinic.api.ClinicInfoEntrance")
@RequestMapping(value = "/c/c/e/clinicinfo")
public class ClinicInfoEntrance {

	public static Logger logger = Logger.getLogger(ClinicInfoEntrance.class);

	@RequestMapping(value = "/info")
	@Transactional(rollbackFor = Exception.class)
	public void info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.name,t.headman_name,t.contact_tel,t.address,t.remark from t_clinic t where t.id=?");
			pst.setObject(1, loginStatus.getClinicId());
			ResultSet rs = pst.executeQuery();
			JSONObject info = new JSONObject();
			if (rs.next()) {
				info.put("name", rs.getObject("name"));
				info.put("headmanName", rs.getObject("headman_name"));
				info.put("contactTel", rs.getObject("contact_tel"));
				info.put("address", rs.getObject("address"));
				info.put("remark", rs.getObject("remark"));
			} else
				throw new InteractRuntimeException("门诊不存在");

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

	@RequestMapping(value = "/alter")
	@Transactional(rollbackFor = Exception.class)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			String contactTel = StringUtils.trimToNull(request.getParameter("contact_tel"));
			String address = StringUtils.trimToNull(request.getParameter("address"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(name);
			if (headmanName != null)
				sqlParams.add(headmanName);
			if (contactTel != null)
				sqlParams.add(contactTel);
			if (address != null)
				sqlParams.add(address);
			if (remark != null)
				sqlParams.add(remark);
			if (!sqlParams.isEmpty()) {
				sqlParams.add(loginStatus.getClinicId());
				connection = ZayltDataSource.dataSource.getConnection();
				pst = connection.prepareStatement("update t_clinic set id=id" + (name == null ? "" : ",name=?")
						+ (headmanName == null ? "" : ",headman_name=?") + (contactTel == null ? "" : ",contact_tel=?")
						+ (address == null ? "" : ",address=?") + (remark == null ? "" : ",remark=?") + " where id=?");
				String id = RandomStringUtils.randomNumeric(12);
				for (int i = 0; i < sqlParams.size(); i++)
					pst.setObject(i + 1, sqlParams.get(i));
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

}