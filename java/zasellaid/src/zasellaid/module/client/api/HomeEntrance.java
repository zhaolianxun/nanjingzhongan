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
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.HomeEntrance")
@RequestMapping(value = "/c/homeentrance")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
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

			connection = ZasellaidDataSource.dataSource.getConnection();

			// 查詢订单列表
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			if (keyw != null) {
				keyw = new StringBuilder("%").append(keyw).append("%").toString();
				sqlParams.add(keyw);
				sqlParams.add(keyw);
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
			String sql = new StringBuilder(
					"select t.id hospital_id,t.name hospital_name,t.province_name,t.city_name,t.customer_type,t.last_trace_time from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where t.client_user_del=0 and t.client_user_id=? ")
							.append(keyw == null ? ""
									: " and (t.name like ? or t.dean_name like ? or t.dean_phone like ? or t.director_phone like ?)")
							.append(customerType == null ? "" : " and t.customer_type=? ")
							.append(traceStatus == null ? "" : " and t.trace_status=? ")
							.append(provinceId == null ? "" : " and t.province_id=? ")
							.append(cityId == null ? "" : " and t.city_id=? ")
							.append(traceStarttime == null ? "" : " and t.last_trace_time >= ? ")
							.append(traceEndtime == null ? "" : " and t.last_trace_time <= ? ")
							.append("order by last_trace_time desc limit ?,? ").toString();
			logger.debug(sql);
			pst = connection.prepareStatement(sql);
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
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
					"select count(t.id) total_count,(select count(id) from t_contact_hospital where client_user_del=0 and t.client_user_id=client_user_id) def_count,(select count(id) from t_contact_hospital where client_user_del=0 and customer_type=1 and t.client_user_id=client_user_id) type1_count,(select count(id) from t_contact_hospital where client_user_del=0 and customer_type=2 and t.client_user_id=client_user_id) type2_count,(select count(id) from t_contact_hospital where client_user_del=0 and customer_type=3 and t.client_user_id=client_user_id) type3_count,(select count(id) from t_contact_hospital where client_user_del=0 and customer_type=4 and t.client_user_id=client_user_id) type4_count from t_contact_hospital t left join t_client_user beloner on t.client_user_id=beloner.id where t.client_user_del=0 and t.client_user_id=? ")
							.append(keyw == null ? ""
									: " and (t.name like ? or t.dean_name like ? or t.dean_phone like ? or t.director_phone like ?)")
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

}
