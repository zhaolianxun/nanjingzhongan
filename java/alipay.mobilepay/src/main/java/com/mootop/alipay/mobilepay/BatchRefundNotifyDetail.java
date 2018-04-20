package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.util.DateUtil;

import com.mootop.alipay.mobilepay.config.AlipayConfig;

public class BatchRefundNotifyDetail {
	private String tradeNo;
	private BigDecimal amount;
	private String result;
	private String chargesAccount;
	private String chargesAccountId;
	private BigDecimal chargesAmount;
	private String chargesResult;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getChargesAccount() {
		return chargesAccount;
	}

	public void setChargesAccount(String chargesAccount) {
		this.chargesAccount = chargesAccount;
	}

	public String getChargesAccountId() {
		return chargesAccountId;
	}

	public void setChargesAccountId(String chargesAccountId) {
		this.chargesAccountId = chargesAccountId;
	}

	public BigDecimal getChargesAmount() {
		return chargesAmount;
	}

	public void setChargesAmount(BigDecimal chargesAmount) {
		this.chargesAmount = chargesAmount;
	}

	public String getChargesResult() {
		return chargesResult;
	}

	public void setChargesResult(String chargesResult) {
		this.chargesResult = chargesResult;
	}

}