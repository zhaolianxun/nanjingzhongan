package passion.module.hospitalpublicity.hospital.api;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.dao.mapper.PageMapper;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Page")
@RequestMapping(value = "/hp/h/page")
public class Page extends BaseController {

	public static Logger logger = Logger.getLogger(Page.class);

	@Resource(name = "passion.module.hospitalpublicity.hospital.dao.mapper.PageMapper")
	protected PageMapper pageMapper;

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			Map homeInfo = pageMapper.homeInfo(loginStatus.getHospitalId());

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("homeInfo", homeInfo);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/doctors", method = RequestMethod.POST)
	public void doctors(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getDoctorsPageDoctorsCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getDoctorsPageDoctors(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject doctorsJson = new JSONObject();
			doctorsJson.put("items", items);
			doctorsJson.put("pageNo", pagingPage.getPageNo());
			doctorsJson.put("pageSize", pagingPage.getPageSize());
			doctorsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			doctorsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("doctors", doctorsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/departments", method = RequestMethod.POST)
	public void departments(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getDepartmentsPageDepartmentsCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getDepartmentsPageDepartments(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject departmentsJson = new JSONObject();
			departmentsJson.put("items", items);
			departmentsJson.put("pageNo", pagingPage.getPageNo());
			departmentsJson.put("pageSize", pagingPage.getPageSize());
			departmentsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			departmentsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("departments", departmentsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/activities", method = RequestMethod.POST)
	public void activities(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getActivitiesPageActivitiesCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getActivitiesPageActivities(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject departmentsJson = new JSONObject();
			departmentsJson.put("items", items);
			departmentsJson.put("pageNo", pagingPage.getPageNo());
			departmentsJson.put("pageSize", pagingPage.getPageSize());
			departmentsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			departmentsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("activities", departmentsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/projects", method = RequestMethod.POST)
	public void projects(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getProjectsPageProjectsCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getProjectsPageProjects(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject projectsJson = new JSONObject();
			projectsJson.put("items", items);
			projectsJson.put("pageNo", pagingPage.getPageNo());
			projectsJson.put("pageSize", pagingPage.getPageSize());
			projectsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			projectsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("projects", projectsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/featureddepartments", method = RequestMethod.POST)
	public void featuredDepartment(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getFeaturedDepartmentsPageDepartmentsCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getFeaturedDepartmentsPageDepartments(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject departmentsJson = new JSONObject();
			departmentsJson.put("items", items);
			departmentsJson.put("pageNo", pagingPage.getPageNo());
			departmentsJson.put("pageSize", pagingPage.getPageSize());
			departmentsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			departmentsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("departments", departmentsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/clienthomepagerollpictures", method = RequestMethod.POST)
	public void clientHomePageRollPictures(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 获取并处理参数
			String hospitalId = loginStatus.getHospitalId();
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getClientHomePageRollPicturesCount(hospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> items = pageMapper.getClientHomePageRollPictures(hospitalId, pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject rollPicturesJson = new JSONObject();
			rollPicturesJson.put("items", items);
			rollPicturesJson.put("pageNo", pagingPage.getPageNo());
			rollPicturesJson.put("pageSize", pagingPage.getPageSize());
			rollPicturesJson.put("totalItemCount", pagingPage.getTotalItemCount());
			rollPicturesJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("rollPictures", rollPicturesJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}
}
