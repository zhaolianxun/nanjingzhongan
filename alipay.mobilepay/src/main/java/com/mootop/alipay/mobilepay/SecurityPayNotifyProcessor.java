package com.mootop.alipay.mobilepay;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.bind.JAXBException;

public interface SecurityPayNotifyProcessor {
	public boolean tradeSuccess(SecurityPayNotify notify) throws IOException, JAXBException;

	public boolean tradeFinished(SecurityPayNotify notify);

	public boolean waitBuyerPay(SecurityPayNotify notify);

	public boolean tradeClosed(SecurityPayNotify notify);

	public boolean track(SecurityPayNotify notify);

}
