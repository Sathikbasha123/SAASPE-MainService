package saaspe.constant;

public enum ProductType {

	LICENSES("Licenses"), PLATFORM("Platform"), PROFESSIONALSERVICES("ProfessionalServices");

	public final String val;

	private ProductType(String val) {
		this.val = val;
	}

	public static boolean licenses(String productType) {
		return productType.equalsIgnoreCase("Licenses");
	}

	public static boolean platform(String productType) {
		return productType.equalsIgnoreCase("Platform");
	}

	public static boolean professionalServices(String productType) {
		return productType.equalsIgnoreCase("ProfessionalServices");
	}

}
