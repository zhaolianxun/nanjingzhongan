package passion.util;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class PassionDataSource {

	public static DataSource dataSource;

	public static void setupDataSource(String driver, String connectURI, String username, String password)
			throws ClassNotFoundException {
		Class.forName(driver);
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		poolableConnectionFactory.setValidationQuery("select 1");
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setTestWhileIdle(true);
		config.setTestOnBorrow(false);
		ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(
				poolableConnectionFactory, config);
		poolableConnectionFactory.setPool(connectionPool);
		dataSource = new PoolingDataSource<PoolableConnection>(connectionPool);
	}

}
