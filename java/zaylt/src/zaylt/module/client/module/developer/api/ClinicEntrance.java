package zaylt.module.client.module.developer.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Statement;

import zaylt.entity.InteractRuntimeException;
import zaylt.module.client.business.GetLoginStatus;
import zaylt.module.client.entity.UserLoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("client.develop.api.ClinicEntrance")
@RequestMapping(value = "/c/d/e/clinic")
public class ClinicEntrance {

	public static Logger logger = Logger.getLogger(ClinicEntrance.class);

	@RequestMapping(value = "/clinics")
	@Transactional(rollbackFor = Exception.class)
	public void clinics(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select (select count(id) from t_patient where clinic_id=t.id) patient_count,t.id,t.name,t.headman_name,t.contact_tel,t.address,t.remark from t_clinic t  order by t.name asc limit ?,?");
			pst.setObject(1, pageSize * (pageNo - 1));
			pst.setObject(2, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("clinicId", rs.getObject("id"));
				item.put("address", rs.getObject("address"));
				item.put("remark", rs.getObject("remark"));
				item.put("contactTel", rs.getObject("contact_tel"));
				item.put("name", rs.getObject("name"));
				item.put("headmanName", rs.getObject("headman_name"));
				item.put("patientCount", rs.getObject("patient_count"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject clinics = new JSONObject();
			clinics.put("items", items);
			data.put("clinics", clinics);
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

	@RequestMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public void add(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不能为空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不能为空");
			String clinicName = StringUtils.trimToNull(request.getParameter("clinic_name"));
			if (clinicName == null)
				throw new InteractRuntimeException("clinic_name 不能为空");
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			if (headmanName == null)
				throw new InteractRuntimeException("headman_name 不能为空");
			String contactTel = StringUtils.trimToNull(request.getParameter("contact_tel"));
			if (contactTel == null)
				throw new InteractRuntimeException("contact_tel 不能为空");
			String address = StringUtils.trimToNull(request.getParameter("address"));
			if (address == null)
				throw new InteractRuntimeException("address 不能为空");
			String remark = StringUtils.trimToNull(request.getParameter("remark"));
			if (remark == null)
				throw new InteractRuntimeException("remark 不能为空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select id from t_user t  where phone=?");
			pst.setObject(1, phone);
			ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new InteractRuntimeException("手机号已存在");

			// 查詢订单列表
			String userId = RandomStringUtils.randomNumeric(12);
			pst = connection.prepareStatement(
					"insert into t_clinic (hospital_id,user_id,name,headman_name,contact_tel,address,remark) values(?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, hospitalId);
			pst.setObject(2, userId);
			pst.setObject(3, clinicName);
			pst.setObject(4, headmanName);
			pst.setObject(5, contactTel);
			pst.setObject(6, address);
			pst.setObject(7, remark);
			int n = pst.executeUpdate();
			int clinicId = 0;
			if (n != 1) {
				pst.close();
				throw new InteractRuntimeException("操作失败");
			} else {
				rs = pst.getGeneratedKeys();
				rs.next();
				clinicId = rs.getInt(1);
				pst.close();
			}

			pst = connection.prepareStatement(
					"insert into t_user (id,phone,password,password_md5,register_time,type,hospital_id,clinic_id) values(?,?,?,?,?,?,?,?)");
			pst.setObject(1, userId);
			pst.setObject(2, phone);
			pst.setObject(3, pwd);
			pst.setObject(4, DigestUtils.md5Hex(pwd));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, "2");
			pst.setObject(7, hospitalId);
			pst.setObject(8, clinicId);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("clinicId", clinicId);
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

	@RequestMapping(value = "/add/hospitals")
	@Transactional(rollbackFor = Exception.class)
	public void hospitals(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement("select t.id,t.name from t_hospital t order by t.name asc limit ?,?");
			pst.setObject(1, pageSize * (pageNo - 1));
			pst.setObject(2, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("hospitalId", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
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

	@RequestMapping(value = "/hospitalinfo")
	@Transactional(rollbackFor = Exception.class)
	public void hospitalInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String clinicIdParam = StringUtils.trimToNull(request.getParameter("clinic_id"));
			if (clinicIdParam == null)
				throw new InteractRuntimeException("clinic_id 不能空");
			int clinicId = Integer.parseInt(clinicIdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.intro1,t.intro2,t.intro3,t.intro4,t.intro5,t.intro_pic1,t.intro_pic2,t.tel,t.name,t.headman_name,t.address,t.remark from t_clinic u inner join t_hospital t on u.hospital_id=t.id where u.id=?");
			pst.setObject(1, clinicId);
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

	@RequestMapping(value = "/patients")
	@Transactional(rollbackFor = Exception.class)
	public void patients(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String clinicIdParam = StringUtils.trimToNull(request.getParameter("clinic_id"));
			if (clinicIdParam == null)
				throw new InteractRuntimeException("clinic_id 不能空");
			int clinicId = Integer.parseInt(clinicIdParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.sickness,t.id,t.realname,t.tel,u.name clinic_name from t_patient t left join t_clinic u on t.clinic_id=u.id where t.clinic_id=?  order by t.realname asc limit ?,?");
			pst.setObject(1, clinicId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("patientId", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("tel", rs.getObject("tel"));
				item.put("clinicName", rs.getObject("clinic_name"));
				item.put("sickness", rs.getObject("sickness"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject patients = new JSONObject();
			patients.put("items", items);
			data.put("patients", patients);
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