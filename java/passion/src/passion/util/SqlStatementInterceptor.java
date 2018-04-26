package passion.util;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Logger;

@Intercepts({
		@Signature(type = Executor.class, method = "update", args = {
				MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = {
				MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }) })
public class SqlStatementInterceptor implements Interceptor {
	private static Logger logger = Logger
			.getLogger(SqlStatementInterceptor.class);

	@Override
	public Object intercept(Invocation arg0) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement) arg0.getArgs()[0];
		String sqlId = mappedStatement.getId();
		Object returnValue;
		long start = System.currentTimeMillis();
		returnValue = arg0.proceed();
		long end = System.currentTimeMillis();

		long time = end - start;

		StringBuilder str = new StringBuilder(100);
		str.append(sqlId);
		str.append(": ");
		str.append("cost time ");
		str.append(time);
		str.append(" ms. ");
		String sqlInfo = str.toString();
		logger.debug(sqlInfo);

		return returnValue;
	}

	@Override
	public Object plugin(Object arg0) {
		return Plugin.wrap(arg0, this);
	}

	@Override
	public void setProperties(Properties arg0) {

	}
}