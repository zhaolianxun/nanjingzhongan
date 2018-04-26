package zasellaid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import redis.clients.jedis.JedisPool;
import zasellaid.constant.OutApis;
import zasellaid.constant.SysConstant;
import zasellaid.util.ZasellaidDataSource;

/**
 * Application Lifecycle Listener implementation class
 * ServletContextListenerImpl
 *
 */
public class MainInitiator implements ServletContextListener {
	private static Logger logger = Logger.getLogger(MainInitiator.class);

	/**
	 * Default constructor.
	 */
	public MainInitiator() {
		// TODO Auto-generated constructor stub

	}

	/**
	 * 初始化顺序不能变
	 * 
	 * @see MainInitiator#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			PropertyConfigurator.configure(
					MainInitiator.class.getClassLoader().getResourceAsStream("zasellaid/config/mainConfig.properties"));
			logger.info("-------初始化zasellaid项目-------");
			logger.info("【系统常量初始化】");
			initConstant();
			logger.info("【系统常量初始化结束】");
			logger.info("【JedisPool初始化】");
			initJedisPool();
			logger.info("【JedisPool初始化结束】");
			logger.info("【错误码初始化】");
			initErrorCode();
			logger.info("【错误码初始化结束】");
			SysConstant.sqlSessionFactory = new SqlSessionFactoryBuilder()
					.build(Resources.getResourceAsStream("zasellaid/config/mybatis.xml"));
			ZasellaidDataSource.setupDataSource(SysConstant.jdbc_driver, SysConstant.jdbc_url,
					SysConstant.jdbc_username, SysConstant.jdbc_password);
			logger.info("-------初始化zasellaid项目结束-------");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initErrorCode() throws UnsupportedEncodingException, IOException {
		InputStream in = MainInitiator.class.getClassLoader().getResourceAsStream(SysConstant.errorCodeCnMapperPath);
		SysConstant.errorCodeCnMapper = new Properties();
		SysConstant.errorCodeCnMapper.load(new InputStreamReader(in, SysConstant.SYS_CHARSET));
		in.close();
		logger.debug(SysConstant.errorCodeCnMapper);

		in = MainInitiator.class.getClassLoader().getResourceAsStream(SysConstant.errorCodeEnMapperPath);
		SysConstant.errorCodeEnMapper = new Properties();
		SysConstant.errorCodeEnMapper.load(in);
		in.close();
		logger.debug(SysConstant.errorCodeEnMapper);
	}

	private void initConstant() throws UnsupportedEncodingException, IOException {
		InputStream in = MainInitiator.class.getClassLoader()
				.getResourceAsStream("zasellaid/config/mainConfig.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(in, SysConstant.SYS_CHARSET));
		in.close();
		logger.debug(properties);
		SysConstant.REDIS_IP = StringUtils.trimToNull(properties.getProperty("redis.ip"));
		String redisPortParam = StringUtils.trimToNull(properties.getProperty("redis.port"));
		SysConstant.REDIS_PORT = redisPortParam == null ? null : Integer.parseInt(redisPortParam);
		SysConstant.REDIS_AUTH = StringUtils.trimToNull(properties.getProperty("redis.auth"));
		SysConstant.project_environment = properties.getProperty("project.environment");
		if ("product".equals(SysConstant.project_environment)) {
			SysConstant.project_test = false;
			SysConstant.project_product = true;
		} else if ("test".equals(SysConstant.project_environment)) {
			SysConstant.project_test = true;
			SysConstant.project_product = false;
		}
		SysConstant.jdbc_driver = properties.getProperty("jdbc.driver");
		SysConstant.jdbc_url = properties.getProperty("jdbc.url");
		SysConstant.jdbc_username = properties.getProperty("jdbc.username");
		SysConstant.jdbc_password = properties.getProperty("jdbc.password");

		// 外部接口初始化
		OutApis.sms_verification_verify = StringUtils
				.trimToNull(properties.getProperty("outapi.sms.verification.verify"));
		OutApis.push_push_messageall = StringUtils.trimToNull(properties.getProperty("outapi.push.push.messageall"));
		OutApis.push_push_notification = StringUtils
				.trimToNull(properties.getProperty("outapi.push.push.notification"));

	}

	private void initJedisPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		SysConstant.jedisPool = new JedisPool(config, SysConstant.REDIS_IP, SysConstant.REDIS_PORT, 10000,
				SysConstant.REDIS_AUTH);
	}

	/**
	 * @see MainInitiator#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		if (SysConstant.jedisPool != null)
			SysConstant.jedisPool.close();
	}

}
