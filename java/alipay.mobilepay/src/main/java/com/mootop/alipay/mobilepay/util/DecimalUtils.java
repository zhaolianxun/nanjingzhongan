package com.mootop.alipay.mobilepay.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DecimalUtils {

	public static BigDecimal parseToNull(String source) {
		if (source == null || source.equals(""))
			return null;
		return new BigDecimal(source);
	}

	public static BigDecimal parseToZero(String source) {
		if (source == null || source.equals(""))
			return BigDecimal.ZERO;
		return new BigDecimal(source);
	}
}
