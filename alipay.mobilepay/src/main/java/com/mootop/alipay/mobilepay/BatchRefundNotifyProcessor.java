package com.mootop.alipay.mobilepay;

import java.text.ParseException;

public interface BatchRefundNotifyProcessor {
	public boolean success(BatchRefundNotify notify,
			BatchRefundNotifyDetail detail);

	public boolean fail(BatchRefundNotify notify, BatchRefundNotifyDetail detail);

	public boolean track(BatchRefundNotify notify,
			BatchRefundNotifyDetail detail);

}
