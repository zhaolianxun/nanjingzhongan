package zaylt.module.client.module.clinic.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
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

@Controller("client.clinic.api.UserAction")
@RequestMapping(value = "/c/c/e/patientinfo")
public class PatientInfoEntrance {

	public static Logger logger = Logger.getLogger(PatientInfoEntrance.class);

	@RequestMapping(value = "/patients")
	@Transactional(rollbackFor = Exception.class)
	public void patients(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.sickness,t.id,t.realname,t.tel,t.status,h.name hospital_name from t_patient t inner join t_hospital h on t.hospital_id=h.id where t.clinic_id=? order by t.realname asc limit ?,?");
			pst.setObject(1, loginStatus.getClinicId());
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("patientId", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("tel", rs.getObject("tel"));
				item.put("status", rs.getObject("status"));
				item.put("sickness", rs.getObject("sickness"));
				item.put("hospitalName", rs.getObject("hospital_name"));
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

	@RequestMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public void add(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			if (realname == null)
				throw new InteractRuntimeException("realname 不能为空");
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			if (tel == null)
				throw new InteractRuntimeException("tel 不能为空");
			String sickness = StringUtils.trimToNull(request.getParameter("sickness"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select count(id) from t_patient where clinic_id=? and hospital_id=? and realname=? and tel=?");
		
			pst = connection.prepareStatement(
					"insert into t_patient (id,clinic_id,hospital_id,realname,tel,sickness,remark,add_time) values(?,?,?,?,?,?,?,?)");
			String id = RandomStringUtils.randomNumeric(12);
			pst.setObject(1, id);
			pst.setObject(2, loginStatus.getClinicId());
			pst.setObject(3, loginStatus.getHospitalId());
			pst.setObject(4, realname);
			pst.setObject(5, tel);
			pst.setObject(6, sickness);
			pst.setObject(7, remark);
			pst.setObject(8, new Date().getTime());
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("patientId", id);
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

	@RequestMapping(value = "/addremark")
	@Transactional(rollbackFor = Exception.class)
	public void addremark(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String patientId = StringUtils.trimToNull(request.getParameter("patient_id"));
			if (patientId == null)
				throw new InteractRuntimeException("patient_id 不能为空");
			String remark = StringUtils.trimToNull(request.getParameter("remark"));
			if (remark == null)
				throw new InteractRuntimeException("remark 不能为空");
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection
					.prepareStatement("insert into t_patient_remark (patient_id,remark,add_time) values(?,?,?)");
			pst.setObject(1, patientId);
			pst.setObject(2, remark);
			pst.setObject(3, new Date().getTime());
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

	@RequestMapping(value = "/remarks")
	@Transactional(rollbackFor = Exception.class)
	public void remarks(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Statement st = null;
		try {
			// 获取请求参数
			String patientId = StringUtils.trimToNull(request.getParameter("patient_id"));
			if (patientId == null)
				throw new InteractRuntimeException("patient_id 不能为空");
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
			// 查詢订单列表
			st = connection.createStatement();
			st.execute("set @rownum=0;");
			st.close();
			pst = connection.prepareStatement(
					"select * from (select  (@rownum:=@rownum+1) rownum,t.remark,t.add_time from t_patient_remark t  where t.patient_id=? order by t.add_time asc) tt order by tt.rownum desc limit ?,?");

			pst.setObject(1, patientId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("remark", rs.getObject("remark"));
				item.put("addTime", rs.getObject("add_time"));
				item.put("rownum", rs.getObject("rownum"));
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
			if (st != null)
				st.close();
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
			String patientId = StringUtils.trimToNull(request.getParameter("patient_id"));
			if (patientId == null)
				throw new InteractRuntimeException("patient_id 不能空");
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));
			String sickness = StringUtils.trimToNull(request.getParameter("sickness"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			List sqlParams = new ArrayList();
			if (remark != null)
				sqlParams.add(remark);
			if (sickness != null)
				sqlParams.add(sickness);
			if (realname != null)
				sqlParams.add(realname);
			if (tel != null)
				sqlParams.add(tel);
			if (!sqlParams.isEmpty()) {
				sqlParams.add(patientId);
				connection = ZayltDataSource.dataSource.getConnection();
				pst = connection.prepareStatement("update t_patient set id=id" + (remark == null ? "" : ",remark=?")
						+ (sickness == null ? "" : ",sickness=?") + (realname == null ? "" : ",realname=?")
						+ (tel == null ? "" : ",tel=?") + " where id=?");
				String id = RandomStringUtils.randomNumeric(12);
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

	@RequestMapping(value = "/transferdiagnose")
	@Transactional(rollbackFor = Exception.class)
	public void transferDiagnose(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String patientId = StringUtils.trimToNull(request.getParameter("patient_id"));
			if (patientId == null)
				throw new InteractRuntimeException("patient_id 不能空");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("2"))
				throw new InteractRuntimeException("您不是门诊用户");

			connection = ZayltDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select status from t_patient where id=? for update");
			pst.setObject(1, patientId);
			ResultSet rs = pst.executeQuery();
			String status = null;
			if (rs.next()) {
				status = rs.getString("status");
			} else
				throw new InteractRuntimeException(1404, "目标不存在",null);
			pst.close();

			if (status.equals('3'))
				throw new InteractRuntimeException("已转诊");
			if (status.equals('4'))
				throw new InteractRuntimeException("医院已接收");

			pst = connection.prepareStatement("update t_patient set status='3' where id=?");
			pst.setObject(1, patientId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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