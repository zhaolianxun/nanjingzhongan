package zaylt.module.client.module.hospital.api;

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

@Controller("client.hospital.api.HospitalInfoEntrance")
@RequestMapping(value = "/c/h/e/hospitalinfo")
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

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

	@RequestMapping(value = "/alter")
	@Transactional(rollbackFor = Exception.class)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String intro1 = StringUtils.trimToNull(request.getParameter("intro1"));
			String intro2 = StringUtils.trimToNull(request.getParameter("intro2"));
			String intro3 = StringUtils.trimToNull(request.getParameter("intro3"));
			String intro4 = StringUtils.trimToNull(request.getParameter("intro4"));
			String intro5 = StringUtils.trimToNull(request.getParameter("intro5"));
			String introPic1 = StringUtils.trimToNull(request.getParameter("intro_pic1"));
			String introPic2 = StringUtils.trimToNull(request.getParameter("intro_pic2"));
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String address = StringUtils.trimToNull(request.getParameter("address"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

			List sqlParams = new ArrayList();
			if (intro1 != null)
				sqlParams.add(intro1);
			if (intro2 != null)
				sqlParams.add(intro2);
			if (intro3 != null)
				sqlParams.add(intro3);
			if (intro4 != null)
				sqlParams.add(intro4);
			if (intro5 != null)
				sqlParams.add(intro5);
			if (introPic1 != null)
				sqlParams.add(introPic1);
			if (introPic2 != null)
				sqlParams.add(introPic2);
			if (name != null)
				sqlParams.add(name);
			if (headmanName != null)
				sqlParams.add(headmanName);
			if (tel != null)
				sqlParams.add(tel);
			if (address != null)
				sqlParams.add(address);
			if (remark != null)
				sqlParams.add(remark);
			if (!sqlParams.isEmpty()) {
				sqlParams.add(loginStatus.getHospitalId());
				connection = ZayltDataSource.dataSource.getConnection();
				pst = connection.prepareStatement("update t_hospital set id=id" + (intro1 == null ? "" : ",intro1=?")
						+ (intro2 == null ? "" : ",intro2=?") + (intro3 == null ? "" : ",intro3=?")
						+ (intro4 == null ? "" : ",intro4=?") + (intro5 == null ? "" : ",intro5=?")
						+ (introPic1 == null ? "" : ",intro_pic1=?") + (introPic2 == null ? "" : ",intro_pic2=?")
						+ (name == null ? "" : ",name=?") + (headmanName == null ? "" : ",headman_name=?")
						+ (tel == null ? "" : ",tel=?") + (address == null ? "" : ",address=?")
						+ (remark == null ? "" : ",remark=?") + " where id=?");
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