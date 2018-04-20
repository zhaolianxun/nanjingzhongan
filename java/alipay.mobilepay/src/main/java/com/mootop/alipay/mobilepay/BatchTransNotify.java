package com.mootop.alipay.mobilepay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.mootop.alipay.mobilepay.util.DateUtils;
import com.mootop.alipay.mobilepay.util.DecimalUtils;

public class BatchTransNotify {
	private Date notifyTime;
	private String notifyType;
	private String notifyId;
	private String signType;
	private String sign;
	private String batchNo;
	private String payUserId;
	private String payUserName;
	private String payAccountNo;
	private String successDetails;
	private String failDetails;
	private List<BatchTransNotifyDetail> successList;
	private List<BatchTransNotifyDetail> failList;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("notify_time",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(notifyTime));
		map.put("notify_type", notifyType);
		map.put("notify_id", notifyId);
		map.put("sign_type", signType);
		map.put("sign", sign);
		map.put("batch_no", batchNo);
		map.put("pay_user_id", payUserId);
		map.put("pay_user_name", payUserName);
		map.put("pay_account_no", payAccountNo);
		map.put("success_details", successDetails);
		map.put("fail_details", failDetails);

		return map;
	}

	public BatchTransNotify toBean(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			map.put(name, request.getParameter(name));
		}
		this.notifyTime = DateUtils.parseToNull("yyyy-MM-dd HH:mm:ss",
				map.get("notify_time"));
		this.notifyType = map.get("notify_type");
		this.notifyId = map.get("notify_id");
		this.signType = map.get("sign_type");
		this.sign = map.get("sign");
		this.batchNo = map.get("batch_no");
		this.payUserId = map.get("pay_user_id");
		this.payUserName = map.get("pay_user_name");
		this.payAccountNo = map.get("pay_account_no");
		this.successDetails = map.get("success_details");
		this.failDetails = map.get("fail_details");

		String[] successes = successDetails.split("\\|");
		successList = new ArrayList<BatchTransNotifyDetail>();
		for (String success : successes) {
			String[] items = success.split("\\^");
			BatchTransNotifyDetail detail = new BatchTransNotifyDetail();
			detail.setSerialNumber(items[0]);
			detail.setAccount(items[1]);
			detail.setAccountName(items[2]);
			detail.setAmount(DecimalUtils.parseToNull(items[3]));
			detail.setStatus(items[4]);
			detail.setResult(items[5]);
			detail.setTradeNo(items[6]);
			detail.setFinishTime(DateUtils.parseToNull("yyyyMMddHHmmss",
					items[6]));
			successList.add(detail);
		}

		String[] fails = failDetails.split("\\|");
		failList = new ArrayList<BatchTransNotifyDetail>();
		for (String fail : fails) {
			String[] items = fail.split("\\^");
			BatchTransNotifyDetail detail = new BatchTransNotifyDetail();
			detail.setSerialNumber(items[0]);
			detail.setAccount(items[1]);
			detail.setAccountName(items[2]);
			detail.setAmount(DecimalUtils.parseToNull(items[3]));
			detail.setStatus(items[4]);
			detail.setResult(items[5]);
			detail.setTradeNo(items[6]);
			detail.setFinishTime(DateUtils.parseToNull("yyyyMMddHHmmss",
					items[6]));
			failList.add(detail);
		}
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

	public void setSign_type(String signType) {
		this.signType = signType;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getPayUserId() {
		return payUserId;
	}

	public void setPayUserId(String payUserId) {
		this.payUserId = payUserId;
	}

	public String getPayUserName() {
		return payUserName;
	}

	public void setPayUserName(String payUserName) {
		this.payUserName = payUserName;
	}

	public String getPayAccountNo() {
		return payAccountNo;
	}

	public void setPayAccountNo(String payAccountNo) {
		this.payAccountNo = payAccountNo;
	}

	public String getSuccessDetails() {
		return successDetails;
	}

	public void setSuccessDetails(String successDetails) {
		this.successDetails = successDetails;
	}

	public String getFailDetails() {
		return failDetails;
	}

	public void setFailDetails(String failDetails) {
		this.failDetails = failDetails;
	}

	public List<BatchTransNotifyDetail> getSuccessList() {
		return successList;
	}

	public void setSuccessList(List<BatchTransNotifyDetail> successList) {
		this.successList = successList;
	}

	public List<BatchTransNotifyDetail> getFailList() {
		return failList;
	}

	public void setFailList(List<BatchTransNotifyDetail> failList) {
		this.failList = failList;
	}

}