package saaspe.constant;

public enum UnitPriceType {

	PERMONTH("per month"), PERYEAR("per year"), PERCONTRACTTENURE("per contract tenure");

	public final String val;

	private UnitPriceType(String val) {
		this.val = val;
	}

	public static boolean perMonth(String unitPriceType) {
		return unitPriceType.equalsIgnoreCase("per month");
	}

	public static boolean perYear(String unitPriceType) {
		return unitPriceType.equalsIgnoreCase("per year");
	}

	public static boolean perContracttenure(String unitPriceType) {
		return unitPriceType.equalsIgnoreCase("per contract tenure");
	}
}
