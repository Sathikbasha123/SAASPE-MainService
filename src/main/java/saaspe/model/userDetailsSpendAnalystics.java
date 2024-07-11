package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class userDetailsSpendAnalystics {
	private String applicationId;
	private String applicationName;
	private BigDecimal applicationTotalCost;
	private BigDecimal adminCost;
	private BigDecimal perLicenseCost;
	private BigDecimal adminPerLicenseCost;
	private String currency;
}
