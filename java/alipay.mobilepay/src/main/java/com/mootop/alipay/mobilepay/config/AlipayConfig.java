package com.mootop.alipay.mobilepay.config;

import com.mootop.alipay.mobilepay.util.UtilDate;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.4
 *修改日期：2016-03-08
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {

	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	public String partner = "2088221603354510";
	public String accountName;

	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	public String seller_user_id = partner;
	public String seller = "rencong001@vip.163.com";
	// 商户的私钥
	public String key;
	// 商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	public String private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANQRDD70OrloJeu+eszICreq8wYyOoPMLdDNQvE+1KZmp4b/9XLlI8JmuXSBCN6Z+sXvu6NoYyrHobaXrNnou3I5WtsOaXgVga9zGPIOeec6kFlcuEJewsZL6PLCLmiU6hOuzQNhxNa6Z8VOWg8VjnLfq/o6RSbqSV4wpBVvekXnAgMBAAECgYEAm1A5BcpdT/kE/NkumthPcSDUiE81J93cGDacto7rs0svmuHPY+yJ+hC99qOiWVWwolLnu8/yARzgRRhk0dtvm+y22YI3sXBzNtQDXca4tWjYKzkeTtRclTXrlHMohdDT2Q0qqm9ldZGdkGlgNqatICX/zhpQ7nrehOtOZ00Y8FECQQD17acqtuj334h280GyAttg1q7aCiPrxRHcGprMudztqlGrvIYvJot/mS+eAgnduYN+NuNOv8KqElGuHJz2NVljAkEA3MBiNgUU5uLlE7W3pXfYeuWagMB87Jlnff/u2hFN33SzCmTr3mF3AHZmSjPxro7TkMblIs2dRz1E2ATeswUKrQJAdtpxNOKLOuhMMnij4l0hGu8GOBg4fgSS+hXdhF27GR18NQx6qSTuvC8TZUDE9eRnWUM7nd5tPPZ3hTcM4Kyt0wJABswduhAZ/qoJ57t2ti+kiNQ9F63VqutmVmjlD+3see77/FvzL1vM8ES1DZ/f10IqnZCiSZoPN7xZPFWaDeUf2QJAW8X5FP3BzB3PV3/lRGOVTwoAHLSYeJKtNPeu3rkUg1A2Hd7wH6UNMSdS4/mywpXRE4V/Pg99tFwna/ZJJJdQUA==";

	// 支付宝公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm 
	public String alipay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public String notify_url = "http://商户网址/create_direct_pay_by_user-JAVA-UTF-8/notify_url.jsp";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public String return_url = "http://商户网址/create_direct_pay_by_user-JAVA-UTF-8/return_url.jsp";

	// 签名方式
	public String sign_type = "RSA";

	// 调试用，创建TXT日志文件夹路径，见AlipayCore.java类中的logResult(String sWord)打印方法。
	public String log_path = "C:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public String input_charset = "utf-8";

	//退款日期 时间格式 yyyy-MM-dd HH:mm:ss
	public String refund_date = UtilDate.getDateFormatter();

	// 调用的接口名，无需修改
	public String service = "refund_fastpay_by_platform_pwd";

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

}
