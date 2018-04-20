package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mootop.alipay.mobilepay.config.AlipayConfig;
import com.mootop.alipay.mobilepay.util.DateUtils;

public class BatchTransRequest {
	private Logger logger = Logger.getLogger(BatchTransRequest.class);

	public BatchTransRequest(String batchNo, String notifyUrl,
			List<BatchTransRequestDetail> details, AlipayConfig config) {
		this.service = "batch_trans_notify";
		this.batchNo = batchNo;
		this.partner = config.partner;
		this.inputCharset = config.input_charset;
		this.notifyUrl = notifyUrl;
		this.accountName = config.accountName;
		this.payDate = new Date();
		this.email = config.seller;
		this.details = details;
	}

	private String service;
	private String partner;
	private String inputCharset;
	private String signType;
	private String sign;
	private String notifyUrl;
	private String accountName;
	private String detailData;
	private String batchNo;
	private Integer batchNum;
	private BigDecimal batchFee;
	private String email;
	private Date payDate;
	private String buyerAccountName;
	private String extendParam;

	private List<BatchTransRequestDetail> details;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("service", service);
		map.put("partner", partner);
		map.put("_input_charset", inputCharset);
		map.put("sign_type", signType);
		map.put("sign", sign);
		map.put("notify_url", notifyUrl);
		map.put("account_name", accountName);
		map.put("batch_no", batchNo);
		map.put("email", email);
		map.put("pay_date", DateUtils.formatToNull("yyyymmdd", payDate));
		map.put("buyer_account_name", buyerAccountName);
		map.put("extend_param", extendParam);

		detailData = "";
		BigDecimal batchFee = BigDecimal.ZERO;
		for (BatchTransRequestDetail detail : details) {
			detailData = detailData + detail.getSerialNumber() + "^"
					+ detail.getAccount() + "^" + detail.getAccountName() + "^"
					+ detail.getAmount() + "^" + detail.getRemark() + "|";
			batchFee = batchFee.add(detail.getAmount());
		}
		detailData = detailData.substring(0, detailData.length() - 1);
		map.put("batch_num", details.size() + "");
		map.put("detail_data", detailData);
		map.put("batch_fee", batchFee.setScale(2, RoundingMode.DOWN) + "");
		return map;
	}

	public static void main(String[] args) {
		System.out.println(null + "");
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getInputCharset() {
		return inputCharset;
	}

	public void setInputCharset(String inputCharset) {
		this.inputCharset = inputCharset;
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

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getDetailData() {
		return detailData;
	}

	public void setDetailData(String detailData) {
		this.detailData = detailData;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public Integer getBatchNum() {
		return batchNum;
	}

	public BigDecimal getBatchFee() {
		return batchFee;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getPayDate() {
		return payDate;
	}

	public String getBuyerAccountName() {
		return buyerAccountName;
	}

	public void setBuyerAccountName(String buyerAccountName) {
		this.buyerAccountName = buyerAccountName;
	}

	public String getExtendParam() {
		return extendParam;
	}

	public void setExtendParam(String extendParam) {
		this.extendParam = extendParam;
	}

}