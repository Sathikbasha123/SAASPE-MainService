package saaspe.constant;

public enum ContractType {

	ANNUAL("Annual"), MONTHTOMONTH("Month-To-Month");

	public final String val;

	private ContractType(String val) {
		this.val = val;
	}

	public static boolean annual(String contractType) {
		return contractType.equalsIgnoreCase("Annual") || contractType.equalsIgnoreCase("Annually");
	}

	public static boolean monthToMonth(String contractType) {
		return contractType.equalsIgnoreCase("Month-To-Month");
	}

}
