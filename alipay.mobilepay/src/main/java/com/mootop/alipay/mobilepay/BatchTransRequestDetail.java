package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

public class BatchTransRequestDetail {
	private Logger logger = Logger.getLogger(BatchTransRequestDetail.class);
	private String serialNumber;
	private String account;
	private String accountName;
	private BigDecimal amount;
	private String remark;

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}