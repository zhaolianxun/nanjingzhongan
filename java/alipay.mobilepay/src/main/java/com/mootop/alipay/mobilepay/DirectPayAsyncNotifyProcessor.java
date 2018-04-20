package com.mootop.alipay.mobilepay;

public interface DirectPayAsyncNotifyProcessor {
	public boolean tradeSuccess(DirectPayAsyncNotify notify);

	public boolean tradeFinished(DirectPayAsyncNotify notify);

	public boolean waitBuyerPay(DirectPayAsyncNotify notify);

	public boolean tradeClosed(DirectPayAsyncNotify notify);

	public boolean track(DirectPayAsyncNotify notify);
}
