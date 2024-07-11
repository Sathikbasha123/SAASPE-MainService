package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AzureRecommendationResponse {

	private String resourceId;

	private String resourceName;

	private String subscriptionId;

	private String impact;

	private String action;

	private BigDecimal savingsAmount;

	private BigDecimal annualSavingsAmount;

	private String currency;
	
	private String categroy;

}
