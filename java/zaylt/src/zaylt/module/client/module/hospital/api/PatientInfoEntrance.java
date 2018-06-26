package zaylt.module.client.module.hospital.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Controller("client.hospital.api.PatientInfoEntrance")
@RequestMapping(value = "/c/h/e/patientinfo")
public class PatientInfoEntrance {

	public static Logger logger = Logger.getLogger(PatientInfoEntrance.class);

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
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

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
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

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
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

			connection = ZayltDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.sickness,t.status,t.id,t.realname,t.tel,u.name clinic_name from t_patient t left join t_clinic u on t.clinic_id=u.id  where t.status in ('3','4') and t.hospital_id=? order by t.realname asc limit ?,?");
			pst.setObject(1, loginStatus.getHospitalId());
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

	@RequestMapping(value = "/confirm")
	@Transactional(rollbackFor = Exception.class)
	public void confirm(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String patientId = StringUtils.trimToNull(request.getParameter("patient_id"));
			if (patientId == null)
				throw new InteractRuntimeException("patient_id 不能为空");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("1"))
				throw new InteractRuntimeException("您不是医院用户");

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

			if (status.equals("4"))
				throw new InteractRuntimeException("已确认过");
			if (!status.equals("3"))
				throw new InteractRuntimeException("门诊还没有转诊");

			pst = connection.prepareStatement("update t_patient set status='4' where id=?");
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