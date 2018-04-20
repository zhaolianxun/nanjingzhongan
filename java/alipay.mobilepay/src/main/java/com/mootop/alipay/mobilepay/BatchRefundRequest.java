package com.mootop.alipay.mobilepay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;

import com.mootop.alipay.mobilepay.config.AlipayConfig;

public class BatchRefundRequest {

	public BatchRefundRequest(String notifyUrl, String batchNo,
			List<BatchRefundRequestDetail> details, Date refundDate,
			AlipayConfig config) {
		this.notifyUrl = notifyUrl;
		this.batchNo = batchNo;
		this.details = details;
		this.refundDate = refundDate;
		this.partner = config.partner;
		this.inputCharset = config.input_charset;
		this.sellerUserId = config.seller_user_id;
	}

	private String service = "refund_fastpay_by_platform_pwd";
	private String partner;
	private String inputCharset;
	private String signType;
	private String sign;
	private String notifyUrl;
	private String sellerEmail;
	private String sellerUserId;
	private Date refundDate;
	private String batchNo;
	private int batchNum;
	private String detailData;
	private List<BatchRefundRequestDetail> details;

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("service", service);
		map.put("partner", partner);
		map.put("_input_charset", inputCharset);
		map.put("notify_url", notifyUrl);
		map.put("seller_email", sellerEmail);
		map.put("seller_user_id", sellerUserId);
		map.put("refund_date",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(refundDate));
		map.put("batch_no", batchNo);

		detailData = "";
		BigDecimal batchFee = BigDecimal.ZERO;
		for (BatchRefundRequestDetail detail : details) {
			detailData = detailData + detail.getTradeNo() + "^"
					+ detail.getAmount() + "^" + detail.getReason() + "#";
			batchFee = batchFee.add(detail.getAmount());
		}
		detailData = detailData.substring(0, detailData.length() - 1);
		batchNum = details.size();
		map.put("detail_data", detailData);
		map.put("batch_num", batchNum + "");

		return map;
	}
}