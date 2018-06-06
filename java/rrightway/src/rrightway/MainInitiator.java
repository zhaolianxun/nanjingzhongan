package rrightway;

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

import rrightway.constant.OutApis;
import rrightway.constant.SysConstant;
import rrightway.constant.SysParam;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;
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
					MainInitiator.class.getClassLoader().getResourceAsStream("rrightway/config/mainConfig.properties"));
			logger.info("-------初始化rrightway项目-------");
			logger.info("【系统常量初始化】");
			initConstant();
			logger.info("【JedisPool初始化】");
			initJedisPool();
			logger.info("【错误码初始化】");
			initErrorCode();
			logger.info("【jdbc数据源】");
			RrightwayDataSource.setupDataSource(SysConstant.jdbc_driver, SysConstant.jdbc_url,
					SysConstant.jdbc_username, SysConstant.jdbc_password);
			logger.info("【初始化系统参数】");
			initSysParam();
			logger.info("-------初始化rrightway项目结束-------");
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
			connection = RrightwayDataSource.dataSource.getConnection();
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("select code,value from t_sysparam");

			while (rs.next()) {
				String code = rs.getString("code");
				if (code.equals("service_qq")) {
					SysParam.service_qq = rs.getString("value");
				} else if (code.equals("alipay_receipt_qrcode")) {
					SysParam.alipay_receipt_qrcode = rs.getString("value");
				}
			}
			st.close();

			if (SysConstant.project_environment.equals("test") || SysConstant.project_environment.equals("develop")) {
				SysParam.reviewedExpiredReturnTime = 2 * 60 * 1000l;
				SysParam.unreviewedExpiredReturnTime = 15 * 60 * 1000l;
				SysParam.walletOutableTime = 2 * 60 * 1000l;
				SysParam.buyFromSameSellerInterval = 3 * 60 * 1000l;
				SysParam.autoConfirmRightProjectPeriod = 15 * 60 * 1000l;
				SysParam.cancelableOrderTime = 3 * 60 * 1000l;
			} else if (SysConstant.project_environment.equals("product")) {
				// 买家已提交评价图的自动返现时间，2天
				SysParam.reviewedExpiredReturnTime = 2 * 24 * 60 * 60 * 1000l;
				// 买家未提交评价图的自动返现时间，15天
				SysParam.unreviewedExpiredReturnTime = 15 * 24 * 60 * 60 * 1000l;
				// 钱包中的金额从不可转入余额到可转入的时间间隔，15天
				SysParam.walletOutableTime = 15 * 24 * 60 * 60 * 1000;
				// 从同一商家处购买商品间隔时间，30天
				SysParam.buyFromSameSellerInterval = 30 * 24 * 60 * 60 * 1000l;
				// 买家超出时间未处理，自动同意维权，2天
				SysParam.autoConfirmRightProjectPeriod = 2 * 24 * 60 * 60 * 1000l;
				// 订单可取消的时间，2小时
				SysParam.cancelableOrderTime = 2 * 60 * 60 * 1000l;
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
				.getResourceAsStream("rrightway/config/mainConfig.properties");
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

		SysConstant.ali_open_app_rrightway_appkey = StringUtils
				.trimToNull(properties.getProperty("ali.open.app.rrightway.appkey"));
		SysConstant.ali_open_app_rrightway_appsecret = StringUtils
				.trimToNull(properties.getProperty("ali.open.app.rrightway.appsecret"));
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
