package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OptimizeRequest {

	private String subscriptionName;
	private String resourceId;
	private String resourceName;
	private String action;
	private String impact;
	private BigDecimal savingsAmount;
	private BigDecimal annualSavingsAmount;
	private List<String> emailAddress;
	private String categroy;
}
