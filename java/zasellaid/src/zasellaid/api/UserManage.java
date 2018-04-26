package zasellaid.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import zasellaid.constant.SysConstant;
import zasellaid.dao.entity.ClientUser;
import zasellaid.dao.mapper.ClientUserMapper;
import zasellaid.dao.mapper.UserManageMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.entity.PagingPage;
import zasellaid.util.HttpRespondWithData;

@Controller("controller.UserManage")
@RequestMapping(value = "/um")
public class UserManage {

	public static Logger logger = Logger.getLogger(UserManage.class);

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public void users(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// 获取并处理参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			UserManageMapper userManageMapper = session.getMapper(UserManageMapper.class);

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
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/alteruser", method = RequestMethod.POST)
	public void alterUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id不可空");

			String phone = StringUtils.trim(request.getParameter("phone"));
			if (phone == "")
				throw new InteractRuntimeException("phone不可空");

			String password = StringUtils.trim(request.getParameter("password"));
			if (password == "")
				throw new InteractRuntimeException("password不可空");

			String realname = StringUtils.trim(request.getParameter("realname"));

			// TODO 业务处理
			String referer = request.getHeader("Referer");
			if (StringUtils.isEmpty(referer) || !referer.contains("zasellaid.njshangka.com/usermanage.html")) {
				throw new InteractRuntimeException("您没有权限");
			}
			// UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			// if (loginStatus == null)
			// throw new InteractRuntimeException(20);
			// if (loginStatus.getIfSuperadmin() != 1)
			// throw new InteractRuntimeException("您不是超级管理员");

			session = SysConstant.sqlSessionFactory.openSession(true);
			UserManageMapper userManageMapper = session.getMapper(UserManageMapper.class);
			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);

			ClientUser user = clientUserMapper.selectByPrimaryKey(userId);

			if (phone != null && !phone.equals(user.getPhone())) {
				if (userManageMapper.phoneCheck(phone) > 0)
					throw new InteractRuntimeException("手机号已存在");
			}

			user.setId(userId);
			user.setPhone(phone);
			user.setPassword(password);
			if (password != null)
				user.setPasswordMd5(DigestUtils.md5Hex(password));
			user.setRealname(realname);
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
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/createuser", method = RequestMethod.POST)
	public void createUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			throw new InteractRuntimeException("该功能已关闭，请联系管理员添加组员");
			
//			String phone = StringUtils.trimToNull(request.getParameter("phone"));
//			if (phone == null)
//				throw new InteractRuntimeException("phone不可为空");
//			String password = StringUtils.trimToNull(request.getParameter("password"));
//			if (password == null)
//				throw new InteractRuntimeException("密码不可空");
//			String realname = StringUtils.trimToNull(request.getParameter("realname"));
//
//			// TODO 业务处理
//			String referer = request.getHeader("Referer");
//			if (StringUtils.isEmpty(referer) || !referer.contains("zasellaid.njshangka.com/usermanage.html")) {
//				throw new InteractRuntimeException("您没有权限");
//			}
//			// UserLoginStatus loginStatus = GetLoginStatus.todo(request);
//			// if (loginStatus == null)
//			// throw new InteractRuntimeException(20);
//			// if (loginStatus.getIfSuperadmin() != 1)
//			// throw new InteractRuntimeException("您不是超级管理员");
//
//			session = SysConstant.sqlSessionFactory.openSession(true);
//			UserManageMapper userManageMapper = session.getMapper(UserManageMapper.class);
//			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);
//
//			if (phone != null) {
//				if (userManageMapper.phoneCheck(phone) > 0)
//					throw new InteractRuntimeException("手机号已存在");
//			}
//
//			ClientUser user = new ClientUser();
//			String userId = RandomStringUtils.randomNumeric(12);
//			user.setId(userId);
//			user.setRegisterTime(new Date().getTime());
//			user.setPhone(phone);
//			user.setPassword(password);
//			if (password != null)
//				user.setPasswordMd5(DigestUtils.md5Hex(password));
//			user.setIfLoginable(1);
//			user.setIfSuperadmin(0);
//			user.setIfAdmin(0);
//			user.setRealname(realname);
//			clientUserMapper.insertSelective(user);
//
//			// TODO 返回结果
//			JSONObject data = new JSONObject();
//			data.put("userId", userId);
//			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
			if (session != null)
				session.close();
		}

	}

	@RequestMapping(value = "/forbidlogin", method = RequestMethod.POST)
	public void forbidLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("userId不可空");
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason不可空");

			// TODO 业务处理
			String referer = request.getHeader("Referer");
			if (StringUtils.isEmpty(referer) || !referer.contains("zasellaid.njshangka.com/usermanage.html")) {
				throw new InteractRuntimeException("您没有权限");
			}
			// UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			// if (loginStatus == null)
			// throw new InteractRuntimeException(20);
			// if (loginStatus.getIfSuperadmin() != 1)
			// throw new InteractRuntimeException("您不是超级管理员");

			session = SysConstant.sqlSessionFactory.openSession(true);
			ClientUserMapper clientUserMapper = session.getMapper(ClientUserMapper.class);

			ClientUser user = new ClientUser();
			user.setId(userId);
			user.setIfLoginable(0);
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
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/phonecheck", method = RequestMethod.POST)
	public void phoneCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			UserManageMapper userManageMapper = session.getMapper(UserManageMapper.class);

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
			if (session != null)
				session.close();
		}
	}

}
