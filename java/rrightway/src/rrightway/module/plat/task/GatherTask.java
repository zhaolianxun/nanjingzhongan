package rrightway.module.plat.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rrightway.util.RrightwayDataSource;

@Component
public class GatherTask {

	private static Logger logger = Logger.getLogger(GatherTask.class);

	public GatherTask() {
		logger.info("启动定时任务 RefreshAuthorizerAccessToken");
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void run() {
		logger.debug("执行 task.GatherTask 定时任务");
		Connection connection = null;
		PreparedStatement pst = null;
		try {

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 下架过期或卖完的活动
			pst = connection.prepareStatement(
					"update t_activity set status=3 where status=1 and and ((stock=0) or (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-(keep_days*24*60*60*1000+start_time))>0)");
			int cnt = pst.executeUpdate();
			logger.debug("下架了" + cnt + "个过期活动");
			pst.close();
			connection.commit();

//			// 买家超过2小时未处理，自动同意维权
//			pst = connection.prepareStatement(
//					"update t_order set status=12 where rightprotect_status=7 and status=1 and (rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')-(order_time))>2*60*60*1000");
//			cnt = pst.executeUpdate();
//			logger.debug("下架了" + cnt + "个过期活动");
//			pst.close();
			connection.commit();
		} catch (Exception e) {
			if (connection != null)
				try {
					connection.rollback();
				} catch (SQLException e1) {
					logger.info(ExceptionUtils.getStackTrace(e1));
				}
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
			logger.debug("执行结束 task.GatherTask 定时任务");
		}
	}

}
