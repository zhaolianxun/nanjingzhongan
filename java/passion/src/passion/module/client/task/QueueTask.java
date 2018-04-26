package passion.module.client.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class QueueTask implements Runnable {

	private static Logger logger = Logger.getLogger(QueueTask.class);
	private ExecutorService service = Executors.newSingleThreadExecutor();

	public static BlockingQueue<Payload> queue = new LinkedBlockingDeque<Payload>();

	public QueueTask() {
		service.execute(this);
	}

	public static class Payload {
		private boolean processSuccess = false;
		private int processCount = 0;
	}

	@Override
	public void run() {
		while (true) {
			Payload payload = null;
			try {
				payload = queue.take();
				logger.debug("发现新的队列任务");
				// TODO
				payload.processSuccess = true;
			} catch (Exception e) {
				logger.info(ExceptionUtils.getStackTrace(e));
			} finally {
				if (payload != null && !payload.processSuccess && ++payload.processCount < 10)
					try {
						queue.put(payload);
					} catch (InterruptedException e) {
						logger.info(ExceptionUtils.getStackTrace(e));
					}
			}
		}
	}
}
