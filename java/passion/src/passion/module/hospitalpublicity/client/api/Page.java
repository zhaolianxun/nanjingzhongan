package passion.module.hospitalpublicity.client.api;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.client.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.client.dao.mapper.PageMapper;
import passion.module.hospitalpublicity.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.client.api.Page")
@RequestMapping(value = "/hp/c/page")
public class Page extends BaseController {

	public static Logger logger = Logger.getLogger(Page.class);

	@Resource(name = "passion.module.hospitalpublicity.client.dao.mapper.PageMapper")
	protected PageMapper pageMapper;

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			// TODO 业务处理
			List<Map> teamDoctors = pageMapper.getHomePageTeamDoctors(loginStatus.getHospitalId());
			List<Map> featuredDepartments = pageMapper.getHomePageFeaturedDepartments(loginStatus.getHospitalId());
			List<Map> rollPictures = pageMapper.getHomePageRollPictures(loginStatus.getHospitalId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("teamDoctors", teamDoctors);
			data.put("featuredDepartments", featuredDepartments);
			data.put("rollPictures", rollPictures);
			
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/doctorinfo", method = RequestMethod.POST)
	public void doctorInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			String doctorId = StringUtils.trimToNull(request.getParameter("doctor_id"));
			if (doctorId == null)
				throw new InteractRuntimeException("doctorId不可空");

			// TODO 业务处理
			Map doctorInfo = pageMapper.getDoctorInfoPage(doctorId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("doctorInfo", doctorInfo);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/hospitalinfo", method = RequestMethod.POST)
	public void hospitalInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// TODO 业务处理
			Map hospitalInfo = pageMapper.getHospitalInfoPage(loginStatus.getHospitalId());

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("hospitalInfo", hospitalInfo);

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
			// TODO 获取并处理参数
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getActivityPageActivitiesCount(loginStatus.getHospitalId());
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> activities = pageMapper.getActivityPageActivities(loginStatus.getHospitalId(), pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject activitiesJson = new JSONObject();
			activitiesJson.put("items", activities);
			activitiesJson.put("pageNo", pagingPage.getPageNo());
			activitiesJson.put("pageSize", pagingPage.getPageSize());
			activitiesJson.put("totalItemCount", pagingPage.getTotalItemCount());
			activitiesJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("activities", activitiesJson);
			data.put("topPicture", "");

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
			// TODO 获取并处理参数
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getProjectPageProjectsCount(loginStatus.getHospitalId());
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> projects = pageMapper.getProjectPageProjects(loginStatus.getHospitalId(), pagingPage);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject projectsJson = new JSONObject();
			projectsJson.put("items", projects);
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

	@RequestMapping(value = "/doctorlookmore", method = RequestMethod.POST)
	public void doctorLookMore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// TODO 业务处理
			Long totalItemCount = pageMapper.getDoctorLookMorePageDoctorsCount(loginStatus.getHospitalId());
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> doctors = pageMapper.getDoctorLookMorePageDoctors(loginStatus.getHospitalId(), pagingPage);

			List<Map> teamDoctors = pageMapper.getHomePageTeamDoctors(loginStatus.getHospitalId());

			// TODO 返回结果
			JSONObject data = new JSONObject();
			JSONObject doctorsJson = new JSONObject();
			doctorsJson.put("items", doctors);
			doctorsJson.put("pageNo", pagingPage.getPageNo());
			doctorsJson.put("pageSize", pagingPage.getPageSize());
			doctorsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			doctorsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("doctors", doctorsJson);

			data.put("teamDoctors", teamDoctors);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/activityinfo", method = RequestMethod.POST)
	public void activityInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			String activityId = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityId == null)
				throw new InteractRuntimeException("activityId不可空");

			// TODO 业务处理
			Map activityInfo = pageMapper.getActivityInfoPage(activityId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("activityInfo", activityInfo);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/projectinfo", method = RequestMethod.POST)
	public void projectInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取并处理参数
			String projectId = StringUtils.trimToNull(request.getParameter("project_id"));
			if (projectId == null)
				throw new InteractRuntimeException("project_id不可空");

			// TODO 业务处理
			Map info = pageMapper.getProjectInfoPage(projectId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("projectInfo", info);

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
