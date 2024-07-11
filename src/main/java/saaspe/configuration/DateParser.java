package saaspe.configuration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DateParser {

	DateParser() {

	}

	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<>() ;

		static {
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "dd/MM/yyyy");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
		DATE_FORMAT_REGEXPS.put("^\\d{8}$", "yyyyMMdd");
		DATE_FORMAT_REGEXPS.put("^\\d{12}$", "yyyyMMddHHmm");
		DATE_FORMAT_REGEXPS.put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
		DATE_FORMAT_REGEXPS.put("^\\d{14}$", "yyyyMMddHHmmss");
		DATE_FORMAT_REGEXPS.put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
		DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
		DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
		DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
		DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
		DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}\\.\\d{2}[-+]\\d{2}:\\d{2}$",
					"yyyy-MM-dd'T'HH:mm:ss.SSS");
		}

	public static String determineDateFormat(String dateString) {
		for (Map.Entry<String, String> regexp : DATE_FORMAT_REGEXPS.entrySet()) {
			if (dateString.matches(regexp.getKey()) || dateString.toLowerCase().matches(regexp.getKey())) {
				return DATE_FORMAT_REGEXPS.get(regexp.getKey());
			}
		}
		return null;
	}

	public static Date parse(String value) {
		Date d = null;
		if (value != null) {
			String format = determineDateFormat(value);
			if (format != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				DateFormat istFormat = new SimpleDateFormat();
				TimeZone istTime = TimeZone.getTimeZone("GMT+5:30");
				istFormat.setTimeZone(istTime);
				try {
					Date date = sdf.parse(value);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.add(Calendar.HOUR, 5);
					cal.add(Calendar.MINUTE, 30);
					cal.add(Calendar.SECOND, 10);
					d = cal.getTime();
					return d;
				} catch (ParseException e) {
					return null;
				}
			}
		}
		return d;
	}

	public static String validateAndConvertToDDMMYYYY(String value) {
		String format = determineDateFormat(value);
		if(format!= null && value.contains("-")) {
			String[] date = value.split("-",3);
			int day =Integer.parseInt(date[0]);
			int month =Integer.parseInt(date[1]);
			int year =Integer.parseInt(date[2]);
			boolean validDate = isValid(day,month,year);
			if(day>=1 && validDate) {
				return value;
			}else {
				return ("Invalid date:" + value);
			}
		}else if(format!= null && value.contains("/")) {
			String[] date = value.split("/",3);
			int day =Integer.parseInt(date[0]);
			int month =Integer.parseInt(date[1]);
			int year =Integer.parseInt(date[2]);
			boolean validDate = isValid(day,month,year);
			if(day>=1 && validDate) {
				return value;
			}else {
				return ("Invalid date:" + value);
			}
		}else {
            return ("Unsupported date format: " + value);
        }
    }
	
	public static boolean isValid (int day, int month, int year) {
		boolean valid =false;
			if ((month == 4 || month == 6 || month == 9 || month == 11) && day <= 30) {
				valid = true;
			}
			else if ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12)
					&& day <= 31) {
				valid = true;
			}
			else if (month == 2) {
				if (day <= 28) {
					valid = true;
				} 
				else if (day == 29&&((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)) {
						valid = true;
				}else {
					valid = false;
				}
			}
		return valid;
	}
 
}