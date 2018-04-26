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

@Controller("controller.client.MaintainEntrance")
@RequestMapping(value = "/c/mt")
public class MaintainEntrance {

	public static Logger logger = Logger.getLogger(MaintainEntrance.class);

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public void users(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
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
			if (loginStatus.getMaintainIs() == 0)
				throw new InteractRuntimeException("您不是运维用户");

			connection = ZasellaidDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(phone);
			if (realname != null)
				sqlParams.add(realname);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select (select max(date) from t_daily_report where user_id=t.id) last_daily_report_time,t.last_alive_time,t.phone,t.realname,t.remark,t.register_time,(select count(id) from t_contact_hospital where client_user_id=t.id) hospital_count,(select count(id) from t_daily_report where user_id=t.id) daily_report_count,(select count(id) from t_trace_record where user_id=t.id) trace_record_count from t_client_user t where 1=1 "
							+ (phone == null ? "" : " and t.phone like '%?%' ")
							+ (realname == null ? "" : " and t.realname like '%?%' ")
							+ " order by t.last_alive_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("lastAliveTime", rs.getObject("last_alive_time"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("remark", rs.getObject("remark"));
				item.put("registerTime", rs.getObject("register_time"));
				item.put("hospitalCount", rs.getObject("hospital_count"));
				item.put("dailyReportCount", rs.getObject("daily_report_count"));
				item.put("traceRecordCount", rs.getObject("trace_record_count"));
				item.put("lastDailyReportTime", rs.getObject("last_daily_report_time"));
				items.add(item);
			}
			pst.close();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject users = new JSONObject();
			users.put("items", items);
			data.put("users", users);
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

	@RequestMapping(value = "/hospitals", method = RequestMethod.POST)
	public void hospitals(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String key = StringUtils.trimToNull(request.getParameter("key"));
			String customerTypeParam = StringUtils.trimToNull(request.getParameter("customer_type"));
			Integer customerType = customerTypeParam == null ? null : Integer.parseInt(customerTypeParam);
			String traceStatusParam = StringUtils.trimToNull(request.getParameter("trace_status"));
			Integer traceStatus = traceStatusParam == null ? null : Integer.parseInt(traceStatusParam);
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
			List sqlParams = new ArrayList();
			if (key != null) {
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
				sqlParams.add(key);
			}
			if (customerType != null)
				sqlParams.add(customerType);
			if (traceStatus != null)
				sqlParams.add(traceStatus);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select insert(u.phone, 4, 4, '****') belonger_phone,u.realname belonger_realname,t.name,insert(t.dean_phone, 4, 4, '****') dean_phone,t.dean_name,insert(t.hospital_tel, 4, 4, '****') hospital_tel,t.department_name,insert(t.director_phone, 4, 4, '****') director_phone,t.province_name,t.city_name,t.customer_type,t.trace_status,t.extra_infos,t.add_time from t_contact_hospital t left join t_client_user u on t.client_user_id=u.id where 1=1 "
							+ (key == null ? ""
									: " and (t.name like '%?%' or t.dean_phone like '%?%' or t.dean_name like '%?%' or t.hospital_tel like '%?%' or t.department_name like '%?%' or t.director_phone like '%?%' or t.province_name like '%?%' or t.city_name like '%?%' or t.extra_infos like '%?%') ")
							+ (customerType == null ? "" : " and customer_type=? ")
							+ (traceStatus == null ? "" : " and trace_status=? ")
							+ " order by t.add_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("belongerPhone", rs.getObject("belonger_phone"));
				item.put("belongerRealname", rs.getObject("belonger_realname"));
				item.put("name", rs.getObject("name"));
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
				items.add(item);
			}
			pst.close();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject hospitals = new JSONObject();
			hospitals.put("items", items);
			data.put("hospitals", hospitals);
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
