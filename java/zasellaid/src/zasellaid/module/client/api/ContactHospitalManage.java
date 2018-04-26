package zasellaid.module.client.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import zasellaid.constant.SysConstant;
import zasellaid.dao.entity.ContactHospital;
import zasellaid.dao.mapper.ContactHospitalMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.entity.PagingPage;
import zasellaid.module.client.business.GetLoginStatus;
import zasellaid.module.client.dao.mapper.ContactHospitalManageMapper;
import zasellaid.module.client.entity.UserLoginStatus;
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;

@Controller("controller.client.ContactHospitalManage")
@RequestMapping(value = "/c/ch")
public class ContactHospitalManage {

	public static Logger logger = Logger.getLogger(ContactHospitalManage.class);

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public void add(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			if (provinceIdParam == null)
				throw new InteractRuntimeException("province_id can`t be empty.");
			Integer provinceId = Integer.parseInt(provinceIdParam);
			String provinceName = StringUtils.trimToNull(request.getParameter("province_name"));

			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			if (cityIdParam == null)
				throw new InteractRuntimeException("city_id can`t be empty.");
			Integer cityId = Integer.parseInt(cityIdParam);
			String cityName = StringUtils.trimToNull(request.getParameter("city_name"));

			String customerTypeParam = StringUtils.trimToNull(request.getParameter("customer_type"));
			if (customerTypeParam == null)
				throw new InteractRuntimeException("customer_type can`t be empty.");
			int customerType = Integer.parseInt(customerTypeParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name can`t be empty.");
			String deanPhone = StringUtils.trimToNull(request.getParameter("dean_phone"));
			String deanName = StringUtils.trimToNull(request.getParameter("dean_name"));
			String hospitalTel = StringUtils.trimToNull(request.getParameter("hospital_tel"));
			String departmentName = StringUtils.trimToNull(request.getParameter("department_name"));
			String directorPhone = StringUtils.trimToNull(request.getParameter("director_phone"));
			String extraInfos = StringUtils.trimToNull(request.getParameter("extra_infos"));
			if (extraInfos != null)
				try {
					JSON.parseArray(extraInfos);
				} catch (Exception e) {
					throw new InteractRuntimeException("extra_infos isn`t a json array.");
				}

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);

