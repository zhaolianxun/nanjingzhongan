package rrightway.module.plat.api;

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

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("plat.api.AdminUserManageEntrance")
@RequestMapping(value = "/p/e/usermanage")
public class AdminUserManageEntrance {

	public static Logger logger = Logger.getLogger(AdminUserManageEntrance.class);

	@RequestMapping(value = "/users")
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
			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(new StringBuilder("%").append(phone).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection
					.prepareStatement("select agent_is,agent_coin,id,phone,nickname,realname from t_user where 1=1 "
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
				item.put("agentIs", rs.getObject("agent_is"));
				item.put("agentCoin", rs.getObject("agent_coin"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject users = new JSONObject();
			users.put("items", items);
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
			String agentIsParam = StringUtils.trimToNull(request.getParameter("agent_is"));
			Integer agentIs = agentIsParam == null ? null : Integer.parseInt(agentIsParam);
			String agentLevelParam = StringUtils.trimToNull(request.getParameter("agent_level"));
			Integer agentLevel = agentLevelParam == null ? null : Integer.parseInt(agentLevelParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = RrightwayDataSource.dataSource.getConnection();

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
			if (agentIs != null)
				sqlParams.add(agentIs);
			if (agentLevel != null)
				sqlParams.add(agentLevel);

			sqlParams.add(userId);
			pst = connection.prepareStatement("update t_user set id=id" + (password == null ? "" : " ,password=?")
					+ (password == null ? "" : " ,password_md5=?") + (realname == null ? "" : " ,realname=?")
					+ (nickname == null ? "" : " ,nickname=?") + (phone == null ? "" : " ,phone=?")
					+ (agentIs == null ? "" : " ,agent_is=?") + (agentLevel == null ? "" : " ,agent_level=?")
					+ " where id=?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("用户不存在");
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