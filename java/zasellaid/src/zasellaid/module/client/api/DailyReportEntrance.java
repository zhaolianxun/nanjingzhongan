package zasellaid.module.client.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zasellaid.entity.InteractRuntimeException;
import zasellaid.module.client.business.GetLoginStatus;
import zasellaid.module.client.entity.UserLoginStatus;
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;
import zasellaid.util.ZasellaidDataSource;

@Controller("controller.client.DailyReportEntrance")
@RequestMapping(value = "/c/dailyreport")
public class DailyReportEntrance {

	public static Logger logger = Logger.getLogger(DailyReportEntrance.class);

	@RequestMapping(value = "/writetoday", method = RequestMethod.POST)
	public void addGroupMember(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String yesterdaySummary = StringUtils.trimToNull(request.getParameter("yesterday_summary"));
			if (yesterdaySummary == null)
				throw new InteractRuntimeException("yesterday_summary can`t be empty.");
			String todayGoal = StringUtils.trimToNull(request.getParameter("today_goal"));
			if (todayGoal == null)
				throw new InteractRuntimeException("today_goal can`t be empty.");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"insert into t_daily_report (user_id,yesterday_summary,today_goal,date,add_time) values(?,?,?,now(),rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0'))",
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, yesterdaySummary);
			pst.setObject(3, todayGoal);
			int n = pst.executeUpdate();
			int dailyReportId = 0;
			if (n != 1) {
				pst.close();
				throw new InteractRuntimeException("操作失败");
			} else {
				ResultSet rs = pst.getGeneratedKeys();
				rs.next();
				dailyReportId = rs.getInt(1);
				pst.close();
			}
			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("dailyReportId", dailyReportId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null && !pst.isClosed())
				pst.close();
			if (connection != null)
				connection.close();

		}
	}

	@RequestMapping(value = "/lookreport")
	public void lookReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reportIdParam = StringUtils.trimToNull(request.getParameter("report_id"));
			if (reportIdParam == null)
				throw new InteractRuntimeException("report_id 不能为空");
			int reportId = Integer.parseInt(reportIdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = ZasellaidDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select distinct admin_realname from t_daily_report_adminlook where report_id=? order by look_time desc");
			pst.setObject(1, reportId);
			ResultSet rs = pst.executeQuery();
			JSONArray adminLookerNames = new JSONArray();
			while (rs.next()) {
				adminLookerNames.add(rs.getObject("admin_realname"));
			}

			JSONObject data = new JSONObject();
			data.put("adminLookerNames", adminLookerNames);
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
	
	@RequestMapping(value = "/dailyreports")
	public void dailyReports(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0 || pageSize > 300)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = ZasellaidDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,yesterday_summary,today_goal,date,superior_review from t_daily_report where user_id=? order by add_time desc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("dailyReportId", rs.getObject("id"));
				item.put("superiorReview", rs.getObject("superior_review"));
				item.put("yesterdaySummary", rs.getObject("yesterday_summary"));
				item.put("todayGoal", rs.getObject("today_goal"));
				item.put("date", rs.getString("date"));
				items.add(item);
			}
			pst.close();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("reports", items);
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
}
