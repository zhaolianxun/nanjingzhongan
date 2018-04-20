package rrightway.util;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RrightwayDataSource {

	public static DataSource dataSource;

	public static void setupDataSource(String driver, String connectURI, String username, String password)
			throws ClassNotFoundException {
		Class.forName(driver);
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(
				poolableConnectionFactory);
		poolableConnectionFactory.setPool(connectionPool);

		dataSource = new PoolingDataSource<PoolableConnection>(connectionPool);
	}

	public static List<Map> setupDataSource(ResultSet rs) throws ClassNotFoundException {
		return null;
	}

	public static void main(String[] args) {

	}
}
