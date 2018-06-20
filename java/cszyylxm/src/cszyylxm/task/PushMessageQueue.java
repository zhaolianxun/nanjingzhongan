package cszyylxm.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import cszyylxm.util.CszyylxmDataSource;

public class PushMessageQueue implements Runnable {

	private static Logger logger = Logger.getLogger(PushMessageQueue.class);

	// 创建队列 可接受SmsPayload类的任务 该队列是阻塞队列，也就是当没有任务的时候队列阻塞（也就是暂停）
	public static BlockingQueue<Payload> queue = new LinkedBlockingDeque<Payload>();

	public static class Payload {

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
				connection = CszyylxmDataSource.dataSource.getConnection();
			} catch (Exception e) {

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

}
