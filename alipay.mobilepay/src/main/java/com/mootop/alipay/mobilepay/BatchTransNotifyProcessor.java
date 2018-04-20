package com.mootop.alipay.mobilepay;

import java.text.ParseException;

public interface BatchTransNotifyProcessor {
	public boolean success(BatchTransNotify notify,
			BatchTransNotifyDetail detail);

	public boolean fail(BatchTransNotify notify, BatchTransNotifyDetail detail);

	public boolean track(BatchTransNotify notify, BatchTransNotifyDetail detail);

}
