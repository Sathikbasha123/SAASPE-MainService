package saaspe.constant;

public enum BillingFrequency {

	ANNUALLY("Annually"), ONETIME("OneTime"), QUARTERLY("Quarterly"), SEMUANNYALLY("Semi-Annually"), MONTHLY("Monthly");

	public final String val;

	private BillingFrequency(String val) {
		this.val = val;
	}

	public static boolean monthly(String billingFrequency) {
		return billingFrequency.equalsIgnoreCase("Monthly");
	}

	public static boolean quarterly(String billingFrequency) {
		return billingFrequency.equalsIgnoreCase("Quarterly");
	}

	public static boolean semiAnnually(String billingFrequency) {
		return billingFrequency.equalsIgnoreCase("Semi-Annually");
	}

	public static boolean annually(String billingFrequency) {
		return billingFrequency.equalsIgnoreCase("Annually");
	}

	public static boolean oneTime(String billingFrequency) {
		return billingFrequency.equalsIgnoreCase("OneTime");
	}
}
