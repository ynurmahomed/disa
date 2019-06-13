package org.openmrs.module.disa.extension.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author machabane
 *
 */
public class DateUtil {

	private static Date parsedDate;

	public static Date deserialize(String date) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			parsedDate = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return parsedDate;
	}
}
