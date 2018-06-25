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
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.module.maintain.business.LoginStatus;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("maintain.api.UserManage")
@RequestMapping(value = "/mm/um")
public class UserManageApis {

	public static Logger logger = Logger.getLogger(UserManageApis.class);

	@RequestMapping(value = "/userlist")
	public void hospitallist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String type = StringUtils.trimToNull(request.getParameter("type"));
			String clinicIdParam = StringUtils.trimToNull(request.getParameter("clinic_id"));
			Integer clinicId = clinicIdParam == null ? null : Integer.parseInt(clinicIdParam);
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
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			if (type != null)
				sqlParams.add(type);
			if (clinicId != null)
				sqlParams.add(clinicId);
			if (hospitalId != null)
				sqlParams.add(hospitalId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					new StringBuilder("select u.id,u.phone,u.realname,u.type from t_user u where 1=1 ")
							.append(phone == null ? "" : " and u.phone like ?")
							.append(type == null ? "" : " and u.type like ?")
							.append(clinicId == null ? "" : " and u.clinic_id = ?")
							.append(hospitalId == null ? "" : " and u.hospital_id = ?")
							.append(" order by u.register_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("type", rs.getObject("type"));
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

	@RequestMapping(value = "/udetail")
	public void userDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			pst = connection.prepareStatement(
					new StringBuilder("select u.id,u.phone,u.realname,u.type from t_user u where u.id=? ").toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("id", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("realname", rs.getObject("realname"));
				item.put("type", rs.getObject("type"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在", null);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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

	@RequestMapping(value = "/ualter")
	public void userAlter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String realname = StringUtils.trimToNull(request.getParameter("realname"));

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(phone);
			if (realname != null)
				sqlParams.add(realname);
			sqlParams.add(id);
			pst = connection.prepareStatement(
					new StringBuilder("update t_user set id=id").append(phone == null ? "" : ",phone=?")
							.append(realname == null ? "" : ",realname=?").append(" where id=?").toString());
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