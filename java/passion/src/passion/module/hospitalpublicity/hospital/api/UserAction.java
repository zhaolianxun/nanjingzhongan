package passion.module.hospitalpublicity.hospital.api;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.HpHospital;
import passion.dao.entity.HpHospitalUser;
import passion.dao.mapper.HpHospitalMapper;
import passion.dao.mapper.HpHospitalUserMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.dao.mapper.UserModuleMapper;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import passion.util.ProjectUtils;

@Controller("passion.module.hospitalpublicity.hospital.api.UserAction")
@RequestMapping(value = "/hp/h/user")
public class UserAction extends BaseController {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@Autowired
	protected UserModuleMapper userModuleMapper;
	@Autowired
	protected HpHospitalUserMapper hpHospitalUserMapper;
	@Autowired
	protected HpHospitalMapper hpHospitalMapper;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("用户名不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("密码不可空");

			// TODO 业务处理
			String passwordMd5 = DigestUtils.md5Hex(password);
			HpHospitalUser user = userModuleMapper.loginByUsernameAndPwdCheck(username, passwordMd5);
			if (user == null)
				throw new InteractRuntimeException("用户名或密码错误");
			if (user.getIfLoginable() != 1)
				throw new InteractRuntimeException("禁止登陆:" + user.getLoginunableReason());

			UserLoginStatus status = new UserLoginStatus(user.getId(), user.getUsername(), user.getPhone(),
					user.getNickname(), user.getIfSuperadmin(), user.getIfAdmin(), user.getHospitalId(), new Date());

			request.getSession(true).setAttribute("HospitalPublicityHospitalUserLoginStatus", status);

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

	@RequestMapping(value = "/logincheck", method = RequestMethod.POST)
	public void loginCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			byte ifLogin = 0;
			if (GetUserLoginStatus.todo(request) != null)
				ifLogin = 1;

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("ifLogin", ifLogin);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/forbidlogin", method = RequestMethod.POST)
	public void forbidLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("userId不可空");
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			HpHospitalUser user = new HpHospitalUser();
			user.setId(userId);
			user.setIfLoginable((byte) 0);
			user.setLoginunableReason(reason);
			hpHospitalUserMapper.updateByPrimaryKeySelective(user);

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

	@RequestMapping(value = "/createuser", method = RequestMethod.POST)
	public void createManager(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("用户名不可空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (password == null)
				throw new InteractRuntimeException("密码不可空");
			String ifAdminParam = StringUtils.trimToNull(request.getParameter("if_admin"));
			ifAdminParam = ifAdminParam == null ? "0" : "1";
			if (!ProjectUtils.equalsWithAny(ifAdminParam, "0", "1"))
				throw new InteractRuntimeException("if_admin:wrong value");
			byte ifAdmin = Byte.parseByte(ifAdminParam);

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			HpHospitalUser user = new HpHospitalUser();
			String userId = RandomStringUtils.randomNumeric(12);
			user.setId(userId);
			user.setHospitalId(loginStatus.getHospitalId());
			user.setRegisterTime(new Date().getTime());
			user.setUsername(username);
			user.setPassword(password);
			user.setPasswordMd5(DigestUtils.md5Hex(password));
			user.setIfLoginable((byte) 1);
			user.setIfSuperadmin((byte) 0);
			user.setIfAdmin(ifAdmin);
			hpHospitalUserMapper.insertSelective(user);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("userId", userId);
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
