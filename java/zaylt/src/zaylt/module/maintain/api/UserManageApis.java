package zaylt.module.maintain.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zaylt.entity.InteractRuntimeException;
import zaylt.module.maintain.business.LoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("maintain.api.UserManage")
@RequestMapping(value = "/mm/um")
public class UserManageApis {

	public static Logger logger = Logger.getLogger(UserManageApis.class);

	@RequestMapping(value = "/userlist")
	public void userList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String type = StringUtils.trimToNull(request.getParameter("type"));
			String clinicIdParam = StringUtils.trimToNull(request.getParameter("clinic_id"));
			Integer clinicId = clinicIdParam == null ? null : Integer.parseInt(clinicIdParam);
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			Integer hospitalId = hospitalIdParam == null ? null : Integer.parseInt(hospitalIdParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			if (type != null)
				sqlParams.add(type);
			if (clinicId != null)
				sqlParams.add(clinicId);
			if (hospitalId != null)
				sqlParams.add(hospitalId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					new StringBuilder("select u.id,u.phone,u.realname,u.type from t_user u where 1=1 ")
							.append(phone == null ? "" : " and u.phone like ?")
							.append(type == null ? "" : " and u.type like ?")
							.append(clinicId == null ? "" : " and u.clinic_id = ?")
							.append(hospitalId == null ? "" : " and u.hospital_id = ?")
							.append(" order by u.register_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("type", rs.getObject("type"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/udetail")
	public void userDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String id = StringUtils.trimToNull(request.getParameter("id"));
			if (id == null)
				throw new InteractRuntimeException("id 不能空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select u.power_lookallhospitals,u.power_addhospital,u.id,u.phone,u.realname,u.type from t_user u where u.id=? ")
							.toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("id", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("type", rs.getObject("type"));
				item.put("powerAddhospital", rs.getObject("power_addhospital"));
				item.put("powerLookallhospitals", rs.getObject("power_lookallhospitals"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			zaylt.business.LoginStatus.loginRefresh(id);
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

	@RequestMapping(value = "/ualter")
	public void userAlter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String id = StringUtils.trimToNull(request.getParameter("id"));
			if (id == null)
				throw new InteractRuntimeException("id 不能空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			String powerAddhospitalParam = StringUtils.trimToNull(request.getParameter("power_addhospital"));
			Integer powerAddhospital = powerAddhospitalParam == null ? null : Integer.parseInt(powerAddhospitalParam);
			if (powerAddhospital != null) {
				if (powerAddhospital > 0)
					powerAddhospital = 1;
				else
					powerAddhospital = 0;
			}

			String powerLookallhospitalsParam = StringUtils.trimToNull(request.getParameter("power_lookallhospitals"));
			Integer powerLookallhospitals = powerLookallhospitalsParam == null ? null
					: Integer.parseInt(powerLookallhospitalsParam);
			if (powerLookallhospitals != null) {
				if (powerLookallhospitals > 0)
					powerLookallhospitals = 1;
				else
					powerLookallhospitals = 0;
			}
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(phone);
			if (realname != null)
				sqlParams.add(realname);
			if (powerAddhospital != null)
				sqlParams.add(powerAddhospital);
			if (powerLookallhospitals != null)
				sqlParams.add(powerLookallhospitals);

			sqlParams.add(id);
			pst = connection.prepareStatement(new StringBuilder("update t_user set id=id")
					.append(phone == null ? "" : ",phone=?").append(realname == null ? "" : ",realname=?")
					.append(powerAddhospital == null ? "" : ",power_addhospital=?")
					.append(powerLookallhospitals == null ? "" : ",power_lookallhospitals=?").append(" where id=?")
					.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/hospitaldetailbyname")
	public void hospitalDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不能空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.tel,t.id,t.headman_name,t.cover,t.address,t.province_name,t.city_name,t.district_name from t_hospital t where t.name=? ")
							.toString());
			pst.setObject(1, name);
			ResultSet rs = pst.executeQuery();
			JSONObject detail = new JSONObject();
			if (rs.next()) {
				detail.put("id", rs.getObject("id"));
				detail.put("tel", rs.getObject("tel"));
				detail.put("headmanName", rs.getObject("headman_name"));
				detail.put("cover", rs.getObject("cover"));
				detail.put("address", rs.getObject("address"));
				detail.put("provinceName", rs.getObject("province_name"));
				detail.put("cityName", rs.getObject("city_name"));
				detail.put("districtName", rs.getObject("district_name"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(detail);
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

	@RequestMapping(value = "/bindhospital")
	public void bindHospital(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select developer_id from t_hospital t where t.id=? for update").toString());
			pst.setObject(1, hospitalId);
			ResultSet rs = pst.executeQuery();
			String boundUserId = null;
			if (rs.next()) {
				boundUserId = rs.getString("developer_id");
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			if (boundUserId != null)
				throw new InteractRuntimeException("该医院已绑定了其他用户");

			pst = connection.prepareStatement(
					new StringBuilder("select type from t_user t where t.id=? for update").toString());
			pst.setObject(1, userId);
			rs = pst.executeQuery();
			String type = null;
			if (rs.next()) {
				type = rs.getString("type");
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			if (!type.equals("3"))
				throw new InteractRuntimeException("该用户不是开发者用户");

			pst = connection
					.prepareStatement(new StringBuilder("update t_hospital set developer_id=? where id=?").toString());
			pst.setObject(1, userId);
			pst.setObject(2, hospitalId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
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