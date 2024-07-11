package saaspe.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.lang3.StringUtils;

public class CommonUtil {

	private CommonUtil() {
	    throw new IllegalStateException("Utility class");
	  }
	
	private static SecureRandom random = new SecureRandom();

	public static boolean isValidEmail(String email) {
		if (email == null || email.isEmpty()) {
			return false;
		}
		String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$";
		Pattern pattern = Pattern.compile(emailRegex);
		return pattern.matcher(email).matches();
	}

	public static String getStringValue(String val) {
		return Optional.ofNullable(val).orElse(StringUtils.EMPTY).trim();
	}

	public static int tryParseInt(String value, int defaultVal) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	public static String createHash(String password, String salt) {
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 10000, 256);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return null;
		}
	}

	public static int getRandomNumber(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	public static Date convertLocalDatetoDate(LocalDate localDate) {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
	}

	public static Date firstDateOfYear() {
		LocalDate localDate = LocalDate.now();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, localDate.getYear());
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}

	public static Date lastDateOfYear() {
		LocalDate localDate = LocalDate.now();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, localDate.getYear());
		cal.set(Calendar.MONTH, 11); // 11 = december
		cal.set(Calendar.DAY_OF_MONTH, 31);
		return cal.getTime();
	}

	public static Date currentDate() {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		return Date.from(today.atStartOfDay(defaultZoneId).toInstant());
	}

	public static List<String> getDatesBetweenTwoDates(Date startDate, Date endDate) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate ssD = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate eeD = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return Stream.iterate(ssD.withDayOfMonth(1), date -> date.plusMonths(1))
				.limit(ChronoUnit.MONTHS.between(ssD, eeD.plusMonths(1))).map(date -> date.format(dateTimeFormatter))
				.collect(Collectors.toList());

	}

	public static Date dateStringtoDate(String stringDate) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date date = null;
		try {
			date = formatter.parse(stringDate);
		} catch (ParseException e) {
			throw new ParseException(e.getMessage(), 0);
		}
		return date;
	}

	public static LocalDate dateToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static Date simpleDateFormat(Date date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.parse(formatter.format(date));
	}

	public static int getDaysBasedOnDate(LocalDate localDate) {
		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		int daysInMonth = 0;
		if (month > 0) {
			switch (month) {
			case 1: // January
			case 3: // March
			case 5: // May
			case 7: // July
			case 8: // August
			case 10: // October
			case 12: // December
				daysInMonth = 31;
				break;
			case 4: // April
			case 6: // June
			case 9: // September
			case 11: // November
				daysInMonth = 30;
				break;
			case 2: // February
				if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
					daysInMonth = 29;
				} else {
					daysInMonth = 28;
				}
				break;
			default:
				daysInMonth = 0;
				break;
			}
		}
		return daysInMonth;
	}

}
