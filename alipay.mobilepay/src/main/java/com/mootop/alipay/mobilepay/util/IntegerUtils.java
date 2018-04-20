package com.mootop.alipay.mobilepay.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IntegerUtils {

	public static Integer parseToNull(String source) {
		if (source == null || source.equals(""))
			return null;
		return new Integer(source);
	}

	public static int parseToZero(Object source) {
		if (source == null || source.equals(""))
			return 0;
		return new Integer(source.toString());
	}
}
