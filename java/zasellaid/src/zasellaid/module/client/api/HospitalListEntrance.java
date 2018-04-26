package zasellaid.module.client.api;

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
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zasellaid.entity.InteractRuntimeException;
import zasellaid.module.client.business.GetLoginStatus;
import zasellaid.module.client.entity.UserLoginStatus;
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.HospitalListEntrance")
@RequestMapping(value = "/c/hle")
public class HospitalListEntrance {

	public static Logger logger = Logger.getLogger(HospitalListEntrance.class);

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void hospitals1(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// 获取并处理参数
			String keyw = StringUtils.trimToNull(request.getParameter("keyw"));
			String traceStarttimeParam = StringUtils.trimToNull(request.getParameter("trace_starttime"));
			Long traceStarttime = traceStarttimeParam == null ? null : Long.parseLong(traceStarttimeParam);
			String traceEndtimeParam = StringUtils.trimToNull(request.getParameter("trace_endtime"));
			Long traceEndtime = traceEndtimeParam == null ? null : Long.parseLong(traceEndtimeParam);
			String customerTypeParam = StringUtils.trimToNull(request.getParameter("customer_type"));
			Integer customerType = customerTypeParam == null ? null : Integer.parseInt(customerTypeParam);
			String traceStatusParam = StringUtils.trimToNull(request.getParameter("trace_status"));
			Integer traceStatus = traceStatusParam == null ? null : Integer.parseInt(traceStatusParam);
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			Integer provinceId = provinceIdParam == null ? null : Integer.parseInt(provinceIdParam);
			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			Integer cityId = cityIdParam == null ? null : Integer.parseInt(cityIdParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 30;
			if (pageSize > 300)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() == 0)
				throw new InteractRuntimeException("您不是超级管理员");

			connection = ZasellaidDataSource.dataSource.getConnection();

			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (keyw != null) {
				keyw = new StringBuilder("'%").append(keyw).append("%'").toString();
				sqlParams.add(keyw);
				sqlParams.add(keyw);
			}
			if (customerType != null)
				sqlParams.add(customerType);
			if (traceStatus != null)
				sqlParams.add(traceStatus);
			if (provinceId != null)
				sqlParams.add(provinceId);
			if (cityId != null)
				sqlParams.add(cityId);
			if (traceStarttime != null)
				sqlParams.add(traceStarttime);
			if (traceEndtime != null)
				sqlParams.add(traceEndtime);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id hospital_id,beloner.realname belonger_realname,insert(beloner.phone, 4, 4, '****') belonger_phone,t.name hospital_name,t.province_name,t.city_name,t.customer_type from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where 1=1 ")
							.append(keyw == null ? "" : " and t.name like ? ")
							.append(keyw == null ? "" : " and t.dean_name like ? ")
							.append(customerType == null ? "" : " and t.customer_type=? ")
							.append(traceStatus == null ? "" : " and t.trace_status=? ")
							.append(provinceId == null ? "" : " and t.province_id=? ")
							.append(cityId == null ? "" : " and t.city_id=? ")
							.append(traceStarttime == null ? "" : " and t.last_trace_time >= ? ")
							.append(traceEndtime == null ? "" : " and t.last_trace_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("belongerRealname", rs.getObject("belonger_realname"));
				item.put("belongerPhone", rs.getObject("belonger_phone"));
				item.put("hospitalName", rs.getObject("hospital_name"));
				item.put("provinceName", rs.getObject("province_name"));
				item.put("cityName", rs.getObject("city_name"));
				item.put("hospitalId", rs.getObject("hospital_id"));
				item.put("customerType", rs.getObject("customer_type"));
				items.add(item);
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"select count(t.id) total_count,(select count(id) from t_contact_hospital) def_count,(select count(id) from t_contact_hospital where customer_type=1) type1_count,(select count(id) from t_contact_hospital where customer_type=2) type2_count,(select count(id) from t_contact_hospital where customer_type=3) type3_count,(select count(id) from t_contact_hospital where customer_type=4) type4_count from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where 1=1 ")
							.append(customerType == null ? "" : " and t.customer_type=? ")
							.append(traceStatus == null ? "" : " and t.trace_status=? ")
							.append(provinceId == null ? "" : " and t.province_id=? ")
							.append(cityId == null ? "" : " and t.city_id=? ")
							.append(traceStarttime == null ? "" : " and t.last_trace_time >= ? ")
							.append(traceEndtime == null ? "" : " and t.last_trace_time <= ? ").append(" limit ?,? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			rs = pst.executeQuery();
			int totalCount = 0;
			int defCount = 0;
			int juniorCount = 0;
			int tracingCount = 0;
			int focusCount = 0;
			int signedCount = 0;
			if (rs.next()) {
				totalCount = rs.getInt("total_count");
				defCount = rs.getInt("def_count");
				juniorCount = rs.getInt("type1_count");
				tracingCount = rs.getInt("type2_count");
				focusCount = rs.getInt("type3_count");
				signedCount = rs.getInt("type4_count");
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject hospitals = new JSONObject();
			hospitals.put("items", items);
			hospitals.put("totalCount", totalCount);
			data.put("hospitals", hospitals);
			data.put("defCount", defCount);
			data.put("juniorCount", juniorCount);
			data.put("tracingCount", tracingCount);
			data.put("focusCount", focusCount);
			data.put("signedCount", signedCount);
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

	@RequestMapping(value = "/hospitalinfo", method = RequestMethod.POST)
	public void hospitalInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			// 获取并处理参数
			String hospitalId = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalId == null)
				throw new InteractRuntimeException("hospital_id can`t be null.");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() == 0)
				throw new InteractRuntimeException("您不是超级管理员");

			connection = ZasellaidDataSource.dataSource.getConnection();

			// 查詢订单列表
			// 查詢主轮播图
			pst = connection.prepareStatement(new StringBuilder(
					"select beloner.realname belonger_realname,insert(beloner.phone, 4, 4, '****') belonger_phone,t.name hospital_name,insert(t.dean_phone, 4, 4, '****') dean_phone,t.dean_name,insert(t.hospital_tel, 4, 4, '****') hospital_tel,t.department_name,insert(t.director_phone, 4, 4, '****') director_phone,t.province_name,t.city_name,t.customer_type,t.trace_status,t.extra_infos,t.add_time from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where t.id=?")
							.toString());
			pst.setObject(1, hospitalId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("belongerRealname", rs.getObject("belonger_realname"));
				item.put("belongerPhone", rs.getObject("belonger_phone"));
				item.put("hospitalName", rs.getObject("hospital_name"));
				item.put("deanPhone", rs.getObject("dean_phone"));
				item.put("deanName", rs.getObject("dean_name"));
				item.put("hospitalTel", rs.getObject("hospital_tel"));
				item.put("departmentName", rs.getObject("department_name"));
				item.put("directorPhone", rs.getObject("director_phone"));
				item.put("provinceName", rs.getObject("province_name"));
				item.put("cityName", rs.getObject("city_name"));
				item.put("customerType", rs.getObject("customer_type"));
				item.put("traceStatus", rs.getObject("trace_status"));
				item.put("extraInfos", rs.getObject("extra_infos"));
				item.put("addTime", rs.getObject("add_time"));
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

	@RequestMapping(value = "/tracerecords")
	public void traceRecords(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalId = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalId == null)
				throw new InteractRuntimeException("hospital_id 不能为空");
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0 || pageSize > 300)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() == 0)
				throw new InteractRuntimeException("您不是超级管理员");

			connection = ZasellaidDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select voice_duration,type,content,trace_time from t_trace_record t where t.contact_hospital_id=? order by t.trace_time desc limit ?,?");
			pst.setObject(1, hospitalId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("voiceDuration", rs.getObject("voice_duration"));
				item.put("type", rs.getObject("type"));
				item.put("content", rs.getObject("content"));
				item.put("traceTime", rs.getString("trace_time"));
				items.add(item);
			}
			pst.close();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("traceRecords", items);
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
