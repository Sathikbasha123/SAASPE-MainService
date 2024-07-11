package saaspe.constant;

public enum CloudVendors {

    AWS("AWS"), AZURE("Azure"), GCP("GCP"), ORACLE("Oracle"), DIGITALOCEAN("DigitalOcean"), ALIBABA("Alibaba");

    public final String val;

    private CloudVendors(String val) {
        this.val = val;
    }

    public static boolean azure(String vendor) {
        return vendor.equalsIgnoreCase("azure");
    }

    public static boolean aWS(String vendor) {
        return vendor.equalsIgnoreCase("aws");
    }
}
