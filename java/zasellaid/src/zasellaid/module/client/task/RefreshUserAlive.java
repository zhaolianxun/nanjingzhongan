package zasellaid.module.client.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import zasellaid.util.ZasellaidDataSource;

@Component
public class RefreshUserAlive {

	public static RefreshUserAlive bean;

	private static Logger logger = Logger.getLogger(RefreshUserAlive.class);
	private String sql = "update t_client_user set last_alive_time=rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0') where id in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private Vector<String> userIds = new Vector<String>();

	public RefreshUserAlive() {
		logger.info("启动任务队列 RefreshUserAlive");
		bean = this;
	}

	@Async
	public void run(String userId) {
		if (userIds.size() < 40) {
			userIds.add(userId);
			return;
		} else if (userIds.size() > 40) {
			return;
		}

		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = ZasellaidDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(sql);

			for (int i = 0; i < 40; i++) {
				String s = userIds.get(i);
				pst.setObject(i + 1, s);
			}
			pst.executeUpdate();
			userIds.clear();
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

	public static void main(String[] args) {
		List<String> userIds = new ArrayList<String>();
		userIds.add("1");
		userIds.add("2");
		userIds.add("3");
		userIds.add("2");
		userIds.add("3");

		List<String> s1 = userIds.subList(1, 3);
		System.out.println(s1);
		System.out.println(userIds.removeAll(s1));
		System.out.println(userIds);
	}
}
