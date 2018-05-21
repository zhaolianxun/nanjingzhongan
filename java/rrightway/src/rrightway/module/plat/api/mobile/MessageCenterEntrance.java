package rrightway.module.plat.api.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.MessageCenterEntrance")
@RequestMapping(value = "/p/m/msgcenterent")
public class MessageCenterEntrance {

	public static Logger logger = Logger.getLogger(MessageCenterEntrance.class);

	@RequestMapping(value = "/messages")
	public void messages(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String typeParam = StringUtils.trimToNull(request.getParameter("type"));
			Integer type = typeParam == null ? null : Integer.parseInt(typeParam);
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

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (type != null)
				sqlParams.add(type);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.title,right(t.content, 100) content,t.type,t.read_if,t.send_time from t_message t where 1=1 ")
							.append(type == null ? "" : " and t.type =? ")
							.append(" order by t.read_if asc,t.send_time desc limit ?,?").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("messageId", rs.getObject("id"));
				item.put("title", rs.getObject("title"));
				item.put("content", rs.getObject("content"));
				item.put("type", rs.getObject("type"));
				item.put("readIf", rs.getObject("read_if"));
				item.put("sendTime", rs.getObject("send_time"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject messages = new JSONObject();
			messages.put("items", items);
			data.put("messages", messages);
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

	@RequestMapping(value = "/messagedetail")
	public void messagedetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String messageIdParam = StringUtils.trimToNull(request.getParameter("message_id"));
			if (messageIdParam == null)
				throw new InteractRuntimeException("message_id 不能空");
			Integer messageId = Integer.parseInt(messageIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.title,t.content,t.type,t.read_if,t.send_time from t_message t where t.id=?")
							.toString());
			pst.setObject(1, messageId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("messageId", rs.getObject("id"));
				item.put("title", rs.getObject("title"));
				item.put("content", rs.getObject("content"));
				item.put("type", rs.getObject("type"));
				item.put("readFf", rs.getObject("read_if"));
				item.put("send_time", rs.getObject("send_time"));
			} else {
				throw new InteractRuntimeException("消息不存在");
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

	@RequestMapping(value = "/delete")
	public void delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String messageIdsParam = StringUtils.trimToNull(request.getParameter("message_ids"));
			if (messageIdsParam == null)
				throw new InteractRuntimeException("message_ids 不能空");
			String[] messageIds = messageIdsParam.split(",");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String s = "";
			for (int i = 0; i < messageIds.length; i++) {
				s = s + ",?";
			}
			s = s.substring(1);
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("delete from t_message where id in (" + s + ")");
			for (int i = 0; i < messageIds.length; i++) {
				pst.setString(i + 1, messageIds[i]);
			}
			pst.executeUpdate();

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

	@RequestMapping(value = "/signread")
	public void signread(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String messageIdsParam = StringUtils.trimToNull(request.getParameter("message_ids"));
			if (messageIdsParam == null)
				throw new InteractRuntimeException("message_ids 不能空");
			String[] messageIds = messageIdsParam.split(",");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String s = "";
			for (int i = 0; i < messageIds.length; i++) {
				s = s + ",?";
			}
			s = s.substring(1);
			connection = RrightwayDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("update t_message set read_if=1 where id in (" + s + ")");
			for (int i = 0; i < messageIds.length; i++) {
				pst.setString(i + 1, messageIds[i]);
			}
			pst.executeUpdate();

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