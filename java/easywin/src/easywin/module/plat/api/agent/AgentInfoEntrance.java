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

import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("plat.api.agent.AgentInfoEntrance")
@RequestMapping(value = "/p/e/agent/agentinfoent")
public class AgentInfoEntrance {

	public static Logger logger = Logger.getLogger(AgentInfoEntrance.class);

	@RequestMapping(value = "/ent")
	public void ent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAgentIs() != 1)
				throw new InteractRuntimeException("您不是代理");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement("select agent_coin,agent_level,agent_domain from t_user where id=? ");
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			while (rs.next()) {
				item.put("agentCoin", rs.getObject("agent_coin"));
				item.put("agentLevel", rs.getObject("agent_level"));
				item.put("agentDomain", rs.getObject("agent_domain"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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
			String agentIsParam = StringUtils.trimToNull(request.getParameter("agent_is"));
			Integer agentIs = agentIsParam == null ? null : Integer.parseInt(agentIsParam);

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
			if (agentIs != null) {
				sqlParams.add(agentIs);
			}
			sqlParams.add(userId);
			sqlParams.add(loginStatus.getUserId());
			pst = connection.prepareStatement("update t_user set id=id" + (password == null ? "" : " ,password=?")
					+ (password == null ? "" : " ,password_md5=?") + (realname == null ? "" : " ,realname=?")
					+ (nickname == null ? "" : " ,nickname=?") + (phone == null ? "" : " ,phone=?")
					+ (agentIs == null ? "" : " ,agent_is=?") + " where id=? and from_agent_id=?");
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