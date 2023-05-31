package org.openmrs.module.disa.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author machabane
 *
 */
public class DateUtil {

	public static Date atMidnight(LocalDateTime date) {
		Instant instant = date
				.with(LocalTime.MIDNIGHT)
				.atZone(ZoneId.systemDefault())
				.toInstant();
		return Date.from(instant);
	}

	public static Date toDate(LocalDateTime localDateTime) {
		Instant instant = localDateTime
				.atZone(ZoneId.systemDefault())
				.toInstant();
		return Date.from(instant);
	}

	public static Date stringToDate(String date) throws ParseException {
		return new SimpleDateFormat("dd/MM/yyyy").parse(date);
	}

	public static Date dateWithLeadingZeros() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		return now.getTime();
	}

	public static boolean isValidDate(String inDate) {

		if (inDate == null)
			return false;

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

	public static Date getDateWithoutTime() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.parse(sdf.format(new Date()));
	}
}
