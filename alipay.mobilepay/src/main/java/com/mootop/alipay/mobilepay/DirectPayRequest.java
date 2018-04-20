package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.mootop.alipay.mobilepay.config.AlipayConfig;

public class DirectPayRequest {

	DirectPayRequest(String notifyUrl, String returnUrl, String outTradeNo,
			String subject, BigDecimal totalFee, String body,
			AlipayConfig config) {
		this.notifyUrl = notifyUrl;
		this.outTradeNo = outTradeNo;
		this.subject = subject;
		this.totalFee = totalFee;
		this.paymentType = "1";
		this.partner = config.partner;
		this.inputCharset = config.input_charset;
		this.sellerId = config.seller_user_id;
		this.quantity = 1;
		this.price = totalFee;
		this.returnUrl = returnUrl;
	}

	private String service = "create_direct_pay_by_user";
	private String partner;
	private String inputCharset;
	private String signType;
	private String sign;
	private String notifyUrl;
	private String returnUrl;
	private String outTradeNo;
	private String subject;
	private String paymentType;
	private BigDecimal totalFee = BigDecimal.ZERO;
	private String sellerId;
	private String sellerEmail;
	private String sellerAccountName;
	private String buyerId;
	private String buyerEmail;
	private String buyerAccountName;
	private BigDecimal price = BigDecimal.ZERO;
	private int quantity;
	private String body;
	private String showUrl;
	private String paymethod;
	private String enablePaymethod;
	private String antiPhishingKey;
	private String exterInvokeIp;
	private String extraCommonParam;
	private String itBPay;
	private String token;
	private String qrPayMode;
	private int qrcodeWidth;
	private String needBuyerRealnamed;
	private String promoParam;
	private String hbFqParam;
	private String goodsType;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("service", service);
		map.put("partner", partner);
		map.put("_input_charset", inputCharset);
		map.put("notify_url", notifyUrl);
		map.put("return_url", returnUrl);
		map.put("out_trade_no", outTradeNo);
		map.put("subject", subject);
		map.put("payment_type", paymentType);
		map.put("total_fee", totalFee.setScale(2, RoundingMode.DOWN).toString());
		map.put("seller_id", sellerId);
		map.put("seller_email", sellerEmail);
		map.put("seller_account_name", sellerAccountName);
		map.put("buyer_id", buyerId);
		map.put("buyer_email", buyerEmail);
		map.put("buyer_account_name", buyerAccountName);
		map.put("price", price.setScale(2, RoundingMode.DOWN).toString());
		map.put("quantity", quantity + "");
		map.put("body", body);
		map.put("show_url", showUrl);
		map.put("paymethod", paymethod);
		map.put("enable_paymethod", enablePaymethod);
		map.put("anti_phishing_key", antiPhishingKey);
		map.put("exter_invoke_ip", exterInvokeIp);
		map.put("extra_common_param", extraCommonParam);
		map.put("it_b_pay", itBPay);
		map.put("token", token);
		map.put("qr_pay_mode", qrPayMode);
		map.put("qrcode_width", qrcodeWidth + "");
		map.put("need_buyer_realnamed", needBuyerRealnamed);
		map.put("promo_param", promoParam);
		map.put("hb_fq_param", hbFqParam);
		map.put("goods_type", goodsType);
		return map;
	}
}