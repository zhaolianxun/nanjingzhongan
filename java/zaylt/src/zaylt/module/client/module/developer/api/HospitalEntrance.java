package zaylt.module.client.module.developer.api;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Statement;

import zaylt.entity.InteractRuntimeException;
import zaylt.module.client.business.GetLoginStatus;
import zaylt.module.client.entity.UserLoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("client.develop.api.HospitalEntrance")
@RequestMapping(value = "/c/d/e/hospital")
public class HospitalEntrance {

	public static Logger logger = Logger.getLogger(HospitalEntrance.class);

	@RequestMapping(value = "/hospitals")
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
			pst = connection.prepareStatement(
					"select u.phone,t.id,t.name,t.headman_name,t.tel,t.cover from t_hospital t inner join t_user u on t.id=u.hospital_id and u.type=1 order by t.name asc limit ?,?");
			pst.setObject(1, pageSize * (pageNo - 1));
			pst.setObject(2, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("hospitalId", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("headmanName", rs.getObject("headman_name"));
				item.put("tel", rs.getObject("tel"));
				item.put("cover", rs.getObject("cover"));
				item.put("phone", rs.getObject("phone"));
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

	@RequestMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public void add(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不能为空");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不能为空");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不能为空");
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			if (headmanName == null)
				throw new InteractRuntimeException("headman_name 不能为空");
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			if (tel == null)
				throw new InteractRuntimeException("tel 不能为空");
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			if (cover == null)
				throw new InteractRuntimeException("cover 不能为空");
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
					"insert into t_hospital (user_id,name,headman_name,tel,cover) values(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, userId);
			pst.setObject(2, name);
			pst.setObject(3, headmanName);
			pst.setObject(4, tel);
			pst.setObject(5, cover);
			int n = pst.executeUpdate();
			int hospitalId = 0;
			if (n != 1) {
				pst.close();
				throw new InteractRuntimeException("操作失败");
			} else {
				rs = pst.getGeneratedKeys();
				rs.next();
				hospitalId = rs.getInt(1);
				pst.close();
			}

			pst = connection.prepareStatement(
					"insert into t_user (id,phone,password,password_md5,register_time,type,hospital_id) values(?,?,?,?,?,?,?)");
			pst.setObject(1, userId);
			pst.setObject(2, phone);
			pst.setObject(3, pwd);
			pst.setObject(4, DigestUtils.md5Hex(pwd));
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, "1");
			pst.setObject(7, hospitalId);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("hospitalId", hospitalId);
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

	@RequestMapping(value = "/alter")
	@Transactional(rollbackFor = Exception.class)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			String intro1 = StringUtils.trimToNull(request.getParameter("intro1"));
			String intro2 = StringUtils.trimToNull(request.getParameter("intro2"));
			String intro3 = StringUtils.trimToNull(request.getParameter("intro3"));
			String intro4 = StringUtils.trimToNull(request.getParameter("intro4"));
			String intro5 = StringUtils.trimToNull(request.getParameter("intro5"));
			String introPic1 = StringUtils.trimToNull(request.getParameter("intro_pic1"));
			String introPic2 = StringUtils.trimToNull(request.getParameter("intro_pic2"));
			String address = StringUtils.trimToNull(request.getParameter("address"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(name);
			if (headmanName != null)
				sqlParams.add(headmanName);
			if (tel != null)
				sqlParams.add(tel);
			if (cover != null)
				sqlParams.add(cover);
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
			if (address != null)
				sqlParams.add(address);
			if (remark != null)
				sqlParams.add(remark);
			if (!sqlParams.isEmpty()) {
				sqlParams.add(hospitalId);
				connection = ZayltDataSource.dataSource.getConnection();
				pst = connection.prepareStatement("update t_hospital set id=id" + (name == null ? "" : ",name=?")
						+ (headmanName == null ? "" : ",headman_name=?") + (tel == null ? "" : ",tel=?")
						+ (cover == null ? "" : ",cover=?") + (intro1 == null ? "" : ",intro1=?")
						+ (intro2 == null ? "" : ",intro2=?") + (intro3 == null ? "" : ",intro3=?")
						+ (intro4 == null ? "" : ",intro4=?") + (intro5 == null ? "" : ",intro5=?")
						+ (introPic1 == null ? "" : ",intro_pic1=?") + (introPic2 == null ? "" : ",intro_pic2=?")
						+ (address == null ? "" : ",address=?") + (remark == null ? "" : ",remark=?") + " where id=?");
				for (int i = 0; i < sqlParams.size(); i++)
					pst.setObject(i + 1, sqlParams.get(i));
				int n = pst.executeUpdate();
				pst.close();
				if (n != 1)
					throw new InteractRuntimeException("操作失败");
			}

			if (phone != null || pwd != null) {
				sqlParams = new ArrayList();
				if (phone != null)
					sqlParams.add(phone);
				if (pwd != null) {
					sqlParams.add(pwd);
					sqlParams.add(DigestUtils.md5Hex(pwd));
				}
				sqlParams.add(hospitalId);
				if (connection == null)
					connection = ZayltDataSource.dataSource.getConnection();
				pst = connection
						.prepareStatement("update t_user t inner join t_hospital u on t.hospital_id=u.id set t.id=t.id"
								+ (phone == null ? "" : ",t.phone=?") + (pwd == null ? "" : ",t.password=?")
								+ (pwd == null ? "" : ",t.password_md5=?") + " where u.id=?");
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

	@RequestMapping(value = "/patients")
	@Transactional(rollbackFor = Exception.class)
	public void patients(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
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
					"select t.status,t.id,t.realname,t.tel,u.name clinic_name from t_patient t left join t_clinic u on t.clinic_id=u.id  where  t.hospital_id=? and t.status in ('3','4') order by t.realname asc limit ?,?");
			pst.setObject(1, hospitalId);
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
				item.put("status", rs.getObject("status"));
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

	@RequestMapping(value = "/info")
	@Transactional(rollbackFor = Exception.class)
	public void info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.intro1,t.intro2,t.intro3,t.intro4,t.intro5,t.intro_pic1,t.intro_pic2,t.tel,t.name,t.headman_name,t.address,t.remark from t_hospital t where t.id=?");
			pst.setObject(1, hospitalId);
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

	@RequestMapping(value = "/clinics")
	@Transactional(rollbackFor = Exception.class)
	public void clinics(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			if (hospitalIdParam == null)
				throw new InteractRuntimeException("hospital_id 不能空");
			int hospitalId = Integer.parseInt(hospitalIdParam);
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
					"select (select count(id) from t_patient where clinic_id=t.id) patient_count,t.id,t.name,t.headman_name,t.contact_tel,t.address,t.remark from t_clinic t where t.hospital_id=? order by t.name asc limit ?,?");
			pst.setObject(1, hospitalId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
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
}