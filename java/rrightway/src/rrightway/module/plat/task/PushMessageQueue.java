package rrightway.module.plat.task;

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
import okhttp3.Request;
import okhttp3.Response;
import rrightway.constant.OutApis;
import rrightway.constant.SysConstant;
import rrightway.util.RrightwayDataSource;

public class PushMessageQueue implements Runnable {

	private static Logger logger = Logger.getLogger(PushMessageQueue.class);

	// 创建队列 可接受SmsPayload类的任务 该队列是阻塞队列，也就是当没有任务的时候队列阻塞（也就是暂停）
	public static BlockingQueue<Payload> queue = new LinkedBlockingDeque<Payload>();

	public static void buyerBeChecked(String userId, String phone, String orderId) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您的订单（")
							.append(orderId.length() < 5 ? "" : orderId.substring(orderId.length() - 5))
							.append("）已经核对，收货后请及时提交评论图。").toString(),
					2, new StringBuilder("您的订单已经核对，收货后请及时提交评论图。订单ID：").append(orderId).toString());
			payload.setPhone(phone);
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	public static class Payload {
		private String userId;
		private String phone;
		private String title;
		private int type;// 1系统消息 2买家消息 3卖家消息 4警告消息
		private String content;
		private int failTimes = 0;

		public Payload(String userId, String title, int type, String content) {
			super();
			this.userId = userId;
			this.title = title;
			this.type = type;
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
				connection = RrightwayDataSource.dataSource.getConnection();
				if (payload.phone == null || payload.phone.isEmpty()) {
					pst = connection.prepareStatement("select phone from t_user where id=?");
					pst.setObject(1, payload.userId);
					ResultSet rs = pst.executeQuery();
					if (rs.next()) {
						payload.phone = rs.getString("phone");
					} else
						throw new RuntimeException("用户不存在");
					pst.close();
				}
				pst = connection.prepareStatement(
						"insert into t_message (user_id,title,content,type,send_time) values(?,?,?,?,?)");
				pst.setObject(1, payload.userId);
				pst.setObject(2, payload.title);
				pst.setObject(3, payload.content);
				pst.setObject(4, payload.type);
				pst.setObject(5, new Date().getTime());
				int cnt = pst.executeUpdate();
				if (cnt != 1) {
					payload.failTimes++;
					queue.put(payload);
					logger.info("消息插入失败");
				}

				// 短信校验
				String url = new StringBuilder(OutApis.sms_sms_send).append("?").append("phone=").append(payload.phone)
						.append("&content=").append(payload.content).append("&account=JSM4103805").toString();
				Request okHttpRequest = new Request.Builder().url(url).build();
				Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
				String responseBody = okHttpResponse.body().string();
				okHttpResponse.close();
				JSONObject resultVo = JSON.parseObject(responseBody);
				if (resultVo.getInteger("code") != 0)
					logger.info("短信发送失败 : " + resultVo.getString("codeMsg"));
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
		}
	}

	public static void main(String[] args) throws APIConnectionException, APIRequestException {

	}
}