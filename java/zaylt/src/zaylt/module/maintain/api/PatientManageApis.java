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

@Controller("maintain.api.PatientManage")
@RequestMapping(value = "/mm/pm")
public class PatientManageApis {

	public static Logger logger = Logger.getLogger(PatientManageApis.class);

	@RequestMapping(value = "/patientlist")
	public void hospitallist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String status = StringUtils.trimToNull(request.getParameter("status"));
			String clinicIdParam = StringUtils.trimToNull(request.getParameter("clinic_id"));
			Integer clinicId = clinicIdParam == null ? null : Integer.parseInt(clinicIdParam);
			String hospitalIdParam = StringUtils.trimToNull(request.getParameter("hospital_id"));
			Integer hospitalId = hospitalIdParam == null ? null : Integer.parseInt(hospitalIdParam);
			String hospitalName = StringUtils.trimToNull(request.getParameter("hospital_name"));
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
			if (realname != null)
				sqlParams.add(new StringBuilder("%").append(realname).append("%").toString());
			if (tel != null)
				sqlParams.add(new StringBuilder("%").append(tel).append("%").toString());
			if (status != null)
				sqlParams.add(status);
			if (clinicId != null)
				sqlParams.add(clinicId);
			if (hospitalId != null)
				sqlParams.add(hospitalId);
			if (hospitalName != null)
				sqlParams.add(new StringBuilder("%").append(hospitalName).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select p.id,p.realname,p.tel,p.status,c.name clinic_name,h.name hospital_name from t_patient p left join t_hospital h on p.hospital_id=h.id left join t_clinic c on p.clinic_id=c.id  where 1=1 ")
							.append(realname == null ? "" : " and p.realname like ?")
							.append(tel == null ? "" : " and p.tel like ?")
							.append(status == null ? "" : " and p.status = ?")
							.append(clinicId == null ? "" : " and p.clinic_id = ?")
							.append(hospitalId == null ? "" : " and p.hospital_id = ?")
							.append(hospitalName == null ? "" : " and h.name like ?")
							.append(" order by p.add_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("realname", rs.getObject("realname"));
				item.put("tel", rs.getObject("tel"));
				item.put("status", rs.getObject("status"));
				item.put("clinicName", rs.getObject("clinic_name"));
				item.put("hospitalName", rs.getObject("hospital_name"));
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

}