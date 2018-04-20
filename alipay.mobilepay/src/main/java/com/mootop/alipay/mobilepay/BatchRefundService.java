package com.mootop.alipay.mobilepay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mootop.alipay.mobilepay.config.AlipayConfig;
import com.mootop.alipay.mobilepay.util.AlipayCore;
import com.mootop.alipay.mobilepay.util.AlipayNotify;
import com.mootop.alipay.mobilepay.util.AlipaySubmit;
import com.mootop.alipay.mobilepay.util.UtilDate;

/**
 * 即时到账批量退款服务
 * @author Administrator
 *
 */
public class BatchRefundService {
	private AlipaySubmit submit;
	private AlipayConfig config;
	private AlipayNotify alipayNotify;

	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	//	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	//	private  String partner = "2088221603354510";
	//
	//	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	//	private  String seller_user_id = partner;
	//	private  String seller = "rencong001@vip.163.com";
	//	public  String key;
	//	// 商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	//	private  String private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANQRDD70OrloJeu+eszICreq8wYyOoPMLdDNQvE+1KZmp4b/9XLlI8JmuXSBCN6Z+sXvu6NoYyrHobaXrNnou3I5WtsOaXgVga9zGPIOeec6kFlcuEJewsZL6PLCLmiU6hOuzQNhxNa6Z8VOWg8VjnLfq/o6RSbqSV4wpBVvekXnAgMBAAECgYEAm1A5BcpdT/kE/NkumthPcSDUiE81J93cGDacto7rs0svmuHPY+yJ+hC99qOiWVWwolLnu8/yARzgRRhk0dtvm+y22YI3sXBzNtQDXca4tWjYKzkeTtRclTXrlHMohdDT2Q0qqm9ldZGdkGlgNqatICX/zhpQ7nrehOtOZ00Y8FECQQD17acqtuj334h280GyAttg1q7aCiPrxRHcGprMudztqlGrvIYvJot/mS+eAgnduYN+NuNOv8KqElGuHJz2NVljAkEA3MBiNgUU5uLlE7W3pXfYeuWagMB87Jlnff/u2hFN33SzCmTr3mF3AHZmSjPxro7TkMblIs2dRz1E2ATeswUKrQJAdtpxNOKLOuhMMnij4l0hGu8GOBg4fgSS+hXdhF27GR18NQx6qSTuvC8TZUDE9eRnWUM7nd5tPPZ3hTcM4Kyt0wJABswduhAZ/qoJ57t2ti+kiNQ9F63VqutmVmjlD+3see77/FvzL1vM8ES1DZ/f10IqnZCiSZoPN7xZPFWaDeUf2QJAW8X5FP3BzB3PV3/lRGOVTwoAHLSYeJKtNPeu3rkUg1A2Hd7wH6UNMSdS4/mywpXRE4V/Pg99tFwna/ZJJJdQUA==";
	//
	//	// 支付宝公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm 
	//	private  String alipay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	//
	//	// 签名方式
	//	private  String sign_type = "RSA";
	//
	//	// 字符编码格式 目前支持 gbk 或 utf-8
	//	private  String input_charset = "utf-8";
	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	private String partner;
	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	private String seller_user_id;
	private String seller;
	private String key;
	// 签名方式
	private String sign_type;

	// 字符编码格式 目前支持 gbk 或 utf-8
	private String input_charset;

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

	/**
	 * 异步通知处理进程
	 * @param notify
	 * @param processor
	 * @return
	 */
	public boolean asyncNotifyProcess(BatchRefundNotify notify,
			BatchRefundNotifyProcessor processor) {
		asyncNotifyVerify(notify);
		List<BatchRefundNotifyDetail> successList = notify.getSuccessList();
		for (int i = 0; i < successList.size(); i++)
			processor.success(notify, successList.get(i));

		List<BatchRefundNotifyDetail> failList = notify.getFailList();
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
	public String genAppRequestString(String batchNo,
			List<BatchRefundRequestDetail> details, Date refundDate,
			String notifyUrl) throws UnsupportedEncodingException {
		BatchRefundRequest request = new BatchRefundRequest(notifyUrl, batchNo,
				details, refundDate, config);

		return submit.buildRequest(request.toMap(), "get", "确认");
		//sPara.put("sign", URLEncoder.encode(sPara.get("sign"), "UTF-8"));
		//return AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
	}

	public void init() {
		submit = new AlipaySubmit();
		config = new AlipayConfig();
		alipayNotify = new AlipayNotify();
		config.partner = partner;
		config.sign_type = sign_type;
		config.input_charset = input_charset;
		config.key = key;
		submit.setAlipayConfig(config);
		alipayNotify.setAlipayConfig(config);
	}

	private boolean asyncNotifyVerify(BatchRefundNotify notify) {
		return alipayNotify.verify(notify.toMap());
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getSeller_user_id() {
		return seller_user_id;
	}

	public void setSeller_user_id(String seller_user_id) {
		this.seller_user_id = seller_user_id;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

	public String getInput_charset() {
		return input_charset;
	}

	public void setInput_charset(String input_charset) {
		this.input_charset = input_charset;
	}

}
