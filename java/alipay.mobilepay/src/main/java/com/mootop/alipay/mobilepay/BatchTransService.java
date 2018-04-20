package com.mootop.alipay.mobilepay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.mootop.alipay.mobilepay.config.AlipayConfig;
import com.mootop.alipay.mobilepay.util.AlipayCore;
import com.mootop.alipay.mobilepay.util.AlipayNotify;
import com.mootop.alipay.mobilepay.util.AlipaySubmit;
import com.mootop.alipay.mobilepay.util.UtilDate;

/**
 * 移动支付服务
 * @author Administrator
 *
 */
public class BatchTransService {
	private AlipaySubmit submit;
	private AlipayConfig config;
	private AlipayNotify alipayNotify;

	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	//	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	//	private static String partner = "2088221603354510";
	//
	//	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	//	private static String seller_user_id = partner;
	//	private static String seller = "rencong001@vip.163.com";
	//	public static String key;
	//	// 商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	//	private static String private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANQRDD70OrloJeu+eszICreq8wYyOoPMLdDNQvE+1KZmp4b/9XLlI8JmuXSBCN6Z+sXvu6NoYyrHobaXrNnou3I5WtsOaXgVga9zGPIOeec6kFlcuEJewsZL6PLCLmiU6hOuzQNhxNa6Z8VOWg8VjnLfq/o6RSbqSV4wpBVvekXnAgMBAAECgYEAm1A5BcpdT/kE/NkumthPcSDUiE81J93cGDacto7rs0svmuHPY+yJ+hC99qOiWVWwolLnu8/yARzgRRhk0dtvm+y22YI3sXBzNtQDXca4tWjYKzkeTtRclTXrlHMohdDT2Q0qqm9ldZGdkGlgNqatICX/zhpQ7nrehOtOZ00Y8FECQQD17acqtuj334h280GyAttg1q7aCiPrxRHcGprMudztqlGrvIYvJot/mS+eAgnduYN+NuNOv8KqElGuHJz2NVljAkEA3MBiNgUU5uLlE7W3pXfYeuWagMB87Jlnff/u2hFN33SzCmTr3mF3AHZmSjPxro7TkMblIs2dRz1E2ATeswUKrQJAdtpxNOKLOuhMMnij4l0hGu8GOBg4fgSS+hXdhF27GR18NQx6qSTuvC8TZUDE9eRnWUM7nd5tPPZ3hTcM4Kyt0wJABswduhAZ/qoJ57t2ti+kiNQ9F63VqutmVmjlD+3see77/FvzL1vM8ES1DZ/f10IqnZCiSZoPN7xZPFWaDeUf2QJAW8X5FP3BzB3PV3/lRGOVTwoAHLSYeJKtNPeu3rkUg1A2Hd7wH6UNMSdS4/mywpXRE4V/Pg99tFwna/ZJJJdQUA==";
	//
	//	// 支付宝公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm 
	//	private static String alipay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	//
	//	// 签名方式
	//	private static String sign_type = "RSA";
	//
	//	// 字符编码格式 目前支持 gbk 或 utf-8
	//	private static String input_charset = "utf-8";
	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	private static String partner;

	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	private static String seller_user_id;
	private static String seller;
	public static String key;
	public static String accountName;
	// 商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	private static String private_key;

	// 支付宝公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm 
	private static String alipay_public_key;

	// 签名方式
	private static String sign_type;

	// 字符编码格式 目前支持 gbk 或 utf-8
	private static String input_charset;

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

	/**
	 * 异步通知处理进程
	 * @param notify
	 * @param processor
	 * @return
	 */
	public boolean asyncNotifyProcess(BatchTransNotify notify,
			BatchTransNotifyProcessor processor) {
		asyncNotifyVerify(notify);

		List<BatchTransNotifyDetail> successList = notify.getSuccessList();
		for (int i = 0; i < successList.size(); i++)
			processor.success(notify, successList.get(i));

		List<BatchTransNotifyDetail> failList = notify.getFailList();
		for (int i = 0; i < failList.size(); i++)
			processor.fail(notify, failList.get(i));

		return true;
	}

	/**
	 * 生成APP支付链接字符串
	 * @param notify
	 * @param processor
	 * @return
	 */
	public String genAppRequestString(String batchNo, String notifyUrl,
			List<BatchTransRequestDetail> details)
			throws UnsupportedEncodingException {
		BatchTransRequest request = new BatchTransRequest(batchNo, notifyUrl,
				details, config);
		return submit.buildRequest(request.toMap(), "get", "确认");
		//sPara.put("sign", URLEncoder.encode(sPara.get("sign"), "UTF-8"));
		//return AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
	}

	public void init() {
		submit = new AlipaySubmit();
		config = new AlipayConfig();
		alipayNotify = new AlipayNotify();
		config.partner = partner;
		config.seller = seller;
		config.sign_type = sign_type;
		config.key = key;
		submit.setAlipayConfig(config);
		alipayNotify.setAlipayConfig(config);
	}

	public static String getPartner() {
		return partner;
	}

	public static void setPartner(String partner) {
		BatchTransService.partner = partner;
	}

	public static String getSeller_user_id() {
		return seller_user_id;
	}

	public static void setSeller_user_id(String seller_user_id) {
		BatchTransService.seller_user_id = seller_user_id;
	}

	public static String getSeller() {
		return seller;
	}

	public static void setSeller(String seller) {
		BatchTransService.seller = seller;
	}

	public static String getPrivate_key() {
		return private_key;
	}

	public static void setPrivate_key(String private_key) {
		BatchTransService.private_key = private_key;
	}

	public static String getAlipay_public_key() {
		return alipay_public_key;
	}

	public static void setAlipay_public_key(String alipay_public_key) {
		BatchTransService.alipay_public_key = alipay_public_key;
	}

	public static String getSign_type() {
		return sign_type;
	}

	public static void setSign_type(String sign_type) {
		BatchTransService.sign_type = sign_type;
	}

	public static String getInput_charset() {
		return input_charset;
	}

	public static void setInput_charset(String input_charset) {
		BatchTransService.input_charset = input_charset;
	}

	public static String getKey() {
		return key;
	}

	public static void setKey(String key) {
		BatchTransService.key = key;
	}

	public static String getAccountName() {
		return accountName;
	}

	public static void setAccountName(String accountName) {
		BatchTransService.accountName = accountName;
	}

	private boolean asyncNotifyVerify(BatchTransNotify notify) {
		return alipayNotify.verify(notify.toMap());
	}

}
