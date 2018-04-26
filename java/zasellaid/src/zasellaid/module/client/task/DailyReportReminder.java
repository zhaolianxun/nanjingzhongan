package zasellaid.module.client.task;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zasellaid.constant.OutApis;
import zasellaid.constant.SysConstant;
import zasellaid.entity.InteractRuntimeException;

@Component
public class DailyReportReminder {

	private static Logger logger = Logger.getLogger(DailyReportReminder.class);

	public DailyReportReminder() {
		logger.info("启动定时任务 DailyReportReminder");
	}

	@Scheduled(cron = "0 0 9 * * MON-FRI")
	public void run() {
		logger.debug("执行 task.DailyReportReminder 定时任务");
		try {// "http://121.40.168.181:8082/push/push/messageall?"
			String url = new StringBuilder(OutApis.push_push_messageall).toString();
			String body = new StringBuilder("client=zasellaid").append("&title=忠安助手日报提醒")
					.append("&content=新的一天开始了，请点我记录您的工作内容。").append("&extras={action:\"1\"}").toString();
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body)).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("call out api：" + okHttpResponse.request().url() + "<--- " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			if (resultVo.getInteger("code") != 0)
				throw new InteractRuntimeException(resultVo.getString("codeMsg"));

		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		}
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