			ContactHospital contactHospital = new ContactHospital();
			String id = RandomStringUtils.randomNumeric(12);
			contactHospital.setId(id);
			contactHospital.setCityId(cityId);
			contactHospital.setCityName(cityName);
			contactHospital.setProvinceId(provinceId);
			contactHospital.setProvinceName(provinceName);
			contactHospital.setCustomerType(customerType);
			contactHospital.setDeanPhone(deanPhone);
			contactHospital.setDepartmentName(departmentName);
			contactHospital.setDirectorPhone(directorPhone);
			contactHospital.setExtraInfos(extraInfos);
			contactHospital.setHospitalTel(hospitalTel);
			contactHospital.setName(name);
			contactHospital.setTraceStatus(0);
			contactHospital.setDeanName(deanName);
			contactHospital.setClientUserId(loginStatus.getUserId());
			contactHospital.setAddTime(new Date().getTime());
			contactHospitalMapper.insertSelective(contactHospital);

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("contactHospitalId", id);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	public void detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);

			ContactHospital contactHospital = contactHospitalMapper.selectByPrimaryKey(contactHospitalId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("detail", contactHospital);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/alter", method = RequestMethod.POST)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			String provinceName = StringUtils.trimToNull(request.getParameter("province_name"));
			if (provinceIdParam != null && provinceName == null)
				throw new InteractRuntimeException("province_name can`t be empty.");
			Integer provinceId = provinceIdParam == null ? null : Integer.parseInt(provinceIdParam);

			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			String cityName = StringUtils.trimToNull(request.getParameter("city_name"));
			if (cityIdParam != null && cityName == null)
				throw new InteractRuntimeException("city_name can`t be empty.");
			Integer cityId = cityIdParam == null ? null : Integer.parseInt(cityIdParam);

			String customerTypeParam = StringUtils.trimToNull(request.getParameter("customer_type"));
			Integer customerType = customerTypeParam == null ? null : Integer.parseInt(customerTypeParam);
			String name = StringUtils.trim(request.getParameter("name"));
			if (name == "")
				throw new InteractRuntimeException("name can`t be empty.");
			String deanPhone = StringUtils.trim(request.getParameter("dean_phone"));
			String deanName = StringUtils.trim(request.getParameter("dean_name"));
			String hospitalTel = StringUtils.trim(request.getParameter("hospital_tel"));
			String departmentName = StringUtils.trim(request.getParameter("department_name"));
			String directorPhone = StringUtils.trim(request.getParameter("director_phone"));
			String extraInfos = StringUtils.trim(request.getParameter("extra_infos"));
			if (extraInfos != null && extraInfos != "")
				try {
					JSON.parseArray(extraInfos);
				} catch (Exception e) {
					throw new InteractRuntimeException("extra_infos isn`t a json array.");
				}

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);
			ContactHospital contactHospital = contactHospitalMapper.selectByPrimaryKey(contactHospitalId);
			if (!contactHospital.getClientUserId().equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("不是您的医院哦");

			contactHospital.setCityId(cityId);
			contactHospital.setCityName(cityName);
			contactHospital.setProvinceId(provinceId);
			contactHospital.setProvinceName(provinceName);
			contactHospital.setCustomerType(customerType);
			contactHospital.setDeanPhone(deanPhone);
			contactHospital.setDepartmentName(departmentName);
			contactHospital.setDeanName(deanName);
			contactHospital.setDirectorPhone(directorPhone);
			contactHospital.setExtraInfos(extraInfos);
			contactHospital.setHospitalTel(hospitalTel);
			contactHospital.setName(name);
			contactHospitalMapper.updateByPrimaryKeySelective(contactHospital);

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	// @RequestMapping(value = "/del", method = RequestMethod.POST)
	// public void del(HttpServletRequest request, HttpServletResponse response)
	// throws Exception {
	// SqlSession session = null;
	// try {
	// // TODO 获取请求参数
	// String contactHospitalId =
	// StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
	// if (contactHospitalId == null)
	// throw new InteractRuntimeException("contact_hospital_id can`t be
	// empty.");
	//
	// // TODO 业务处理
	// UserLoginStatus loginStatus = GetLoginStatus.todo(request);
	// if (loginStatus == null)
	// throw new InteractRuntimeException(20);
	//
	// session = SysConstant.sqlSessionFactory.openSession(false);
	// ContactHospitalMapper contactHospitalMapper =
	// session.getMapper(ContactHospitalMapper.class);
	// TraceRecordMapper traceRecordMapper =
	// session.getMapper(TraceRecordMapper.class);
	//
	// ContactHospital contactHospital =
	// contactHospitalMapper.selectByPrimaryKey(contactHospitalId);
	// if (!contactHospital.getClientUserId().equals(loginStatus.getUserId()))
	// throw new InteractRuntimeException("不是您的医院哦");
	//
	// int traceCount =
	// traceRecordMapper.contactHospitalTraceRecordCount(contactHospitalId);
	// if (traceCount == 0)
	// contactHospitalMapper.deleteByPrimaryKey(contactHospitalId);
	// else
	// contactHospital.setClientUserDel(1);
	// contactHospitalMapper.updateByPrimaryKeySelective(contactHospital);
	//
	// session.commit();
	// // TODO 返回结果
	// HttpRespondWithData.todo(request, response, 0, null, null);
	// } catch (Exception e) {
	// // TODO 处理异常
	// logger.info(ExceptionUtils.getStackTrace(e));
	// if (session != null)
	// session.rollback();
	// HttpRespondWithData.exception(request, response, e);
	// } finally {
	// // 释放资源
	// if (session != null)
	// session.close();
	// }
	// }

	@RequestMapping(value = "/hospitals", method = RequestMethod.POST)
	public void hospitals(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// 获取并处理参数
			String traceStarttimeParam = StringUtils.trimToNull(request.getParameter("trace_starttime"));
			Long traceStarttime = traceStarttimeParam == null ? null : Long.parseLong(traceStarttimeParam);
			String traceEndtimeParam = StringUtils.trimToNull(request.getParameter("trace_endtime"));
			Long traceEndtime = traceEndtimeParam == null ? null : Long.parseLong(traceEndtimeParam);
			String customerTypeParam = StringUtils.trimToNull(request.getParameter("customer_type"));
			Byte customerType = customerTypeParam == null ? null : Byte.parseByte(customerTypeParam);
			String traceStatusParam = StringUtils.trimToNull(request.getParameter("trace_status"));
			Byte traceStatus = traceStatusParam == null ? null : Byte.parseByte(traceStatusParam);
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			Integer provinceId = provinceIdParam == null ? null : Integer.parseInt(provinceIdParam);
			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			Integer cityId = cityIdParam == null ? null : Integer.parseInt(cityIdParam);
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

			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalManageMapper contactHospitalManageMapper = session
					.getMapper(ContactHospitalManageMapper.class);

			Long totalItemCount = contactHospitalManageMapper.hospitals1Count(loginStatus.getUserId(), customerType,
					traceStarttime, traceEndtime, traceStatus, provinceId, cityId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> hospitals = contactHospitalManageMapper.hospitals1(loginStatus.getUserId(), customerType,
					traceStarttime, traceEndtime, traceStatus, provinceId, cityId, pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject hospitalsJson = new JSONObject();
			hospitalsJson.put("items", hospitals);
			hospitalsJson.put("pageNo", pagingPage.getPageNo());
			hospitalsJson.put("pageSize", pagingPage.getPageSize());
			hospitalsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			hospitalsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("hospitals", hospitalsJson);

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/existprovinces", method = RequestMethod.POST)
	public void existProvinces(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalManageMapper contactHospitalManageMapper = session
					.getMapper(ContactHospitalManageMapper.class);

			List<Map> items = contactHospitalManageMapper.existProvinces();
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/existcities", method = RequestMethod.POST)
	public void existCities(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			if (provinceIdParam == null)
				throw new InteractRuntimeException("province_id can`t be empty.");
			int provinceId = Integer.parseInt(provinceIdParam);

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			ContactHospitalManageMapper contactHospitalManageMapper = session
					.getMapper(ContactHospitalManageMapper.class);

			List<Map> items = contactHospitalManageMapper.existCities(provinceId);
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}
}
