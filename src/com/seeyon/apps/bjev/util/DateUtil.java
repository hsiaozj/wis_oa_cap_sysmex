package com.seeyon.apps.bjev.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static String getDateStringYYMMDD(String dateStr, String dateFormat) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String ret = "";
		try {
			Date date = simpleDateFormat.parse(dateStr);
			ret = format.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public static String getDateString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String ret = format.format(date);
		return ret;
	}

	public static Date getDateByString(String sDate) throws ParseException {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
		Date ret = sDateFormat.parse(sDate);
		return ret;
	}
}
