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

	@RequestMapping(value = "/wgw")
	@Transactional(rollbackFor = Exception.class)
	public void weiguanwang(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.longitude,t.latitude,t.province_name,t.city_name,t.district_name,t.tel,t.name from t_hospital t where t.id=?");
			pst.setObject(1, loginStatus.getHospitalId());
			ResultSet rs = pst.executeQuery();
			JSONObject info = new JSONObject();
			if (rs.next()) {
				info.put("provinceName", rs.getObject("province_name"));
				info.put("cityName", rs.getObject("city_name"));
				info.put("districtName", rs.getObject("district_name"));
				info.put("tel", rs.getObject("tel"));
				info.put("name", rs.getObject("name"));
				info.put("longitude", rs.getObject("longitude"));
				info.put("latitude", rs.getObject("latitude"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			pst = connection.prepareStatement(
					"select t.id,t.name,t.headimg,t.office,t.job_titles,left(t.good_at, 50) good_at_brief from t_doctor t where t.hospital_id=? order by t.name asc limit 0,10");
			pst.setObject(1, loginStatus.getHospitalId());
			rs = pst.executeQuery();
			JSONArray doctors = new JSONArray();
			while (rs.next()) {
				JSONObject doctor = new JSONObject();
				doctor.put("id", rs.getObject("id"));
				doctor.put("name", rs.getObject("name"));
				doctor.put("headimg", rs.getObject("headimg"));
				doctor.put("office", rs.getObject("office"));
				doctor.put("jobTitles", rs.getObject("job_titles"));
				doctor.put("goodAtBrief", rs.getObject("good_at_brief"));
				doctors.add(doctor);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(info);
			data.put("doctors", doctors);
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
	
	@RequestMapping(value = "/doctorlist")
	public void doctorList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
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
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getHospitalId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select h.name hoapital_name,t.id,t.name,t.headimg,t.office,t.job_titles,left(t.good_at, 50) good_at_brief from t_doctor t left join t_hospital h on t.hospital_id=h.id where t.hospital_id=? order by t.name asc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("headimg", rs.getObject("headimg"));
				item.put("office", rs.getObject("office"));
				item.put("jobTitles", rs.getObject("job_titles"));
				item.put("goodAtBrief", rs.getObject("good_at_brief"));
				item.put("hoapitalName", rs.getObject("hoapital_name"));
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

	@RequestMapping(value = "/doctorinfo")
	public void doctorInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");
			connection = ZayltDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					"select h.name hoapital_name,t.id,t.name,t.headimg,t.office,t.job_titles,t.good_at,t.intro from t_doctor t left join t_hospital h on t.hospital_id=h.id where t.id=?");
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("headimg", rs.getObject("headimg"));
				item.put("office", rs.getObject("office"));
				item.put("jobTitles", rs.getObject("job_titles"));
				item.put("goodAt", rs.getObject("good_at"));
				item.put("intro", rs.getObject("intro"));
				item.put("hoapitalName", rs.getObject("hoapital_name"));
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