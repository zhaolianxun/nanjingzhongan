package rrightway.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("plat.api.AgentAppManageEntrance")
@RequestMapping(value = "/p/e/agent/appmanage")
public class AgentAppManageEntrance {

	public static Logger logger = Logger.getLogger(AgentAppManageEntrance.class);

	@RequestMapping(value = "/apps")
	public void apps(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			if (phone != null)
				sqlParams.add(phone);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select case when v.agent_level=1 then u.agent1_price when v.agent_level=2 then u.agent2_price when v.agent_level=3 then u.agent3_price end agent_price,t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t left join t_app_seed u on t.seed_id=u.id left join t_user v on v.id=t.user_id where 1=1 and from_agent_id=?"
							+ (phone == null ? "" : " and phone=?") + " order by t.bind_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			ResultSet rs = pst.executeQuery();
			JSONArray apps = new JSONArray();
			while (rs.next()) {
				JSONObject app = new JSONObject();
				app.put("appId", rs.getObject("id"));
				app.put("headImg", rs.getObject("head_img"));
				app.put("seedId", rs.getObject("seed_id"));
				app.put("nickName", rs.getObject("nick_name"));
				app.put("currentTemplateVersion", rs.getObject("current_template_version"));
				app.put("authorized", rs.getObject("authorized"));
				app.put("auditTemplateVersion", rs.getObject("audit_template_version"));
				app.put("commitTemplateVersion", rs.getObject("commit_template_version"));
				app.put("bindTime", rs.getObject("bind_time"));
				app.put("commitStatus", rs.getObject("commit_status"));
				app.put("auditStatus", rs.getObject("audit_status"));
				app.put("auditFailReason", rs.getObject("audit_fail_reason"));
				app.put("newestTemplateVersion", rs.getObject("newest_template_version"));
				app.put("useEndtime", rs.getObject("use_endtime"));
				app.put("templateCode", rs.getObject("template_code"));
				app.put("agentPrice", rs.getObject("agent_price"));
				apps.add(app);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("apps", apps);
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

	@RequestMapping(value = "/grantappusetime")
	public void grantAppUseTime(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getAgentIs() != 1)
				throw new InteractRuntimeException("您不是代理");

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement(
					"select u.name seed_name,v.phone user_phone,t.nick_name app_name,t.use_endtime,u.agent1_price,u.agent2_price,u.agent3_price from t_app t left join t_app_seed u on t.seed_id=u.id left join t_user v on t.user_id=v.id where t.id=?");
			pst.setObject(1, appId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("应用不存在");
			}
			Long useEndtime = (Long) rs.getObject("use_endtime");
			String appName = rs.getString("app_name");
			String userPhone = rs.getString("user_phone");
			String seedName = rs.getString("seed_name");
			int agent1Price = rs.getInt("agent1_price");
			int agent2Price = rs.getInt("agent2_price");
			int agent3Price = rs.getInt("agent3_price");
			pst.close();
			long newUseEndtime = 0;
			long now = new Date().getTime();
			if (useEndtime == null || useEndtime <= now)
				newUseEndtime = now + 365 * 24 * 60 * 60 * 1000l;
			else
				newUseEndtime = useEndtime + 365 * 24 * 60 * 60 * 1000l;

			pst = connection.prepareStatement("select agent_level,agent_coin from t_user where id=? for update");
			pst.setObject(1, loginStatus.getUserId());
			rs = pst.executeQuery();
			rs.next();
			int agentCoin = rs.getInt("agent_coin");
			int agentLevel = rs.getInt("agent_level");
			pst.close();

			int agentPrice = 0;
			if (agentLevel == 1)
				agentPrice = agent1Price;
			if (agentLevel == 2)
				agentPrice = agent2Price;
			if (agentLevel == 3)
				agentPrice = agent3Price;
			if (agentCoin < agentPrice)
				throw new InteractRuntimeException("余额不足");

			// 查詢订单列表
			pst = connection.prepareStatement("update t_app set use_endtime=? where id=?");
			pst.setObject(1, newUseEndtime);
			pst.setObject(2, appId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

			pst = connection
					.prepareStatement("update t_user set agent_coin=agent_coin-? where id=? and agent_coin-? >= 0");
			pst.setObject(1, agentPrice);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, agentPrice);
			n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

			pst = connection
					.prepareStatement("insert into t_agent_coin_bill (user_id,amount,source,time) values(?,?,?,?)");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, agentPrice);
			pst.setObject(3, new StringBuilder("给").append(userPhone).append("的'").append(seedName).append(":")
					.append(appName).append("'发放1年使用时间").toString());
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