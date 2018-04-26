package passion.module.client.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

import passion.dao.entity.ClientUser;
import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("controller.client.UserManage")
@RequestMapping(value = "/c/usermanage")
public class UserManage extends BaseController {

	public static Logger logger = Logger.getLogger(UserManage.class);
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManagementMapper;
	@Autowired
	protected AppShopEntranceMapper appShopModuleMapper;
	@Autowired
	protected MyMiniappEntranceMapper myMiniappMapper;
	@Autowired
	protected UserManageMapper userManageMapper;

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public void users(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取并处理参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			Long totalItemCount = userManageMapper.userCount();
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> users = userManageMapper.users(pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject usersJson = new JSONObject();
			usersJson.put("items", users);
			usersJson.put("pageNo", pagingPage.getPageNo());
			usersJson.put("pageSize", pagingPage.getPageSize());
			usersJson.put("totalItemCount", pagingPage.getTotalItemCount());
			usersJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("users", usersJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/alteruser", method = RequestMethod.POST)
	public void alterUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id不可空");

			String username = StringUtils.trim(request.getParameter("username"));
			if (username == "")
				throw new InteractRuntimeException("username不可空");

			String phone = StringUtils.trim(request.getParameter("phone"));
			if (phone == "")
				throw new InteractRuntimeException("phone不可空");

			String password = StringUtils.trim(request.getParameter("password"));
			if (password == "")
				throw new InteractRuntimeException("password不可空");

			String nickname = StringUtils.trim(request.getParameter("nickname"));

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			ClientUser user = clientUserMapper.selectByPrimaryKey(userId);

			if (phone != null && !phone.equals(user.getPhone())) {
				if (userManageMapper.phoneCheck(phone) > 0)
					throw new InteractRuntimeException("手机号已存在");
			}
			if (username != null && !phone.equals(user.getUsername())) {
				if (userManageMapper.usernameCheck(username) > 0)
					throw new InteractRuntimeException("用户名已存在");
			}

			user.setId(userId);
			user.setUsername(username);
			user.setPhone(phone);
			user.setPassword(password);
			if (password != null)
				user.setPasswordMd5(DigestUtils.md5Hex(password));
			user.setNickname(nickname);
			clientUserMapper.updateByPrimaryKeySelective(user);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

	@RequestMapping(value = "/createuser", method = RequestMethod.POST)
	public void createUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (username == null && phone == null)
				throw new InteractRuntimeException("username和phone不可都为空");
			String password = StringUtils.trimToNull(request.getParameter("password"));
			if (username != null && password == null)
				throw new InteractRuntimeException("密码不可空");
			String nickname = StringUtils.trimToNull(request.getParameter("nickname"));

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			if (phone != null) {
				if (userManageMapper.phoneCheck(phone) > 0)
					throw new InteractRuntimeException("手机号已存在");
			}
			if (username != null) {
				if (userManageMapper.usernameCheck(username) > 0)
					throw new InteractRuntimeException("用户名已存在");
			}

			ClientUser user = new ClientUser();
			String userId = RandomStringUtils.randomNumeric(12);
			user.setId(userId);
			user.setRegisterTime(new Date().getTime());
			user.setUsername(username);
			user.setPhone(phone);
			user.setPassword(password);
			if (password != null)
				user.setPasswordMd5(DigestUtils.md5Hex(password));
			user.setIfLoginable((byte) 1);
			user.setIfSuperadmin((byte) 0);
			user.setIfAdmin((byte) 0);
			user.setIfAgent((byte) 0);
			user.setNickname(nickname);
			clientUserMapper.insertSelective(user);

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
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfSuperadmin() != 1)
				throw new InteractRuntimeException("您不是超级管理员");

			ClientUser user = new ClientUser();
			user.setId(userId);
			user.setIfLoginable((byte) 0);
			user.setLoginunableReason(reason);
			clientUserMapper.updateByPrimaryKeySelective(user);

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

	@RequestMapping(value = "/phonecheck", method = RequestMethod.POST)
	public void phoneCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");

			// TODO 业务处理
			int ifExist = 0;
			if (userManageMapper.phoneCheck(phone) > 0)
				ifExist = 1;

			JSONObject data = new JSONObject();
			data.put("ifExist", ifExist);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/usernamecheck", method = RequestMethod.POST)
	public void usernameCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String username = StringUtils.trimToNull(request.getParameter("username"));
			if (username == null)
				throw new InteractRuntimeException("username不可空");

			// TODO 业务处理
			int ifExist = 0;
			if (userManageMapper.usernameCheck(username) > 0)
				ifExist = 1;

			JSONObject data = new JSONObject();
			data.put("ifExist", ifExist);

			// TODO 返回结果
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
