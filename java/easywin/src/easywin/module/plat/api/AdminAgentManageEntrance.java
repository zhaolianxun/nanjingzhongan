package easywin.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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

@Controller("plat.api.AdminAgentManageEntrance")
@RequestMapping(value = "/p/e/agentmanage")
public class AdminAgentManageEntrance {

	public static Logger logger = Logger.getLogger(AdminAgentManageEntrance.class);

	@RequestMapping(value = "/agents")
	public void templates(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select agent_is,agent_coin,id,phone,nickname,realname from t_user where 1=1 and agent_is=1 "
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
				item.put("agentCoin", rs.getObject("agent_coin"));
				item.put("agentIs", rs.getObject("agent_is"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject agents = new JSONObject();
			agents.put("items", items);
			data.put("agents", agents);
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

	@RequestMapping(value = "/addagentcoin")
	public void addAgentCoin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");
			String coinParam = StringUtils.trimToNull(request.getParameter("coin"));
			if (coinParam == null)
				throw new InteractRuntimeException("coin 不能为空");
			Integer coin = Integer.parseInt(coinParam);
			if (coin <= 0)
				throw new InteractRuntimeException("coin 必须大于0");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢订单列表
			pst = connection.prepareStatement("update t_user set agent_coin=agent_coin+? where id=? and agent_is=1");
			pst.setObject(1, coin);
			pst.setObject(2, userId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("用户不存在或用户不是代理");
			pst.close();

			pst = connection
					.prepareStatement("insert into t_agent_coin_bill (user_id,amount,source,time) values(?,?,?,?)");
			pst.setObject(1, userId);
			pst.setObject(2, coin);
			pst.setObject(3, "管理员操作");
			pst.setObject(4, new Date().getTime());
			n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/subagentcoin")
	public void subAgentCoin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String userId = StringUtils.trimToNull(request.getParameter("user_id"));
			if (userId == null)
				throw new InteractRuntimeException("user_id 不能为空");
			String coinParam = StringUtils.trimToNull(request.getParameter("coin"));
			if (coinParam == null)
				throw new InteractRuntimeException("coin 不能为空");
			Integer coin = Integer.parseInt(coinParam);
			if (coin <= 0)
				throw new InteractRuntimeException("coin 必须大于0");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢订单列表
			pst = connection.prepareStatement(
					"update t_user set agent_coin=agent_coin-? where id=? and agent_is=1 and agent_coin-? >= 0");
			pst.setObject(1, coin);
			pst.setObject(2, userId);
			pst.setObject(3, coin);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("用户不存在或用户不是代理或余额不足");
			pst.close();

			pst = connection
					.prepareStatement("insert into t_agent_coin_bill (user_id,amount,source,time) values(?,?,?,?)");
			pst.setObject(1, userId);
			pst.setObject(2, 0 - coin);
			pst.setObject(3, "管理员操作");
			pst.setObject(4, new Date().getTime());
			n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
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