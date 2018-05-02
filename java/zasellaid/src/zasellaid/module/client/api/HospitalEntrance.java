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
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.HospitalEntrance")
@RequestMapping(value = "/c/hent")
public class HospitalEntrance {

	public static Logger logger = Logger.getLogger(HospitalEntrance.class);

	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public void add(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// TODO 获取请求参数
			String hospitalIdsParam = StringUtils.trimToNull(request.getParameter("hospital_ids"));
			if (hospitalIdsParam == null)
				throw new InteractRuntimeException("hospital_ids can`t be empty.");
			String[] hospitalIds = hospitalIdsParam.split(",");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String s = "";
			for (int i = 0; i < hospitalIds.length; i++) {
				s = s + ",?";
			}
			s = s.substring(1);
			List sqlParams = new ArrayList();
			sqlParams.add(new Date().getTime());
			for (int i = 0; i < hospitalIds.length; i++) {
				sqlParams.add(hospitalIds[i]);
			}
			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"update t_contact_hospital set client_user_del=1,client_user_time=? where id in (" + s + ")");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			pst.executeUpdate();
			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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

	@RequestMapping(value = "/recycle/restore", method = RequestMethod.POST)
	public void restore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// TODO 获取请求参数
			String hospitalIdsParam = StringUtils.trimToNull(request.getParameter("hospital_ids"));
			if (hospitalIdsParam == null)
				throw new InteractRuntimeException("hospital_ids can`t be empty.");
			String[] hospitalIds = hospitalIdsParam.split(",");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String s = "";
			for (int i = 0; i < hospitalIds.length; i++) {
				s = s + ",?";
			}
			s = s.substring(1);

			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"update t_contact_hospital set client_user_del=0,client_user_time=null where id in (" + s + ")");
			for (int i = 0; i < hospitalIds.length; i++) {
				pst.setString(i + 1, hospitalIds[i]);
			}
			pst.executeUpdate();
			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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

	@RequestMapping(value = "/recycle/hospitals", method = RequestMethod.POST)
	public void userHome(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取并处理参数
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
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id hospital_id,t.name hospital_name,t.province_name,t.city_name,t.customer_type,t.last_trace_time from t_contact_hospital t  where t.client_user_del=1 and t.client_user_id=? order by client_user_time desc")
							.append(" limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("provinceName", rs.getObject("province_name"));
				item.put("cityName", rs.getObject("city_name"));
				item.put("hospitalId", rs.getObject("hospital_id"));
				item.put("hospitalName", rs.getObject("hospital_name"));
				item.put("customerType", rs.getObject("customer_type"));
				item.put("lastTraceTime", rs.getObject("last_trace_time"));
				items.add(item);
			}
			pst.close();

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
