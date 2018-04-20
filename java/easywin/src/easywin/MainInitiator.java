package easywin;

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
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import easywin.constant.OutApis;
import easywin.constant.SysConstant;
import easywin.util.EasywinDataSource;
import redis.clients.jedis.JedisPool;

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
					MainInitiator.class.getClassLoader().getResourceAsStream("easywin/config/mainConfig.properties"));
			logger.info("-------初始化easywin项目-------");
			logger.info("【系统常量初始化】");
			initConstant();
			logger.info("【JedisPool初始化】");
			initJedisPool();
			logger.info("【错误码初始化】");
			initErrorCode();
			logger.info("【jdbc数据源】");
			EasywinDataSource.setupDataSource(SysConstant.jdbc_driver, SysConstant.jdbc_url, SysConstant.jdbc_username,
					SysConstant.jdbc_password);
			logger.info("-------初始化easywin项目结束-------");
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
				.getResourceAsStream("easywin/config/mainConfig.properties");
		Properties properties = new Properties();
		properties.load(new InputStreamReader(in, SysConstant.SYS_CHARSET));
		in.close();
		logger.debug(properties);
		SysConstant.REDIS_IP = StringUtils.trimToNull(properties.getProperty("redis.ip"));
		String redisPortParam = StringUtils.trimToNull(properties.getProperty("redis.port"));
		SysConstant.REDIS_PORT = redisPortParam == null ? null : Integer.parseInt(redisPortParam);
		SysConstant.REDIS_AUTH = StringUtils.trimToNull(properties.getProperty("redis.auth"));
		SysConstant.project_environment = properties.getProperty("project.environment");
		if (SysConstant.project_environment.equals("test")) {
			SysConstant.project_test = true;
		} else if (SysConstant.project_environment.equals("product")) {
			SysConstant.project_product = true;
		}
		SysConstant.jdbc_driver = properties.getProperty("jdbc.driver");
		SysConstant.jdbc_url = properties.getProperty("jdbc.url");
		SysConstant.jdbc_username = properties.getProperty("jdbc.username");
		SysConstant.jdbc_password = properties.getProperty("jdbc.password");

		// 微信小程序参数
		SysConstant.wechat_open_thirdparty_AppId = StringUtils
				.trimToNull(properties.getProperty("wechat.open.thirdparty.AppId"));
		SysConstant.wechat_open_thirdparty_AppSecret = StringUtils
				.trimToNull(properties.getProperty("wechat.open.thirdparty.AppSecret"));
		SysConstant.wechat_open_thirdparty_MsgVerificationToken = StringUtils
				.trimToNull(properties.getProperty("wechat.open.thirdparty.MsgVerificationToken"));
		SysConstant.wechat_open_thirdparty_MsgEncryptAndDecryptKey = StringUtils
				.trimToNull(properties.getProperty("wechat.open.thirdparty.MsgEncryptAndDecryptKey"));

		SysConstant.project_domain = StringUtils.trimToNull(properties.getProperty("project.domain"));
		SysConstant.project_rooturl = StringUtils.trimToNull(properties.getProperty("project.rooturl"));
		// 外部接口初始化
		OutApis.sms_verification_verify = StringUtils
				.trimToNull(properties.getProperty("outapi.sms.verification.verify"));
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
