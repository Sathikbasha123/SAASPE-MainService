package saaspe.constant;

public enum ContractStatus {

    ACTIVE("Active"), INACTIVE("InActive"), EXPIRED("Expired");

    public final String val;

    private ContractStatus(String val) {
        this.val = val;
    }

    public static boolean active(String contractStatus) {
        return contractStatus.equalsIgnoreCase("Active");
    }

    public static boolean inActive(String contractStatus) {
        return contractStatus.equalsIgnoreCase("InActive");
    }

    public static boolean expired(String contractStatus) {
        return contractStatus.equalsIgnoreCase("Expired");
    }

}
