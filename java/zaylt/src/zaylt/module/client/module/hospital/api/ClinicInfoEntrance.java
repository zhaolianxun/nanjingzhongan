package zaylt.module.client.module.hospital.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.regex.Pattern;

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

@Controller("client.hospital.api.ClinicInfoEntrance")
@RequestMapping(value = "/c/h/e/clinicinfo")
public class ClinicInfoEntrance {

	public static Logger logger = Logger.getLogger(ClinicInfoEntrance.class);

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
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.id,t.name,t.headman_name,t.contact_tel,t.address,t.remark from t_clinic t where t.hospital_id=? order by t.name asc limit ?,?");
			pst.setObject(1, loginStatus.getHospitalId());
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
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone 不能为空");
//			if (Pattern.compile("^\\d+$").matcher(phone).find())
//				throw new InteractRuntimeException("手机号格式有误");
			String pwd = StringUtils.trimToNull(request.getParameter("pwd"));
			if (pwd == null)
				throw new InteractRuntimeException("pwd 不能为空");
//			if (Pattern.compile("\\s").matcher(pwd).find())
//				throw new InteractRuntimeException("密码格式错误");
			String clinicName = StringUtils.trimToNull(request.getParameter("clinic_name"));
			if (clinicName == null)
				throw new InteractRuntimeException("clinic_name 不能为空");
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			if (headmanName == null)
				throw new InteractRuntimeException("headman_name 不能为空");
			String contactTel = StringUtils.trimToNull(request.getParameter("contact_tel"));
			if (contactTel == null)
				throw new InteractRuntimeException("contact_tel 不能为空");
//			if (Pattern.compile("^\\d+$").matcher(contactTel).find())
//				throw new InteractRuntimeException("联系电话格式有误");
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
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

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
			pst.setObject(1, loginStatus.getHospitalId());
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
			pst.setObject(7, loginStatus.getHospitalId());
			pst.setObject(8, clinicId);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("手机号已存在");

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

}