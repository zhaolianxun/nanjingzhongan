package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.mootop.alipay.mobilepay.util.DateUtils;
import com.mootop.alipay.mobilepay.util.DecimalUtils;
import com.mootop.alipay.mobilepay.util.IntegerUtils;

public class DirectPayAsyncNotify {
	private Logger logger = Logger.getLogger(DirectPayAsyncNotify.class);
	private Date notifyTime;
	private String notifyType;
	private String notifyId;
	private String signType;
	private String sign;
	private String outTradeNo;
	private String subject;
	private String paymentType;
	private String tradeNo;
	private String tradeStatus;
	private Date gmtCreate;
	private Date gmtPayment;
	private Date gmtClose;
	private String refundStatus;
	private Date gmtRefund;
	private String sellerEmail;
	private String buyerEmail;
	private String sellerId;
	private String buyerId;
	private BigDecimal price;
	private BigDecimal totalFee;
	private int quantity;
	private String body;
	private BigDecimal discount;
	private String isTotalFeeAdjust;
	private String useCoupon;
	private String extraCommonParam;
	private String businessScene;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("notify_time",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(notifyTime));
		map.put("notify_type", notifyType);
		map.put("notify_id", notifyId);
		map.put("sign_type", signType);
		map.put("sign", sign);
		map.put("out_trade_no", outTradeNo);
		map.put("subject", subject);
		map.put("payment_type", paymentType);
		map.put("trade_no", tradeNo);
		map.put("trade_status", tradeStatus);
		map.put("seller_id", sellerId);
		map.put("seller_email", sellerEmail);
		map.put("buyer_id", buyerId);
		map.put("buyer_email", buyerEmail);
		map.put("total_fee", totalFee.setScale(2, RoundingMode.DOWN).toString());
		map.put("quantity", quantity + "");
		map.put("price", price.setScale(2, RoundingMode.DOWN).toString());
		map.put("body", body);
		map.put("gmt_create",
				DateUtils.formatToNull("yyyy-MM-dd HH:mm:ss", gmtCreate));
		map.put("gmt_payment",
				DateUtils.formatToNull("yyyy-MM-dd HH:mm:ss", gmtPayment));
		map.put("is_total_fee_adjust", isTotalFeeAdjust);
		map.put("use_coupon", useCoupon);
		map.put("discount", discount.setScale(2, RoundingMode.DOWN).toString());
		map.put("refund_status", refundStatus);
		map.put("gmt_refund",
				DateUtils.formatToNull("yyyy-MM-dd HH:mm:ss", gmtRefund));
		return map;
	}

	public DirectPayAsyncNotify toBean(Map<String, String> map)
			throws ParseException {
		logger.debug(map);
		this.notifyTime = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("notify_time"));
		this.notifyType = map.get("notify_type");
		this.notifyId = map.get("notify_id");
		this.signType = map.get("sign_type");
		this.sign = map.get("sign");
		this.notifyId = map.get("notify_id");
		this.outTradeNo = map.get("out_trade_no");
		this.subject = map.get("subject");
		this.paymentType = map.get("payment_type");
		this.tradeNo = map.get("trade_no");
		this.tradeStatus = map.get("trade_status");
		this.tradeNo = map.get("trade_no");
		this.sellerId = map.get("seller_id");
		this.sellerEmail = map.get("seller_email");
		this.buyerId = map.get("buyer_id");
		this.buyerEmail = map.get("buyer_email");
		this.totalFee = DecimalUtils.parseToNull(map.get("total_fee"));
		this.quantity = IntegerUtils.parseToNull(map.get("quantity"));
		this.price = DecimalUtils.parseToNull(map.get("price"));
		this.body = map.get("body");
		this.gmtCreate = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("gmt_create"));
		this.gmtPayment = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("gmt_payment"));
		this.isTotalFeeAdjust = map.get("is_total_fee_adjust");
		this.useCoupon = map.get("use_coupon");
		this.discount = DecimalUtils.parseToNull(map.get("discount"));
		this.refundStatus = map.get("refund_status");
		this.gmtRefund = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("gmt_refund"));
		return this;
	}

	public DirectPayAsyncNotify toBean(HttpServletRequest request)
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

	public Date getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(Date notifyTime) {
		this.notifyTime = notifyTime;
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerEmail() {
		return sellerEmail;
	}

	public void setSellerEmail(String sellerEmail) {
		this.sellerEmail = sellerEmail;
	}

	public String getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}

	public String getBuyerEmail() {
		return buyerEmail;
	}

	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}

	public BigDecimal getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(BigDecimal totalFee) {
		this.totalFee = totalFee;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtPayment() {
		return gmtPayment;
	}

	public void setGmtPayment(Date gmtPayment) {
		this.gmtPayment = gmtPayment;
	}

	public String getIsTotalFeeAdjust() {
		return isTotalFeeAdjust;
	}

	public void setIsTotalFeeAdjust(String isTotalFeeAdjust) {
		this.isTotalFeeAdjust = isTotalFeeAdjust;
	}

	public String getUseCoupon() {
		return useCoupon;
	}

	public void setUseCoupon(String useCoupon) {
		this.useCoupon = useCoupon;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public Date getGmtRefund() {
		return gmtRefund;
	}

	public void setGmtRefund(Date gmtRefund) {
		this.gmtRefund = gmtRefund;
	}

}