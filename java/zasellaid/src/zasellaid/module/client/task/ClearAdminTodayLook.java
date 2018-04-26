package zasellaid.module.client.task;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import zasellaid.constant.SysConstant;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.util.ZasellaidDataSource;

@Component
public class ClearAdminTodayLook {

	private static Logger logger = Logger.getLogger(ClearAdminTodayLook.class);

	public ClearAdminTodayLook() {
		logger.info("启动定时任务 DailyReportReminder");
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void run() {
		logger.debug("执行 task.ClearAdminTodayLook 定时任务");
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(
					"update t_client_user set admin_look_report_today=null where admin_look_report_today is not null");
			pst.executeUpdate();
		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
		}
		logger.debug("执行结束 task.ClearAdminTodayLook 定时任务");
	}

	public static void main(String[] args) throws IOException {

		// http://121.40.168.181:8082/push/push/message
		String a = "{}";

		String url = new StringBuilder(
				"http://121.40.168.181:8082/push/push/message?client=ycc&aliases=123&title=123&content=123&qq="
						+ URLEncoder.encode(a)).toString();
		Request okHttpRequest = new Request.Builder().url(url).build();
		Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
		if (okHttpResponse.isSuccessful()) {
			logger.info("ff");
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getInteger("code") != 0)
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));
		} else {
			logger.info("ff1");
		}
	}
}
