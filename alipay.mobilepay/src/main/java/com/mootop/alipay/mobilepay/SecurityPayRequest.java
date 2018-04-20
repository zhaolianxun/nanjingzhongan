package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.mootop.alipay.mobilepay.config.AlipayConfig;

public class SecurityPayRequest {

	SecurityPayRequest(String notifyUrl, String outTradeNo, String subject,
			BigDecimal totalFee, String body, AlipayConfig config) {
		this.notifyUrl = notifyUrl;
		this.outTradeNo = outTradeNo;
		this.subject = subject;
		this.totalFee = totalFee;
		this.body = body;
		this.paymentType = "1";
		this.partner = config.partner;
		this.inputCharset = config.input_charset;
		this.sellerId = config.seller;
	}

	private String service = "mobile.securitypay.pay";
	private String partner;
	private String inputCharset;
	private String signType;
	private String sign;
	private String notifyUrl;
	private String appId;
	private String appenv;
	private String outTradeNo;
	private String subject;
	private String paymentType;
	private String sellerId;
	private BigDecimal totalFee;
	private String body;
	private String goodsType;
	private String hbFqParam;
	private String rnCheck;
	private String itBPay;
	private String externToken;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("service", service);
		map.put("partner", partner);
		map.put("_input_charset", inputCharset);
		map.put("notify_url", notifyUrl);
		map.put("app_id", appId);
		map.put("appenv", appenv);
		map.put("out_trade_no", outTradeNo);
		map.put("subject", subject);
		map.put("payment_type", paymentType);
		map.put("seller_id", sellerId);
		map.put("total_fee", totalFee.setScale(2, RoundingMode.DOWN).toString());
		map.put("body", body);
		map.put("goods_type", goodsType);
		map.put("hb_fq_param", hbFqParam);
		map.put("rn_check", rnCheck);
		map.put("it_b_pay", itBPay);
		map.put("extern_token", externToken);
		return map;
	}

}