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

@Controller("maintain.api.HospitalManage")
@RequestMapping(value = "/mm/hm")
public class HospitalManageApis {

	public static Logger logger = Logger.getLogger(HospitalManageApis.class);

	@RequestMapping(value = "/hospitallist")
	public void hospitallist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
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
			if (tel != null)
				sqlParams.add(new StringBuilder("%").append(tel).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					new StringBuilder("select t.name,t.tel,t.id,t.headman_name from t_hospital t where 1=1 ")
							.append(name == null ? "" : " and t.name like ?")
							.append(tel == null ? "" : " and t.tel like ?")
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
				item.put("tel", rs.getObject("tel"));
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

	@RequestMapping(value = "/hospitaldetail")
	public void hospitalDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
					"select t.name,t.tel,t.id,t.headman_name,t.cover,t.intro1,t.intro2,t.intro3,t.intro4,t.intro5,t.intro_pic1,t.intro_pic2,t.address,t.province_name,t.city_name,t.district_name,t.longitude,t.latitude from t_hospital t where t.id=? ")
							.toString());
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			JSONObject detail = new JSONObject();
			if (rs.next()) {
				detail.put("id", rs.getObject("id"));
				detail.put("name", rs.getObject("name"));
				detail.put("tel", rs.getObject("tel"));
				detail.put("headmanName", rs.getObject("headman_name"));
				detail.put("cover", rs.getObject("cover"));
				detail.put("intro1", rs.getObject("intro1"));
				detail.put("intro2", rs.getObject("intro2"));
				detail.put("intro3", rs.getObject("intro3"));
				detail.put("intro4", rs.getObject("intro4"));
				detail.put("intro5", rs.getObject("intro5"));
				detail.put("introPic1", rs.getObject("intro_pic1"));
				detail.put("introPic2", rs.getObject("intro_pic2"));
				detail.put("address", rs.getObject("address"));
				detail.put("provinceName", rs.getObject("province_name"));
				detail.put("cityName", rs.getObject("city_name"));
				detail.put("districtName", rs.getObject("district_name"));
				detail.put("longitude", rs.getObject("longitude"));
				detail.put("latitude", rs.getObject("latitude"));
			} else
				throw new InteractRuntimeException(1404, "目标不存在");
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

	@RequestMapping(value = "/hospitalalter")
	public void hospitalAlter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String headmanName = StringUtils.trimToNull(request.getParameter("headman_name"));
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			String intro1 = StringUtils.trimToNull(request.getParameter("intro1"));
			String intro2 = StringUtils.trimToNull(request.getParameter("intro2"));
			String intro3 = StringUtils.trimToNull(request.getParameter("intro3"));
			String introPic1 = StringUtils.trimToNull(request.getParameter("intro_pic1"));
			String introPic2 = StringUtils.trimToNull(request.getParameter("intro_pic2"));
			String address = StringUtils.trimToNull(request.getParameter("address"));
			String provinceName = StringUtils.trimToNull(request.getParameter("province_name"));
			String cityName = StringUtils.trimToNull(request.getParameter("city_name"));
			String districtName = StringUtils.trimToNull(request.getParameter("district_name"));
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			Integer provinceId = provinceIdParam == null ? null : Integer.parseInt(provinceIdParam);
			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			Integer cityId = cityIdParam == null ? null : Integer.parseInt(cityIdParam);
			String districtIdParam = StringUtils.trimToNull(request.getParameter("district_id"));
			Integer districtId = districtIdParam == null ? null : Integer.parseInt(districtIdParam);
			String longitude = StringUtils.trimToNull(request.getParameter("longitude"));
			String latitude = StringUtils.trimToNull(request.getParameter("latitude"));
			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(name);
			if (tel != null)
				sqlParams.add(tel);
			if (headmanName != null)
				sqlParams.add(headmanName);
			if (cover != null)
				sqlParams.add(cover);
			if (intro1 != null)
				sqlParams.add(intro1);
			if (intro2 != null)
				sqlParams.add(intro2);
			if (intro3 != null)
				sqlParams.add(intro3);
			if (introPic1 != null)
				sqlParams.add(introPic1);
			if (introPic2 != null)
				sqlParams.add(introPic2);
			if (address != null)
				sqlParams.add(address);
			if (provinceName != null)
				sqlParams.add(provinceName);
			if (cityName != null)
				sqlParams.add(cityName);
			if (districtName != null)
				sqlParams.add(districtName);
			if (provinceId != null)
				sqlParams.add(provinceId);
			if (cityId != null)
				sqlParams.add(cityId);
			if (districtId != null)
				sqlParams.add(districtId);
			if (longitude != null)
				sqlParams.add(longitude);
			if (latitude != null)
				sqlParams.add(latitude);
			sqlParams.add(id);
			pst = connection.prepareStatement(new StringBuilder("update t_hospital set id=id")
					.append(name == null ? "" : ",name=?").append(tel == null ? "" : ",tel=?")
					.append(headmanName == null ? "" : ",headman_name=?").append(cover == null ? "" : ",cover=?")
					.append(intro1 == null ? "" : ",intro1=?").append(intro2 == null ? "" : ",intro2=?")
					.append(intro3 == null ? "" : ",intro3=?").append(introPic1 == null ? "" : ",intro_pic1=?")
					.append(introPic2 == null ? "" : ",intro_pic2=?").append(address == null ? "" : ",address=?")
					.append(provinceName == null ? "" : ",province_name=?")
					.append(cityName == null ? "" : ",city_name=?")
					.append(districtName == null ? "" : ",district_name=?")
					.append(provinceId == null ? "" : ",province_id=?").append(cityId == null ? "" : ",city_id=?")
					.append(districtId == null ? "" : ",district_id=?").append(longitude == null ? "" : ",longitude=?")
					.append(latitude == null ? "" : ",latitude=?").append(" where id=?").toString());
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