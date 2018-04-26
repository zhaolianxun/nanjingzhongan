package zasellaid.module.client.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.UserListEntrance")
@RequestMapping(value = "/c/ule")
public class UserListEntrance {

	public static Logger logger = Logger.getLogger(UserListEntrance.class);

	@RequestMapping(value = "/userinfopage", method = RequestMethod.POST)
	public void userinfopage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取并处理参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");
			connection = ZasellaidDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					"select t.phone,t.realname,t.remark,t.last_alive_time,t.register_time,(select max(add_time) from t_daily_report where user_id=t.id) last_report_time,(select count(id) from t_trace_record where user_id=t.id) trace_count,(select count(id) from t_contact_hospital where client_user_id=t.id) hospital_count from t_client_user t where t.id=?");
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("remark", rs.getObject("remark"));
				item.put("lastAliveTime", rs.getObject("last_alive_time"));
				item.put("registerTime", rs.getObject("register_time"));
				item.put("lastReportTime", rs.getObject("last_report_time"));
				item.put("traceCount", rs.getObject("trace_count"));
				item.put("hospitalCount", rs.getObject("hospital_count"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("userInfo", item);
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

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {

			// TODO 获取请求参数
			String keyw = StringUtils.trimToNull(request.getParameter("keyw"));
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 30;

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

			connection = ZasellaidDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (keyw != null) {
				keyw = new StringBuilder("%").append(keyw).append("%").toString();
				sqlParams.add(keyw);
				sqlParams.add(keyw);
			}
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select admin_look_report_today,id,realname,insert(phone, 4, 4, '****') phone,(select count(id) from t_trace_record where user_id=t.id and from_unixtime(trace_time/1000,'%Y-%m-%d')=DATE_SUB(curdate(),INTERVAL 0 DAY)) trace_count_today,(select count(id) from t_trace_record where user_id=t.id and from_unixtime(trace_time/1000,'%Y-%m-%d')=DATE_SUB(curdate(),INTERVAL 1 DAY)) trace_count_yesterday,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=1) junior_customer_count,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=2) tracing_customer_count,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=3) vital_customer_count,(select if(count(id)>0,1,0) from t_daily_report where user_id=t.id and date=curdate()) daily_report_submit_is,(select if(superior_review is not null and superior_review != '',1,0) from t_daily_report where user_id=t.id and date=curdate()) daily_report_review_is,t.if_admin from t_client_user t where 1=1 "
							+ (keyw == null ? "" : " and (phone like ? or realname like ?)")
							+ "  order by t.register_time desc limit ?,? ");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("adminLookReportToday", rs.getObject("admin_look_report_today"));
				item.put("userId", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("phone", rs.getObject("phone"));
				item.put("traceCountToday", rs.getObject("trace_count_today"));
				item.put("traceCountYesterday", rs.getObject("trace_count_yesterday"));
				item.put("juniorCustomerCount", rs.getObject("junior_customer_count"));
				item.put("tracingCustomerCount", rs.getObject("tracing_customer_count"));
				item.put("vitalCustomerCount", rs.getObject("vital_customer_count"));
				item.put("dailyReportSubmitIs", rs.getObject("daily_report_submit_is"));
				item.put("dailyReportReviewIs", rs.getObject("daily_report_review_is"));
				item.put("ifAdmin", rs.getObject("if_admin"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject users = new JSONObject();
			users.put("items", items);
			data.put("users", users);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
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

	@RequestMapping(value = "/userdailyreports")
	public void userDailyReports(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");
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
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

			connection = ZasellaidDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,yesterday_summary,today_goal,date,superior_review from t_daily_report where user_id=? order by add_time desc limit ?,?");
			pst.setObject(1, userId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("dailyReportId", rs.getObject("id"));
				item.put("yesterdaySummary", rs.getObject("yesterday_summary"));
				item.put("todayGoal", rs.getObject("today_goal"));
				item.put("superiorReview", rs.getObject("superior_review"));
				item.put("date", rs.getString("date"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("reports", items);
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

	@RequestMapping(value = "/userhome", method = RequestMethod.POST)
	public void userHome(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取并处理参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");
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
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

			connection = ZasellaidDataSource.dataSource.getConnection();

			// 查詢订单列表
			List sqlParams = new ArrayList();
			sqlParams.add(userId);
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
					"select t.id hospital_id,insert(beloner.phone, 4, 4, '****') belonger_phone,t.name hospital_name,t.province_name,t.city_name,t.customer_type,t.last_trace_time from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where 1=1 and t.client_user_id=? ")
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
				item.put("belongerPhone", rs.getObject("belonger_phone"));
				item.put("hospitalName", rs.getObject("hospital_name"));
				item.put("provinceName", rs.getObject("province_name"));
				item.put("cityName", rs.getObject("city_name"));
				item.put("hospitalId", rs.getObject("hospital_id"));
				item.put("customerType", rs.getObject("customer_type"));
				item.put("lastTraceTime", rs.getObject("last_trace_time"));
				items.add(item);
			}
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"select count(t.id) total_count,(select count(id) from t_contact_hospital where t.client_user_id=client_user_id) def_count,(select count(id) from t_contact_hospital where customer_type=1 and t.client_user_id=client_user_id) type1_count,(select count(id) from t_contact_hospital where customer_type=2 and t.client_user_id=client_user_id) type2_count,(select count(id) from t_contact_hospital where customer_type=3 and t.client_user_id=client_user_id) type3_count,(select count(id) from t_contact_hospital where customer_type=4 and t.client_user_id=client_user_id) type4_count from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where 1=1 and t.client_user_id=? ")
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

	@RequestMapping(value = "/usertracerecords")
	public void userTraceRecords(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

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

	@RequestMapping(value = "/userhospitalinfo", method = RequestMethod.POST)
	public void userHospitalInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select beloner.realname belonger_realname,insert(beloner.phone, 4, 4, '****') belonger_phone,t.name hospital_name,t.dean_phone,t.dean_name,t.hospital_tel,t.department_name,t.director_phone,t.province_name,t.city_name,t.customer_type,t.trace_status,t.extra_infos,t.add_time from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where t.id=?")
							.toString());
			pst.setObject(1, contactHospitalId);
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
			// TODO 处理异常
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

	@RequestMapping(value = "/lookreport")
	public void lookReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reportIdParam = StringUtils.trimToNull(request.getParameter("report_id"));
			if (reportIdParam == null)
				throw new InteractRuntimeException("report_id 不能为空");
			int reportId = Integer.parseInt(reportIdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1 && loginStatus.getMaintainIs() != 1)
				throw new InteractRuntimeException("您不是超级管理员或运维");

			connection = ZasellaidDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢订单列表
			if (loginStatus.getIfSuperadmin() != 1) {
				pst = connection
						.prepareStatement("delete from t_daily_report_adminlook where report_id=? and admin_id=?");
				pst.setObject(1, reportId);
				pst.setObject(2, loginStatus.getUserId());
				int n = pst.executeUpdate();
				pst.close();

				pst = connection.prepareStatement(
						"insert into t_daily_report_adminlook (admin_id,admin_realname,report_id,look_time,report_writer_name) values(?,?,?,?,(select aa.realname from t_daily_report a left join t_client_user aa on a.user_id=aa.id where a.id=?))");
				pst.setObject(1, loginStatus.getUserId());
				pst.setObject(2, loginStatus.getRealname());
				pst.setObject(3, reportId);
				pst.setObject(4, new Date().getTime());
				pst.setObject(5, reportId);
				n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");

				// 跟新自己今日查看记录
				pst = connection.prepareStatement(
						"update t_client_user t set t.admin_look_report_today=(select GROUP_CONCAT(tttt.report_writer_name) from (select distinct ttt.report_writer_name from t_daily_report_adminlook ttt where ttt.admin_id=? and from_unixtime(ttt.look_time/1000,'%Y%m%d')=from_unixtime(rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')/1000,'%Y%m%d')) tttt) where id=?");
				pst.setObject(1, loginStatus.getUserId());
				pst.setObject(2, loginStatus.getUserId());
				n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			}

			pst = connection.prepareStatement(
					"select distinct admin_realname from t_daily_report_adminlook where report_id=? order by look_time desc");
			pst.setObject(1, reportId);
			ResultSet rs = pst.executeQuery();
			JSONArray adminLookerNames = new JSONArray();
			while (rs.next()) {
				adminLookerNames.add(rs.getObject("admin_realname"));
			}
			connection.commit();
			JSONObject data = new JSONObject();
			data.put("adminLookerNames", adminLookerNames);
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, data);
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
