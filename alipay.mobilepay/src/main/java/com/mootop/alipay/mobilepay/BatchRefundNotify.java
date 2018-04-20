package com.mootop.alipay.mobilepay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.mootop.alipay.mobilepay.util.DateUtils;
import com.mootop.alipay.mobilepay.util.DecimalUtils;
import com.mootop.alipay.mobilepay.util.IntegerUtils;

public class BatchRefundNotify {
	private Date notifyTime;
	private String notifyType;
	private String notifyId;
	private String signType;
	private String sign;
	private String batchNo;
	private int successNum;
	private String resultDetails;
	private List<BatchRefundNotifyDetail> successList;
	private List<BatchRefundNotifyDetail> failList;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("notify_time",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(notifyTime));
		map.put("notify_type", notifyType);
		map.put("notify_id", notifyId);
		map.put("sign_type", signType);
		map.put("sign", sign);
		map.put("batch_no", batchNo);
		map.put("success_num", successNum + "");
		map.put("result_details", resultDetails);

		return map;
	}

	public BatchRefundNotify toBean(HttpServletRequest request) {
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
		this.successNum = IntegerUtils.parseToZero(map.get("success_num"));
		this.resultDetails = map.get("result_details");

		String[] results = resultDetails.split("\\#");
		successList = new ArrayList<BatchRefundNotifyDetail>();
		failList = new ArrayList<BatchRefundNotifyDetail>();
		for (String result : results) {
			List<String> items = new ArrayList<String>();
			String[] fen = result.split("\\$");
			items.addAll(Arrays.asList(fen[0].split("\\^")));
			BatchRefundNotifyDetail detail = new BatchRefundNotifyDetail();
			detail.setTradeNo(items.get(0));
			detail.setAmount(DecimalUtils.parseToZero(items.get(1)));
			detail.setResult(items.get(2));
			if (fen.length > 1) {
				items.addAll(Arrays.asList(fen[1].split("\\^")));
				detail.setChargesAccount(items.get(3));
				detail.setChargesAccountId(items.get(4));
				detail.setChargesAmount(DecimalUtils.parseToZero(items.get(5)));
				detail.setChargesResult(items.get(6));
			}

			if (items.get(2).equals("SUCCESS"))
				successList.add(detail);
			else
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

	public int getSuccessNum() {
		return successNum;
	}

	public void setSuccessNum(int successNum) {
		this.successNum = successNum;
	}

	public String getResultDetails() {
		return resultDetails;
	}

	public void setResultDetails(String resultDetails) {
		this.resultDetails = resultDetails;
	}

	public List<BatchRefundNotifyDetail> getSuccessList() {
		return successList;
	}

	public void setSuccessList(List<BatchRefundNotifyDetail> successList) {
		this.successList = successList;
	}

	public List<BatchRefundNotifyDetail> getFailList() {
		return failList;
	}

	public void setFailList(List<BatchRefundNotifyDetail> failList) {
		this.failList = failList;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

}