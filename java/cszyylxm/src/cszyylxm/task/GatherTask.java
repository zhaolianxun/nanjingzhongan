package cszyylxm.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cszyylxm.constant.SysParam;
import cszyylxm.util.CszyylxmDataSource;
import cszyylxm.util.InteractRuntimeException;

@Component
public class GatherTask {

	private static Logger logger = Logger.getLogger(GatherTask.class);

	public GatherTask() {
		logger.info("启动定时任务 GatherTask");
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void run() {
		logger.debug("执行 task.GatherTask 定时任务");
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			connection = CszyylxmDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

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
