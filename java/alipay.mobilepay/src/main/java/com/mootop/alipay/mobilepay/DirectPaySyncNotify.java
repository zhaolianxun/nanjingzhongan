package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.mootop.alipay.mobilepay.util.DateUtils;
import com.mootop.alipay.mobilepay.util.DecimalUtils;

public class DirectPaySyncNotify {
	private Logger logger = Logger.getLogger(DirectPaySyncNotify.class);
	private String isSuccess;
	private String signType;
	private String sign;
	private String outTradeNo;
	private String subject;
	private String paymentType;
	private String exterface;
	private String tradeNo;
	private String tradeStatus;
	private String notifyId;
	private Date notifyTime;
	private String notifyType;
	private String sellerEmail;
	private String buyerEmail;
	private String sellerId;
	private String buyerId;
	private BigDecimal totalFee;
	private String body;
	private String extraCommonParam;

	public DirectPaySyncNotify toBean(Map<String, String> map)
			throws ParseException {
		logger.debug(map);
		this.isSuccess = map.get("is_success");
		this.signType = map.get("sign_type");
		this.sign = map.get("sign");
		this.outTradeNo = map.get("out_trade_no");
		this.subject = map.get("subject");
		this.paymentType = map.get("payment_type");
		this.exterface = map.get("exterface");
		this.tradeNo = map.get("trade_no");
		this.tradeStatus = map.get("trade_status");
		this.notifyId = map.get("notify_id");
		this.notifyTime = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("notify_time"));
		this.notifyType = map.get("notify_type");
		this.sellerEmail = map.get("seller_email");
		this.buyerEmail = map.get("buyer_email");
		this.sellerId = map.get("seller_id");
		this.buyerId = map.get("buyer_id");
		this.totalFee = DecimalUtils.parseToNull(map.get("total_fee"));
		this.body = map.get("body");
		this.extraCommonParam = map.get("extra_common_param");
		return this;
	}

	public DirectPaySyncNotify toBean(HttpServletRequest request)
			throws ParseException {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			map.put(name, request.getParameter(name));
		}
		toBean(map);
		return this;
	}

}