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

import passion.dao.entity.HpActivity;
import passion.dao.mapper.HpActivityMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Activity")
@RequestMapping(value = "/hp/h/activity")
public class Activity extends BaseController {

	public static Logger logger = Logger.getLogger(Activity.class);

	@Autowired
	protected HpActivityMapper hpActivityMapper;

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
			String summary = StringUtils.trimToNull(request.getParameter("summary"));
			if (summary == null)
				throw new InteractRuntimeException("summary不可空");
			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpActivity activity = new HpActivity();
			String activityId = RandomStringUtils.randomNumeric(12);
			activity.setId(activityId);
			activity.setContent(content);
			activity.setCreateTime(new Date().getTime());
			activity.setHospitalId(loginStatus.getHospitalId());
			activity.setSummary(summary);
			activity.setTitle(title);
			activity.setTitlePicture(titlePicture);
			hpActivityMapper.insertSelective(activity);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("activityId", activityId);
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
	public void alert(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String activityId = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityId == null)
				throw new InteractRuntimeException("activity_id不可空");
			String title = StringUtils.trim(request.getParameter("title"));
			if (title == "")
				throw new InteractRuntimeException("title不可空");
			String titlePicture = StringUtils.trim(request.getParameter("title_picture"));
			if (titlePicture == "")
				throw new InteractRuntimeException("titlePicture不可空");
			String summary = StringUtils.trim(request.getParameter("summary"));
			if (summary == "")
				throw new InteractRuntimeException("summary不可空");
			String content = StringUtils.trim(request.getParameter("content"));
			if (content == "")
				throw new InteractRuntimeException("content不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpActivity activity = hpActivityMapper.selectByPrimaryKey(activityId);
			if (activity == null)
				throw new InteractRuntimeException("活动不存在");
			if (!activity.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("活动不属于您的医院");

			if (titlePicture != null && StringUtils.isNotEmpty(activity.getTitlePicture()))
				ossMapper.delOssOfArray(activity.getTitlePicture());

			activity.setContent(content);
			activity.setSummary(summary);
			activity.setTitle(title);
			activity.setTitlePicture(titlePicture);
			hpActivityMapper.updateByPrimaryKeySelective(activity);

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
			String activityId = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityId == null)
				throw new InteractRuntimeException("activity_id不可空");
			
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpActivity activity = hpActivityMapper.selectByPrimaryKey(activityId);
			if (activity == null)
				throw new InteractRuntimeException("活动不存在");
			if (!activity.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("活动不属于您的医院");

			if (StringUtils.isNotEmpty(activity.getTitlePicture()))
				ossMapper.delOssOfArray(activity.getTitlePicture());

			hpActivityMapper.deleteByPrimaryKey(activityId);

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
