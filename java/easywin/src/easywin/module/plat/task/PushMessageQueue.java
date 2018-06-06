package easywin.module.plat.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import easywin.constant.OutApis;
import easywin.constant.SysConstant;
import easywin.util.EasywinDataSource;
import okhttp3.Request;
import okhttp3.Response;

public class PushMessageQueue implements Runnable {

	private static Logger logger = Logger.getLogger(PushMessageQueue.class);

	// 创建队列 可接受SmsPayload类的任务 该队列是阻塞队列，也就是当没有任务的时候队列阻塞（也就是暂停）
	public static BlockingQueue<Payload> queue = new LinkedBlockingDeque<Payload>();

	public static void systemMsg(String userId, String phone, String title, String msg) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId, title, msg);
			payload.setPhone(phone);
			payload.business = "systemMsg";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	public static class Payload {
		private String userId;
		private String phone;
		private String title;
		private String content;
		private int failTimes = 0;
		private String business;

		public Payload(String userId, String title, String content) {
			super();
			this.userId = userId;
			this.title = title;
			this.content = content;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

	}

	static {
		logger.info("启动消息队列PushMessageQueue");
		new Thread(new PushMessageQueue(), "Thread-PushMessageQueue").start();
	}

	@Override
	public void run() {
		while (true) {
			Payload payload = null;
			Connection connection = null;
			PreparedStatement pst = null;
			try {
				// 从队列中取一个任务 如果队列中没有任务就在take方法中阻塞
				payload = queue.take();
				logger.debug("发现新的消息发送送任务");
				connection = EasywinDataSource.dataSource.getConnection();
				if ((payload.phone == null || payload.phone.isEmpty()) && (payload.userId != null )) {
					pst = connection.prepareStatement("select phone from t_user where id=?");
					pst.setObject(1, payload.userId);
					ResultSet rs = pst.executeQuery();
					if (rs.next()) {
						payload.phone = rs.getString("phone");
					} else
						throw new RuntimeException("用户不存在");
					pst.close();
				}

				// 短信校验
				if (payload.phone != null && !payload.phone.isEmpty()) {
					String url = new StringBuilder(OutApis.sms_sms_sendtemplate).append("?").append("phone=")
							.append(payload.phone).append("&contents=").append(payload.content).append("&business=")
							.append(payload.business).append("&client=easywin").toString();
					Request okHttpRequest = new Request.Builder().url(url).build();
					Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
					String responseBody = okHttpResponse.body().string();
					okHttpResponse.close();
					JSONObject resultVo = JSON.parseObject(responseBody);
					if (resultVo.getInteger("code") != 0)
						logger.info("短信发送失败 : " + resultVo.getString("codeMsg"));
				}
			} catch (Exception e) {
				logger.info(ExceptionUtils.getStackTrace(e));
				if (payload != null) {
					if (payload.failTimes < 3) {
						payload.failTimes++;
						try {
							queue.put(payload);
						} catch (InterruptedException e1) {
							logger.info(ExceptionUtils.getStackTrace(e1));
						}
					}
				}
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
		}
	}

	public static void main(String[] args) throws APIConnectionException, APIRequestException {

	}
}
