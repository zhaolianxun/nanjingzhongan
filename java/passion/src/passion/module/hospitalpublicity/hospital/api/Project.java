package passion.module.hospitalpublicity.hospital.api;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.HpProject;
import passion.dao.mapper.HpProjectMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Project")
@RequestMapping(value = "/hp/h/project")
public class Project extends BaseController {

	public static Logger logger = Logger.getLogger(Project.class);
	@Autowired
	protected HpProjectMapper hpProjectMapper;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String title = StringUtils.trimToNull(request.getParameter("title"));
			if (title == null)
				throw new InteractRuntimeException("title不可空");
			String titlePicture = StringUtils.trimToNull(request.getParameter("title_picture"));
			if (titlePicture == null)
				throw new InteractRuntimeException("titlePicture不可空");
			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpProject project = new HpProject();
			String projectId = RandomStringUtils.randomNumeric(12);
			project.setId(projectId);
			project.setContent(content);
			project.setCreateTime(new Date().getTime());
			project.setHospitalId(loginStatus.getHospitalId());
			project.setTitle(title);
			project.setTitlePicture(titlePicture);
			hpProjectMapper.insertSelective(project);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("projectId", projectId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/alter", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String projectId = StringUtils.trimToNull(request.getParameter("project_id"));
			if (projectId == null)
				throw new InteractRuntimeException("project_id不可空");
			String title = StringUtils.trim(request.getParameter("title"));
			if (title == "")
				throw new InteractRuntimeException("title不可空");
			String titlePicture = StringUtils.trim(request.getParameter("title_picture"));
			if (titlePicture == "")
				throw new InteractRuntimeException("titlePicture不可空");
			String content = StringUtils.trim(request.getParameter("content"));
			if (content == "")
				throw new InteractRuntimeException("content不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpProject project = hpProjectMapper.selectByPrimaryKey(projectId);
			if (project == null)
				throw new InteractRuntimeException("活动不存在");
			if (!project.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("项目不属于您的医院");

			if (titlePicture != null && StringUtils.isNotEmpty(project.getTitlePicture()))
				ossMapper.delOssOfArray(project.getTitlePicture());

			project.setContent(content);
			project.setTitle(title);
			project.setTitlePicture(titlePicture);
			hpProjectMapper.updateByPrimaryKeySelective(project);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/del", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void del(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String projectId = StringUtils.trimToNull(request.getParameter("project_id"));
			if (projectId == null)
				throw new InteractRuntimeException("project_id不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpProject project = hpProjectMapper.selectByPrimaryKey(projectId);
			if (project == null)
				throw new InteractRuntimeException("活动不存在");
			if (!project.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("项目不属于您的医院");

			if (StringUtils.isNotEmpty(project.getTitlePicture()))
				ossMapper.delOssOfArray(project.getTitlePicture());

			hpProjectMapper.deleteByPrimaryKey(projectId);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}
}
