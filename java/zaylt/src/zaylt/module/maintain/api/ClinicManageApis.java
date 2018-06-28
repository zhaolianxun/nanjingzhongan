package zaylt.module.maintain.api;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import zaylt.module.maintain.business.LoginStatus;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("maintain.api.ClinicManage")
@RequestMapping(value = "/mm/cm")
public class ClinicManageApis {

	public static Logger logger = Logger.getLogger(ClinicManageApis.class);

	@RequestMapping(value = "/cliniclist")
	public void hospitallist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String contactTel = StringUtils.trimToNull(request.getParameter("contact_tel"));
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			String hospitalName = StringUtils.trimToNull(request.getParameter("hospital_name"));
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
			if (name != null)
				sqlParams.add(new StringBuilder("%").append(name).append("%").toString());
			if (contactTel != null)
				sqlParams.add(new StringBuilder("%").append(contactTel).append("%").toString());
			if (hospitalId != null)
				sqlParams.add(hospitalId);
			if (userId != null)
				sqlParams.add(userId);
			if (hospitalName != null)
				sqlParams.add(new StringBuilder("%").append(hospitalName).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.name,t.contact_tel,t.id,h.name hospital_name,t.headman_name from t_clinic t left join t_hospital h on t.hospital_id=h.id where 1=1 ")
							.append(name == null ? "" : " and t.name like ?")
							.append(contactTel == null ? "" : " and t.contact_tel like ?")
							.append(hospitalId == null ? "" : " and t.hospital_id = ?")
							.append(userId == null ? "" : " and t.user_id = ?")
							.append(hospitalName == null ? "" : " and h.name like ?")
							.append(" order by t.add_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("contactTel", rs.getObject("contact_tel"));
				item.put("hospitalName", rs.getObject("hospital_name"));
				item.put("headmanName", rs.getObject("headman_name"));
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

	@RequestMapping(value = "/clinicdetail")
	public void clinicDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			connection = ZayltDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.name,t.contact_tel,t.id,h.name hospital_name,t.headman_name,t.featured_project from t_clinic t left join t_hospital h on t.hospital_id=h.id where t.id=?")
							.toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject detail = new JSONObject();
			if (rs.next()) {
				detail.put("id", rs.getObject("id"));
				detail.put("name", rs.getObject("name"));
				detail.put("contactTel", rs.getObject("contact_tel"));
				detail.put("hospitalName", rs.getObject("hospital_name"));
				detail.put("headmanName", rs.getObject("headman_name"));
				detail.put("featuredProject", rs.getObject("featured_project"));
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

	@RequestMapping(value = "/clinicalter")
	public void clinicAlter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String contactTel = StringUtils.trimToNull(request.getParameter("contact_tel"));
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			String featuredProject = StringUtils.trimToNull(request.getParameter("featured_project"));
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(name);
			if (contactTel != null)
				sqlParams.add(contactTel);
			if (headmanName != null)
				sqlParams.add(headmanName);
			if (featuredProject != null)
				sqlParams.add(featuredProject);
			sqlParams.add(id);
			pst = connection.prepareStatement(new StringBuilder("update t_clinic set id=id")
					.append(name == null ? "" : ",name=?").append(contactTel == null ? "" : ",contact_tel=?")
					.append(headmanName == null ? "" : ",headman_name=?")
					.append(featuredProject == null ? "" : ",featured_project=?").append(" where id=?").toString());
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
}