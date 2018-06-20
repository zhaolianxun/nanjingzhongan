package cszyylxm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cszyylxm.constant.OutApis;
import cszyylxm.constant.SysConstant;
import cszyylxm.constant.SysParam;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.CszyylxmDataSource;
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
					MainInitiator.class.getClassLoader().getResourceAsStream("cszyylxm/config/mainConfig.properties"));
			logger.info("-------初始化cszyylxm项目-------");
			logger.info("【系统常量初始化】");
			initConstant();
			logger.info("【JedisPool初始化】");
			initJedisPool();
			logger.info("【错误码初始化】");
			initErrorCode();
			logger.info("【jdbc数据源】");
			CszyylxmDataSource.setupDataSource(SysConstant.jdbc_driver, SysConstant.jdbc_url, SysConstant.jdbc_username,
					SysConstant.jdbc_password);
			// logger.info("【初始化系统参数】");
			// initSysParam();
			logger.info("-------初始化cszyylxm项目结束-------");
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

	public void initSysParam() throws Exception {
		Connection connection = null;
		Statement st = null;
		try {
			connection = CszyylxmDataSource.dataSource.getConnection();
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("select code,value from t_sysparam");

			while (rs.next()) {
				String code = rs.getString("code");
				String value = rs.getString("value");
			}
			st.close();

			if (SysConstant.project_environment.equals("test") || SysConstant.project_environment.equals("develop")) {

			} else if (SysConstant.project_environment.equals("product")) {

			}
			// 返回结果
		} catch (Exception e) {
			throw e;
		} finally {
			// 释放资源
			if (st != null)
				st.close();
			if (connection != null)
				connection.close();
		}
	}

	private void initConstant() throws UnsupportedEncodingException, IOException {
		InputStream in = MainInitiator.class.getClassLoader()
				.getResourceAsStream("cszyylxm/config/mainConfig.properties");
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

		SysConstant.ali_open_app_cszyylxm_appkey = StringUtils
				.trimToNull(properties.getProperty("ali.open.app.cszyylxm.appkey"));
		SysConstant.ali_open_app_cszyylxm_appsecret = StringUtils
				.trimToNull(properties.getProperty("ali.open.app.cszyylxm.appsecret"));
		SysConstant.ali_open_oauth_url = StringUtils.trimToNull(properties.getProperty("ali.open.oauth.url"));
		SysConstant.ali_open_gettoken_url = StringUtils.trimToNull(properties.getProperty("ali.open.gettoken.url"));

		SysConstant.project_domain = StringUtils.trimToNull(properties.getProperty("project.domain"));
		SysConstant.project_rooturl = StringUtils.trimToNull(properties.getProperty("project.rooturl"));
		SysConstant.project_ossroot = StringUtils.trimToNull(properties.getProperty("project.ossroot"));
		SysConstant.project_name = StringUtils.trimToNull(properties.getProperty("project.name"));

		// 外部接口初始化
		OutApis.sms_verification_verify = StringUtils
				.trimToNull(properties.getProperty("outapi.sms.verification.verify"));
		OutApis.sms_sms_send = StringUtils.trimToNull(properties.getProperty("outapi.sms.sms.send"));
		OutApis.sms_sms_sendtemplate = StringUtils.trimToNull(properties.getProperty("outapi.sms.sms.sendtemplate"));

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