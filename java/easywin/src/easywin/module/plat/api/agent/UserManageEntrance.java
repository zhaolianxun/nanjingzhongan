package easywin.module.plat.api.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("plat.api.agent.UserManageEntrance")
@RequestMapping(value = "/p/e/agent/usermanage")
public class UserManageEntrance {

	public static Logger logger = Logger.getLogger(UserManageEntrance.class);

	@RequestMapping(value = "/userinfo")
	public void userinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAgentIs() != 1)
				throw new InteractRuntimeException("您不是代理");
			connection = EasywinDataSource.dataSource.getConnection();

			// 查詢订单列表
			pst = connection.prepareStatement(
					"select u.agent_coin,u.agent_level,u.agent_domain,u.agent_is,u.id,u.phone,u.register_time,a.phone from_agent_phone from t_user u left join t_user a on u.from_agent_id=a.id where u.id=?");
			pst.setObject(1, userId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("userId", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("agentIs", rs.getObject("agent_is"));
				item.put("agentCoin", rs.getObject("agent_coin"));
				item.put("agentLevel", rs.getObject("agent_level"));
				item.put("agentDomain", rs.getObject("agent_domain"));
				item.put("fromAgentPhone", rs.getObject("from_agent_phone"));
				item.put("registerTime", rs.getObject("register_time"));
			}
			pst.close();

			JSONObject data = new JSONObject();
			data.putAll(item);
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/users")
	public void users(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAgentIs() != 1)
				throw new InteractRuntimeException("您不是代理");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select id,phone,nickname,realname,register_time from t_user where 1=1 and from_agent_id=? "
							+ (phone == null ? "" : " and phone like ? ") + " order by register_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("userId", rs.getObject("id"));
				item.put("phone", rs.getObject("phone"));
				item.put("nickname", rs.getObject("nickname"));
				item.put("realname", rs.getObject("realname"));
				item.put("registerTime", rs.getObject("register_time"));
				items.add(item);
			}
			pst.close();

			
			sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			pst = connection.prepareStatement("select count(id) total_count from t_user where 1=1 and from_agent_id=? "
					+ (phone == null ? "" : " and phone like ? "));
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			rs = pst.executeQuery();
			JSONObject sum = new JSONObject();
			if (rs.next()) {
				sum.put("count", rs.getObject("total_count"));
			}
			pst.close();
			
			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject users = new JSONObject();
			users.put("items", items);
			users.putAll(sum);
			data.put("users", users);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/alter")
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");
			String nickname = StringUtils.trimToNull(request.getParameter("nickname"));
			String realname = StringUtils.trimToNull(request.getParameter("realname"));
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String password = StringUtils.trimToNull(request.getParameter("password"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAgentIs() != 1)
				throw new InteractRuntimeException("您不是代理");
			connection = EasywinDataSource.dataSource.getConnection();

			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (password != null) {
				sqlParams.add(password);
				sqlParams.add(DigestUtils.md5Hex(password));
			}
			if (realname != null)
				sqlParams.add(realname);
			if (nickname != null)
				sqlParams.add(nickname);
			if (phone != null)
				sqlParams.add(phone);
			sqlParams.add(userId);
			sqlParams.add(loginStatus.getUserId());
			pst = connection.prepareStatement("update t_user set id=id" + (password == null ? "" : " ,password=?")
					+ (password == null ? "" : " ,password_md5=?") + (realname == null ? "" : " ,realname=?")
					+ (nickname == null ? "" : " ,nickname=?") + (phone == null ? "" : " ,phone=?")
					+ " where id=? and from_agent_id=?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("用户不存在/不是您的代理名下用户");
			pst.close();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

}