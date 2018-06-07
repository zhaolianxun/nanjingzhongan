package rrightway.module.plat.task;

import java.math.BigDecimal;
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

	public static void systemMsg(String userId, String phone, String title, String msg) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId, title, 0, msg);
			payload.setPhone(phone);
			payload.smsContents = msg;
			payload.business = "systemMsg";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	public static void sellerMsg(String userId, String phone, String title, String msg) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId, title, 1, msg);
			payload.setPhone(phone);
			payload.smsContents = msg;
			payload.business = "systemMsg";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	public static void buyerMsg(String userId, String phone, String title, String msg) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId, title, 2, msg);
			payload.setPhone(phone);
			payload.smsContents = msg;
			payload.business = "systemMsg";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	public static void warningMsg(String userId, String phone, String title, String msg) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId, title, 3, msg);
			payload.setPhone(phone);
			payload.smsContents = msg;
			payload.business = "systemMsg";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0002
	public static void topupComplete(String userId, String phone, BigDecimal amount) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您已充值成功（").append(amount.toString()).append("元）。").toString(), 0,
					new StringBuilder("您已充值成功（").append(amount.toString()).append("元）。").toString());
			payload.setPhone(phone);
			payload.smsContents = amount.toString();
			payload.business = "topupComplete";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0004
	public static void topupFail(String userId, String phone, String reason) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您的充值失败，请查看。").toString(), 0,
					new StringBuilder("您的充值失败，原因：").append(reason).toString());
			payload.setPhone(phone);
			payload.smsContents = reason;
			payload.business = "topupFail";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0003
	public static void orderBeCheckedToBuyer(String buyerId, String phone, String orderId) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(buyerId,
					new StringBuilder("您的订单（尾号")
							.append(orderId.length() < 5 ? "" : orderId.substring(orderId.length() - 5))
							.append("），商户已核对，收货后请及时提交评论图。").toString(),
					2, new StringBuilder("您的订单，商户已核对，收货后请及时提交评论图。订单ID：").append(orderId).toString());
			payload.setPhone(phone);
			payload.smsContents = orderId;
			payload.business = "orderBeCheckedToBuyer";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0005
	public static void newApplyToSeller(String sellerId, String phone, String activityTitle) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(sellerId,
					new StringBuilder("您有新的订单（活动：").append(activityTitle).append("）").toString(), 1,
					new StringBuilder("您有新的订单（活动：").append(activityTitle).append("）").toString());
			payload.setPhone(phone);
			payload.smsContents = activityTitle;
			payload.business = "newApplyToSeller";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0006
	public static void freezeUser(String userId, String phone, String reason) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您的账号被冻结，原因：").append(reason).toString(), 0,
					new StringBuilder("您的账号被冻结，原因：").append(reason).toString());
			payload.setPhone(phone);
			payload.smsContents = reason;
			payload.business = "freezeUser";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0007
	public static void adminAlterPhone(String userId, String phone) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您的手机号已被重置为：").append(phone).toString(), 0,
					new StringBuilder("您的手机号已被重置为：").append(phone).toString());
			payload.setPhone(phone);
			payload.smsContents = phone;
			payload.business = "adminAlterPhone";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0008
	public static void unfreezeUser(String userId, String phone) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(userId,
					new StringBuilder("您的账号已解冻。").toString(), 0, new StringBuilder("您的账号已解冻。").toString());
			payload.setPhone(phone);
			payload.business = "unfreezeUser";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0009
	public static void complainWarningToSeller(String sellerId, String phone, String orderId) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(sellerId,
					new StringBuilder("试客对您的投诉已审核通过，请注意。订单尾号：")
							.append(orderId.length() < 5 ? "" : orderId.substring(orderId.length() - 5)).toString(),
					0, new StringBuilder("试客对您的投诉已审核通过，请注意。订单号：").append(orderId).toString());
			payload.setPhone(phone);
			payload.smsContents = orderId;
			payload.business = "complainWarningToSeller";
			PushMessageQueue.queue.put(payload);
		} catch (InterruptedException e) {
			logger.info("消息插入失败");
		}
	}

	// w JSM42444-0010
	public static void uploadTaobaoOrderIdToSeller(String sellerId, String phone, String orderId) {
		try {
			PushMessageQueue.Payload payload = new PushMessageQueue.Payload(sellerId,
					new StringBuilder("试客上传了淘单号，请及时核对。订单（")
							.append(orderId.length() < 5 ? "" : orderId.substring(orderId.length() - 5)).append("）")
							.toString(),
					1, new StringBuilder("试客上传了淘单号，请及时核对。订单（").append(orderId).append("）").toString());
			payload.setPhone(phone);
			payload.smsContents = orderId;
			payload.business = "uploadTaobaoOrderIdToSeller";
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
		private String smsContents;
		private int failTimes = 0;
		private String business;

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

				pst = connection.prepareStatement(
						"insert into t_message (user_id,title,content,type,send_time) values(?,?,?,?,?)");
				pst.setObject(1, payload.userId);
				pst.setObject(2, payload.title);
				pst.setObject(3, payload.content);
				pst.setObject(4, payload.type);
				pst.setObject(5, new Date().getTime());
				int cnt = pst.executeUpdate();
				pst.close();
				if (cnt != 1)
					throw new RuntimeException("消息插入失败");

				// 短信校验
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

					if (payload.phone != null && !payload.phone.isEmpty()) {
						String url = new StringBuilder(OutApis.sms_sms_sendtemplate).append("?").append("phone=")
								.append(payload.phone).append("&contents=").append(payload.smsContents)
								.append("&business=").append(payload.business).append("&client=rrightway").toString();
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
