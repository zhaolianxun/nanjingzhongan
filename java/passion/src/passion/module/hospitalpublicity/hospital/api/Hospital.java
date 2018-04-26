package passion.module.hospitalpublicity.hospital.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import passion.dao.entity.HpHospital;
import passion.dao.mapper.HpHospitalMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Hospital")
@RequestMapping(value = "/hp/h/hospital")
public class Hospital extends BaseController {

	public static Logger logger = Logger.getLogger(Hospital.class);

	@Autowired
	protected HpHospitalMapper hpHospitalMapper;

	@RequestMapping(value = "/alter", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String name = StringUtils.trim(request.getParameter("name"));
			if (StringUtils.isBlank(name))
				throw new InteractRuntimeException("name不可空");
			String picture = StringUtils.trim(request.getParameter("picture"));
			if (StringUtils.isBlank(picture))
				throw new InteractRuntimeException("picture不可空");
			String introduction = StringUtils.trim(request.getParameter("introduction"));
			if (StringUtils.isBlank(introduction))
				throw new InteractRuntimeException("introduction不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpHospital hospital = new HpHospital();
			hospital.setId(loginStatus.getHospitalId());
			hospital.setHospitalName(name);
			hospital.setPicture(picture);
			hospital.setIntroduction(introduction);
			hpHospitalMapper.updateByPrimaryKeySelective(hospital);

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
