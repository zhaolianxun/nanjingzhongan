package passion.module.hospitalpublicity.hospital.api;

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
import passion.dao.entity.HpClientHomepageRollpicture;
import passion.dao.mapper.HpActivityMapper;
import passion.dao.mapper.HpClientHomepageRollpictureMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.dao.mapper.ClientHomeRollpictureManageMapper;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.ClientHomePageRollPictures")
@RequestMapping(value = "/hp/h/clienthomepagerollpictures")
public class ClientHomePageRollPictures extends BaseController {

	public static Logger logger = Logger.getLogger(ClientHomePageRollPictures.class);
	@Autowired
	protected HpActivityMapper hpActivityMapper;

	@Autowired
	protected ClientHomeRollpictureManageMapper clientHomepageRollpictureManageMapper;

	@Autowired
	protected HpClientHomepageRollpictureMapper hpClientHomePageRollpictureMapper;

	@RequestMapping(value = "/addactivity", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void clientHomePageRollPictures(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String picture = StringUtils.trimToNull(request.getParameter("picture"));
			if (picture == null)
				throw new InteractRuntimeException("picture不可空");
			String activityId = StringUtils.trimToNull(request.getParameter("activity_id"));
			if (activityId == null)
				throw new InteractRuntimeException("activityId不可空");

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
				throw new InteractRuntimeException("不是您医院的活动");

			if (clientHomepageRollpictureManageMapper.getLinkIdCount(activityId) > 0)
				throw new InteractRuntimeException("已存在");

			// 锁表
			byte maxSequenceNo = clientHomepageRollpictureManageMapper
					.selectMaxSequenceNoAndLockTable(activity.getHospitalId());
			if (maxSequenceNo >= 127)
				throw new InteractRuntimeException("位置已满");

			HpClientHomepageRollpicture rollpicture = new HpClientHomepageRollpicture();
			String id = RandomStringUtils.randomNumeric(12);
			rollpicture.setId(id);
			rollpicture.setHospitalId(loginStatus.getHospitalId());
			rollpicture.setLinkId(activityId);
			rollpicture.setType((byte) 1);
			rollpicture.setSequenceNo((byte) (maxSequenceNo + 1));
			rollpicture.setPicture(picture);
			hpClientHomePageRollpictureMapper.insertSelective(rollpicture);

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

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String id = StringUtils.trimToNull(request.getParameter("id"));
			if (id == null)
				throw new InteractRuntimeException("id不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpClientHomepageRollpicture clientHomePageRollpicture = hpClientHomePageRollpictureMapper
					.selectByPrimaryKey(id);
			if (clientHomePageRollpicture == null)
				throw new InteractRuntimeException("不存在");
			if (!clientHomePageRollpicture.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不属于您的医院");

			if (StringUtils.isNotEmpty(clientHomePageRollpicture.getPicture()))
				ossMapper.delOssOfArray(clientHomePageRollpicture.getPicture());
			hpClientHomePageRollpictureMapper.deleteByPrimaryKey(id);

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
