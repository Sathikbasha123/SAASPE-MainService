package saaspe.adaptor.model;

import lombok.Data;

@Data
public class ZohoCRMLicenseResponse {
	
	private int totalLicensesPurchased;
	private int availableCount;
	private int usedCount;

}
