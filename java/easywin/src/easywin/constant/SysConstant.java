package easywin.constant;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;

import okhttp3.OkHttpClient;
import redis.clients.jedis.JedisPool;

public class SysConstant {
	// 系统参数
	public static String SYS_CHARSET = "utf-8";
	public static String project_environment = null;
	public static boolean project_test = false;
	public static boolean project_product = false;

	public static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	public static OkHttpClient okHttpClient = new OkHttpClient();

	public static String project_domain;
	public static String project_rooturl;

	// 错误码
	public static String errorCodeCnMapperPath = "easywin/config/errorCode_CN.properties";
	public static String errorCodeEnMapperPath = "easywin/config/errorCode_EN.properties";
	public static Properties errorCodeCnMapper = null;
	public static Properties errorCodeEnMapper = null;

	// spring上下文环境
	public static ApplicationContext springContext = null;

	//
	// redis
	public static JedisPool jedisPool = null;
	public static String REDIS_AUTH = null;
	public static String REDIS_IP = null;
	public static Integer REDIS_PORT = null;

	public static Pattern urlsWithEmptyPattern = Pattern.compile("(,\\s*,)|(^\\s*,)|(,\\s*$)");

	public static String jdbc_driver = null;
	public static String jdbc_url = null;
	public static String jdbc_username = null;
	public static String jdbc_password = null;

	public static String wechat_open_thirdparty_AppId;
	public static String wechat_open_thirdparty_AppSecret;
	public static String wechat_open_thirdparty_MsgVerificationToken;
	public static String wechat_open_thirdparty_MsgEncryptAndDecryptKey;

	public static void main(String[] args) {
		System.out.println(urlsWithEmptyPattern.matcher("1,  1,1").find());
	}
}
