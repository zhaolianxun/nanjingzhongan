package com.mootop.alipay.mobilepay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static Date parseToNull(String pattern, String source) {
		try {
			if (source == null || source.equals(""))
				return null;

			return new SimpleDateFormat(pattern).parse(source);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String formatToNull(String pattern, Date date) {
		if (date == null)
			return null;
		return new SimpleDateFormat(pattern).format(date);
	}

	public static String formatToEmpty(String pattern, Date date) {
		if (date == null)
			return "";
		return new SimpleDateFormat(pattern).format(date);
	}
}
